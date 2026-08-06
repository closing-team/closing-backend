package com.closing.closing.domain.terms.repository;

import com.closing.closing.domain.terms.entity.UserTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {
    @Query("SELECT ut.term.id FROM UserTerm ut WHERE ut.user.id = :userId")
    Set<Long> findAgreedTermIdsByUserId(@Param("userId") Long userId);
}
