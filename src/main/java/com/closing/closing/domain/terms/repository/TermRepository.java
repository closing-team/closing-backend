package com.closing.closing.domain.terms.repository;

import com.closing.closing.domain.terms.entity.Term;
import com.closing.closing.domain.terms.entity.TermType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    @Query("""
            SELECT t FROM Term t
            WHERE t.effectiveDate = (
                SELECT MAX(t2.effectiveDate) FROM Term t2 WHERE t2.type = t.type
            )
            ORDER BY t.type
            """)
    List<Term> findLatestTerms();
}
