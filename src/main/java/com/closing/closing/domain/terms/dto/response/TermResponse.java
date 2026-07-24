package com.closing.closing.domain.terms.dto.response;

import com.closing.closing.domain.terms.entity.Term;
import com.closing.closing.domain.terms.entity.TermType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TermResponse {
    private final Long termId;
    private final TermType type;
    private final String version;
    private final String content;
    private final LocalDate effectiveDate;
    private final boolean isRequired;

    private TermResponse(Term term) {
        this.termId = term.getId();
        this.type = term.getType();
        this.version = term.getVersion();
        this.content = term.getContent();
        this.effectiveDate = term.getEffectiveDate();
        this.isRequired = term.isRequired();
    }

    public static TermResponse from(Term term) {
        return new TermResponse(term);
    }
}
