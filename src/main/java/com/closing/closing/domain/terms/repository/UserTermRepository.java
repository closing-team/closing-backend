package com.closing.closing.domain.terms.repository;

import com.closing.closing.domain.terms.entity.UserTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {
}
