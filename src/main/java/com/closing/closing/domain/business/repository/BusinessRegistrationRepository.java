package com.closing.closing.domain.business.repository;

import com.closing.closing.domain.business.entity.BusinessRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRegistrationRepository extends JpaRepository<BusinessRegistration, Long> {
    Optional<BusinessRegistration> findByUserId(Long userId);
}
