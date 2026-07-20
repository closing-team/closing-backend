package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.client.BizInfoClient;
import com.closing.closing.domain.support.dto.BizInfoResDTO;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.entity.SupportStatus;
import com.closing.closing.domain.support.repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SupportSyncService {

    private static final int PAGE_SIZE = 100;
    private static final String BIZINFO_URL_PREFIX = "https://www.bizinfo.go.kr/";
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(\\d{4})\\s*[-./]?\\s*(\\d{2})\\s*[-./]?\\s*(\\d{2})");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final List<String> DIRECT_CLOSURE_KEYWORDS = List.of(
            "폐업",
            "사업정리",
            "원스톱폐업",
            "희망리턴",
            "점포철거",
            "철거비",
            "원상복구",
            "체납액징수특례");
    private static final List<String> RESTART_KEYWORDS = List.of(
            "재기지원",
            "재기사업화",
            "재도전",
            "채무조정",
            "법률자문");
    private static final List<String> SMALL_BUSINESS_KEYWORDS = List.of(
            "소상공인",
            "자영업자",
            "사업주");

    private final BizInfoClient bizInfoClient;
    private final SupportRepository supportRepository;

    public int syncClosureSupports() {
        int pageIndex = 1;
        int syncedCount = 0;

        while (true) {
            BizInfoResDTO.BizInfoResponseDTO response =
                    bizInfoClient.getSupportAnnouncements(PAGE_SIZE, pageIndex);
            List<BizInfoResDTO.BizInfoItemDTO> items = response.items();

            for (BizInfoResDTO.BizInfoItemDTO item : items) {
                if (isClosureSupport(item) && hasRequiredValues(item)) {
                    upsert(item);
                    syncedCount++;
                }
            }

            if (items.isEmpty() || pageIndex * PAGE_SIZE >= response.totalCount()) {
                break;
            }
            pageIndex++;
        }

        deleteNonClosureSupports();
        return syncedCount;
    }

    private boolean isClosureSupport(BizInfoResDTO.BizInfoItemDTO item) {
        return isClosureSupport(item.announcementTitle(), item.summary());
    }

    private boolean isClosureSupport(String title, String summary) {
        String normalizedTitle = Optional.ofNullable(title).orElse("")
                .replace(" ", "");
        boolean hasDirectKeyword = DIRECT_CLOSURE_KEYWORDS.stream()
                .anyMatch(normalizedTitle::contains);
        if (hasDirectKeyword) {
            return true;
        }

        boolean isSmallBusinessRestart = SMALL_BUSINESS_KEYWORDS.stream()
                .anyMatch(normalizedTitle::contains)
                && RESTART_KEYWORDS.stream().anyMatch(normalizedTitle::contains);
        if (isSmallBusinessRestart) {
            return true;
        }

        return normalizedTitle.contains("소상공인지원사업통합공고")
                && DIRECT_CLOSURE_KEYWORDS.stream()
                .anyMatch(Optional.ofNullable(summary).orElse("")::contains);
    }

    private boolean hasRequiredValues(BizInfoResDTO.BizInfoItemDTO item) {
        return item.announcementId() != null
                && item.announcementTitle() != null
                && item.announcementUrl() != null;
    }

    private void upsert(BizInfoResDTO.BizInfoItemDTO item) {
        ApplicationPeriod period = parseApplicationPeriod(item.applicationPeriod());
        LocalDate applyStartDate = period.startDate() == null
                ? parseRegistrationDate(item.registrationDateTime())
                : period.startDate();
        SupportStatus status = calculateStatus(period.endDate());
        String organizationName = getOrganizationName(item);
        String content = stripHtml(item.summary());
        int viewCount = item.inqireCo() == null ? 0 : item.inqireCo();

        supportRepository.findByExternalUrl(item.announcementUrl())
                .ifPresentOrElse(
                        supportInfo -> {
                            supportInfo.updateFromExternal(
                                    organizationName,
                                    item.announcementTitle(),
                                    content,
                                    applyStartDate,
                                    period.endDate(),
                                    period.text(),
                                    status,
                                    viewCount);
                            supportRepository.save(supportInfo);
                        },
                        () -> supportRepository.save(SupportInfo.builder()
                                .organizationName(organizationName)
                                .title(item.announcementTitle())
                                .content(content)
                                .applyStartDate(applyStartDate)
                                .applyEndDate(period.endDate())
                                .applicationPeriod(period.text())
                                .externalUrl(item.announcementUrl())
                                .status(status)
                                .viewCount(viewCount)
                                .build()));
    }

    private ApplicationPeriod parseApplicationPeriod(String value) {
        if (value == null || value.isBlank()) {
            return new ApplicationPeriod(null, null, null);
        }

        try {
            Matcher matcher = DATE_PATTERN.matcher(value);
            LocalDate startDate = matcher.find() ? toLocalDate(matcher) : null;
            LocalDate endDate = matcher.find() ? toLocalDate(matcher) : null;
            String text = endDate == null ? extractPeriodText(value) : null;
            return new ApplicationPeriod(startDate, endDate, text);
        } catch (DateTimeException exception) {
            return new ApplicationPeriod(null, null, value.trim());
        }
    }

    private LocalDate parseRegistrationDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            Matcher matcher = DATE_PATTERN.matcher(value);
            return matcher.find() ? toLocalDate(matcher) : null;
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private String extractPeriodText(String value) {
        String withoutDates = DATE_PATTERN.matcher(value).replaceAll("")
                .replace("~", "")
                .trim();
        return withoutDates.isBlank() ? null : withoutDates;
    }

    private LocalDate toLocalDate(Matcher matcher) {
        return LocalDate.of(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
    }

    private SupportStatus calculateStatus(LocalDate endDate) {
        return endDate != null && LocalDate.now().isAfter(endDate)
                ? SupportStatus.CLOSED
                : SupportStatus.ONGOING;
    }

    private String getOrganizationName(BizInfoResDTO.BizInfoItemDTO item) {
        return item.organizationName() == null
                ? "기관 정보 없음"
                : item.organizationName();
    }

    private String stripHtml(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String withoutTags = HTML_TAG_PATTERN.matcher(value).replaceAll(" ");
        String unescaped = HtmlUtils.htmlUnescape(withoutTags);
        return WHITESPACE_PATTERN.matcher(unescaped).replaceAll(" ").trim();
    }

    private void deleteNonClosureSupports() {
        List<SupportInfo> importedSupports =
                supportRepository.findAllByExternalUrlStartingWith(BIZINFO_URL_PREFIX);
        List<SupportInfo> nonClosureSupports = importedSupports.stream()
                .filter(support -> !isClosureSupport(
                        support.getTitle(), support.getContent()))
                .toList();
        supportRepository.deleteAll(nonClosureSupports);
    }

    private record ApplicationPeriod(
            LocalDate startDate,
            LocalDate endDate,
            String text
    ) {
    }
}
