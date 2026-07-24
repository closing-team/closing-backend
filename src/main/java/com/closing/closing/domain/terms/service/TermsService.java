package com.closing.closing.domain.terms.service;

import com.closing.closing.domain.terms.dto.request.AgreeTermsRequest;
import com.closing.closing.domain.terms.dto.response.TermResponse;
import com.closing.closing.domain.terms.entity.Term;
import com.closing.closing.domain.terms.entity.UserTerm;
import com.closing.closing.domain.terms.repository.TermRepository;
import com.closing.closing.domain.terms.repository.UserTermRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public List<TermResponse> getLatestTerms() {
        return termRepository.findLatestTerms().stream()
                .map(TermResponse::from)
                .toList();
    }

    @Transactional
    public void agreeTerms(String authorizationHeader, AgreeTermsRequest request) {
        Long userId = extractUserIdFromSignupToken(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_SIGNUP_TOKEN_NOT_FOUND));

        List<Term> latestTerms = termRepository.findLatestTerms();
        validateRequiredTerms(latestTerms, request.getTermIds());

        List<Term> agreedTerms = termRepository.findAllById(request.getTermIds());
        List<UserTerm> userTerms = agreedTerms.stream()
                .map(term -> UserTerm.builder().user(user).term(term).build())
                .toList();

        userTermRepository.saveAll(userTerms);
    }

    private Long extractUserIdFromSignupToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.AUTH_SIGNUP_TOKEN_NOT_FOUND);
        }
        String token = authorizationHeader.substring(7);
        try {
            jwtProvider.validate(token);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTH_SIGNUP_TOKEN_NOT_FOUND);
        }
        if (!jwtProvider.isSignupToken(token)) {
            throw new CustomException(ErrorCode.AUTH_SIGNUP_TOKEN_NOT_FOUND);
        }
        return jwtProvider.getUserId(token);
    }

    private void validateRequiredTerms(List<Term> latestTerms, List<Long> agreedTermIds) {
        Set<Long> agreedSet = Set.copyOf(agreedTermIds);
        boolean allRequiredAgreed = latestTerms.stream()
                .filter(Term::isRequired)
                .allMatch(term -> agreedSet.contains(term.getId()));

        if (!allRequiredAgreed) {
            throw new CustomException(ErrorCode.TERM_REQUIRED);
        }
    }
}
