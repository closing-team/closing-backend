package com.closing.closing.domain.support.repository;

import com.closing.closing.domain.support.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUser_IdAndSupportInfo_Id(Long userId, Long supportId);

    Optional<Bookmark> findByUser_IdAndSupportInfo_Id(Long userId, Long supportId);
}
