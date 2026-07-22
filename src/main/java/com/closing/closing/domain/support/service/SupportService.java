package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.exception.SupportException;
import com.closing.closing.domain.support.exception.code.SupportErrorCode;
import com.closing.closing.domain.support.repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final LocalDate LAST_END_DATE = LocalDate.of(9999, 12, 31);

    private final SupportRepository supportRepository;

    @Transactional
    public SupportResDTO.SupportDetailDTO getSupport(
            Long supportId,
            String authorizationHeader) {
        int updatedCount = supportRepository.increaseViewCount(supportId);
        if (updatedCount == 0) {
            throw new SupportException(SupportErrorCode.SUPPORT_NOT_FOUND);
        }

        SupportInfo supportInfo = supportRepository.findById(supportId)
                .orElseThrow(() -> new SupportException(
                        SupportErrorCode.SUPPORT_NOT_FOUND));

        // TODO: 인증 추가 후 토큰의 사용자 ID로 북마크 여부 조회 필요
        return SupportResDTO.SupportDetailDTO.from(supportInfo, false);
    }

    public SupportResDTO.SupportListDTO getSupports(
            String sortValue,
            String cursorValue,
            String sizeValue,
            String authorizationHeader) {
        SupportSort sort = SupportSort.from(sortValue);
        int size = parseSize(sizeValue);
        SupportCursor cursor = parseCursor(sort, cursorValue);
        Pageable pageable = PageRequest.of(0, size + 1);

        List<SupportInfo> result = findSupports(sort, cursor, pageable);
        boolean hasNext = result.size() > size;
        List<SupportInfo> supports = hasNext ? result.subList(0, size) : result;

        // TODO: 인증 추가 후 토큰의 사용자 ID로 북마크 여부 조회 필요
        List<SupportResDTO.SupportSummaryDTO> supportDTOs = supports.stream()
                .map(support -> SupportResDTO.SupportSummaryDTO.from(support, false))
                .toList();

        String nextCursor = hasNext
                ? createCursor(sort, supports.get(supports.size() - 1))
                : null;

        return SupportResDTO.SupportListDTO.builder()
                .supports(supportDTOs)
                .page(SupportResDTO.PageDTO.builder()
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    private List<SupportInfo> findSupports(
            SupportSort sort,
            SupportCursor cursor,
            Pageable pageable) {
        return switch (sort) {
            case POPULAR -> supportRepository.findAllByPopular(
                    cursor.viewCount(), cursor.supportId(), pageable);
            case LATEST -> supportRepository.findAllByLatest(
                    cursor.createdAt(), cursor.supportId(), pageable);
            case DEADLINE -> supportRepository.findAllByDeadline(
                    cursor.applyEndDate(), cursor.supportId(), LAST_END_DATE, pageable);
        };
    }

    private int parseSize(String value) {
        try {
            int size = Integer.parseInt(value);
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new SupportException(SupportErrorCode.SUPPORT_INVALID_QUERY);
            }
            return size;
        } catch (NumberFormatException exception) {
            throw new SupportException(SupportErrorCode.SUPPORT_INVALID_QUERY);
        }
    }

    private SupportCursor parseCursor(SupportSort sort, String value) {
        if (value == null || value.isBlank()) {
            return SupportCursor.initial();
        }

        int separatorIndex = value.lastIndexOf('_');
        if (separatorIndex <= 0 || separatorIndex == value.length() - 1) {
            throw new SupportException(SupportErrorCode.SUPPORT_INVALID_QUERY);
        }

        String sortCursor = value.substring(0, separatorIndex);
        String idCursor = value.substring(separatorIndex + 1);

        try {
            long supportId = Long.parseLong(idCursor);
            if (supportId <= 0) {
                throw new SupportException(SupportErrorCode.SUPPORT_INVALID_QUERY);
            }

            return switch (sort) {
                case POPULAR -> {
                    int viewCount = Integer.parseInt(sortCursor);
                    if (viewCount < 0) {
                        throw new SupportException(SupportErrorCode.SUPPORT_INVALID_QUERY);
                    }
                    yield SupportCursor.popular(viewCount, supportId);
                }
                case LATEST -> SupportCursor.latest(
                        LocalDateTime.parse(sortCursor), supportId);
                case DEADLINE -> SupportCursor.deadline(
                        LocalDate.parse(sortCursor), supportId);
            };
        } catch (NumberFormatException | DateTimeParseException exception) {
            throw new SupportException(SupportErrorCode.SUPPORT_INVALID_QUERY);
        }
    }

    private String createCursor(SupportSort sort, SupportInfo supportInfo) {
        String sortCursor = switch (sort) {
            case POPULAR -> String.valueOf(supportInfo.getViewCount());
            case LATEST -> supportInfo.getCreatedAt().toString();
            case DEADLINE -> (supportInfo.getApplyEndDate() == null
                    ? LAST_END_DATE
                    : supportInfo.getApplyEndDate()).toString();
        };
        return sortCursor + "_" + supportInfo.getId();
    }

    private enum SupportSort {
        POPULAR,
        LATEST,
        DEADLINE;

        private static SupportSort from(String value) {
            try {
                return SupportSort.valueOf(value);
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new SupportException(SupportErrorCode.SUPPORT_INVALID_QUERY);
            }
        }
    }

    private record SupportCursor(
            Long supportId,
            Integer viewCount,
            LocalDateTime createdAt,
            LocalDate applyEndDate
    ) {
        private static SupportCursor initial() {
            return new SupportCursor(null, null, null, null);
        }

        private static SupportCursor popular(int viewCount, long supportId) {
            return new SupportCursor(supportId, viewCount, null, null);
        }

        private static SupportCursor latest(LocalDateTime createdAt, long supportId) {
            return new SupportCursor(supportId, null, createdAt, null);
        }

        private static SupportCursor deadline(LocalDate applyEndDate, long supportId) {
            return new SupportCursor(supportId, null, null, applyEndDate);
        }
    }
}
