package com.closing.closing.domain.support.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class BizInfoResDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BizInfoResponseDTO(
            List<BizInfoItemDTO> jsonArray
    ) {
        public List<BizInfoItemDTO> items() {
            return jsonArray == null ? List.of() : jsonArray;
        }

        public int totalCount() {
            return items().isEmpty() || items().get(0).totCnt() == null
                    ? 0
                    : items().get(0).totCnt();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BizInfoItemDTO(
            String pblancId,
            String pblancNm,
            String pblancUrl,
            String jrsdInsttNm,
            String excInsttNm,
            String bsnsSumryCn,
            String reqstBeginEndDe,
            String trgetNm,
            String reqstMthPapersCn,
            String refrncNm,
            String rceptEngnHmpgUrl,
            Integer inqireCo,
            Integer totCnt,
            @JsonAlias({"hashtags", "hashTags"}) String hashtags,
            String seq,
            String title,
            String link,
            String author,
            String description,
            String reqstDt,
            String creatPnttm,
            String pubDate
    ) {
        public String announcementId() {
            return firstNotBlank(pblancId, seq);
        }

        public String announcementTitle() {
            return firstNotBlank(pblancNm, title);
        }

        public String announcementUrl() {
            return firstNotBlank(pblancUrl, link);
        }

        public String organizationName() {
            return firstNotBlank(jrsdInsttNm, author, excInsttNm);
        }

        public String summary() {
            return firstNotBlank(bsnsSumryCn, description);
        }

        public String applicationPeriod() {
            return firstNotBlank(reqstBeginEndDe, reqstDt);
        }

        public String registrationDateTime() {
            return firstNotBlank(creatPnttm, pubDate);
        }

        private String firstNotBlank(String... values) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
            return null;
        }
    }
}
