package com.closing.closing.domain.user.repository;

import com.closing.closing.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoIdAndDeletedAtIsNull(String kakaoId);
}
