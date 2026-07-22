package com.closing.closing.domain.chat.service;

import com.closing.closing.domain.chat.dto.request.MessageRequest;
import com.closing.closing.domain.chat.dto.response.MessageSendResponse;
import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.chat.entity.MessageType;
import com.closing.closing.domain.chat.repository.ChatMessageRepository;
import com.closing.closing.domain.chat.repository.ChatRoomRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String CHAT_IMAGE_DIRECTORY = "chat-messages";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ImageStorage imageStorage;

    @Transactional
    public MessageSendResponse sendMessage(
            Long userId,
            Long chatRoomId,
            MessageRequest request,
            List<MultipartFile> images
    ) {

        // 요청 형식 검증
        validateMessageRequest(request, images);

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository
                .findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));


        // 발신자가 누군지 확인, 발신자 반환
        User sender = findSender(chatRoom, userId);

        // 텍스트 메세지인지 이미지 메세지인지
        boolean hasImages = images != null && !images.isEmpty();

        // 이미지가 있으면 이미지 메세지 보내기
        if (hasImages) {
            return sendImageMessage(
                    sender,
                    chatRoom,
                    images,
                    userId
            );
        }

        // 이미지가 없으면 텍스트 메세지 보내기
        return sendTextMessage(
                sender,
                chatRoom,
                request.getContent(),
                userId
        );
    }

    // 요청 형식 검증
    private void validateMessageRequest(
            MessageRequest request,
            List<MultipartFile> images
    ) {
        // 텍스트가 있는지
        boolean hasText =
                request != null
                && request.getContent() != null
                && !request.getContent().isBlank();

        // 이미지가 있는지
        boolean hasImages =
                images != null
                && !images.isEmpty();

        // 텍스트와 이미지 모두 있으면 에러
        if (hasText && hasImages) {
            throw new CustomException(ErrorCode.MULTIPLE_CHAT_MESSAGE_TYPES);
        }

        // 텍스트와 이미지 모두 없으면 에러
        if(!hasText && !hasImages) {
            throw new CustomException(ErrorCode.EMPTY_CHAT_MESSAGE);
        }

        // 이미지만 있으면 이미지 검증
        if (hasImages) {
            validateImages(images);
        }
    }

    // 이미지 검증
    private void validateImages(List<MultipartFile> images) {
        boolean hasEmptyImage =
                images.stream()
                        .anyMatch(MultipartFile::isEmpty);

        if (hasEmptyImage) {
            throw new CustomException(ErrorCode.INVALID_CHAT_IMAGE);
        }
    }

    // 발신자 확인
    private User findSender(
            ChatRoom chatRoom,
            Long userId
    ) {
        User buyer = chatRoom.getBuyer();
        User seller = chatRoom.getSeller();

        // 구매자가 발신자
        if (buyer.getId().equals(userId)) {
            validateActiveUser(buyer);
            return buyer;
        }

        // 판매자가 발신자
        if (seller.getId().equals(userId)) {
            validateActiveUser(seller);
            return seller;
        }

        throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_FORBIDDEN);
    }

    // 발신자 검증
    private void validateActiveUser(User user) {
        if (user.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    // 텍스트 메세지 보내기
    private MessageSendResponse sendTextMessage(
            User sender,
            ChatRoom chatRoom,
            String content,
            Long userId
    ) {
        ChatMessage chatMessage =
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .content(content)
                        .messageType(MessageType.TEXT)
                        .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return MessageSendResponse.from(
                List.of(savedMessage),
                userId
        );
    }

    // 이미지 메세지 보내기
    private MessageSendResponse sendImageMessage(
            User sender,
            ChatRoom chatRoom,
            List<MultipartFile> images,
            Long userId
    ) {
        List<String> uploadedImageUrls = new ArrayList<>();

        try {
            for (MultipartFile image : images) {
                String imageUrl =
                        imageStorage.upload(
                                image,
                                CHAT_IMAGE_DIRECTORY
                        );

                uploadedImageUrls.add(imageUrl);
            }

            List<ChatMessage> imageMessages =
                    uploadedImageUrls.stream()
                            .map(imageUrl ->
                                    ChatMessage.builder()
                                            .chatRoom(chatRoom)
                                            .sender(sender)
                                            .content(imageUrl)
                                            .messageType(
                                                    MessageType.IMAGE
                                            )
                                            .build()
                            )
                            .toList();

            List<ChatMessage> savedMessages =
                    chatMessageRepository
                            .saveAllAndFlush(imageMessages);

            return MessageSendResponse.from(
                    savedMessages,
                    userId
            );
        } catch (RuntimeException exception) {
            deleteUploadedImagesSafely(
                    uploadedImageUrls
            );

            throw exception;
        }
    }

    private void deleteUploadedImagesSafely(
            List<String> imageUrls
    ) {
        for (String imageUrl : imageUrls) {
            try {
                imageStorage.delete(imageUrl);
            } catch (RuntimeException exception) {
                log.warn(
                        "채팅 이미지 정리에 실패했습니다. imageUrl={}",
                        imageUrl,
                        exception
                );
            }
        }
    }
}
