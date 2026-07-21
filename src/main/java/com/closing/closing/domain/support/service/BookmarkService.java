package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.entity.Bookmark;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.exception.SupportException;
import com.closing.closing.domain.support.exception.code.SupportErrorCode;
import com.closing.closing.domain.support.repository.BookmarkRepository;
import com.closing.closing.domain.support.repository.SupportRepository;
import com.closing.closing.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final SupportRepository supportRepository;
    private final EntityManager entityManager;

    @Transactional
    public BookmarkResDTO.BookmarkCreateDTO createBookmark(Long userId, Long supportId) {
        SupportInfo supportInfo = supportRepository.findById(supportId)
                .orElseThrow(() -> new SupportException(
                        SupportErrorCode.SUPPORT_NOT_FOUND));

        if (bookmarkRepository.existsByUser_IdAndSupportInfo_Id(userId, supportId)) {
            throw new SupportException(SupportErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        User user = entityManager.getReference(User.class, userId);
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .supportInfo(supportInfo)
                .build();

        try {
            Bookmark savedBookmark = bookmarkRepository.saveAndFlush(bookmark);
            return BookmarkResDTO.BookmarkCreateDTO.from(savedBookmark);
        } catch (DataIntegrityViolationException exception) {
            // 사전 중복 확인 이후 동시에 저장된 경우에도 UNIQUE 제약으로 중복을 차단한다.
            if (isUniqueConstraintViolation(exception)) {
                throw new SupportException(SupportErrorCode.BOOKMARK_ALREADY_EXISTS);
            }
            throw exception;
        }
    }

    private boolean isUniqueConstraintViolation(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
