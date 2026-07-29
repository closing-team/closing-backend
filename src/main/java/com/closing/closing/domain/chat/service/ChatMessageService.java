package com.closing.closing.domain.chat.service;

import com.closing.closing.domain.chat.dto.request.MessageHistoryRequest;
import com.closing.closing.domain.chat.dto.request.MessageRequest;
import com.closing.closing.domain.chat.dto.response.MessageHistoryListResponse;
import com.closing.closing.domain.chat.dto.response.MessageResponse;
import com.closing.closing.domain.chat.dto.response.MessageSendResponse;
import com.closing.closing.domain.chat.dto.response.websocket.ChatMessageResult;
import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.chat.entity.MessageType;
import com.closing.closing.domain.chat.repository.ChatMessageRepository;
import com.closing.closing.domain.chat.repository.ChatRoomRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.domain.product.dto.response.CursorPageResponse;
import com.closing.closing.global.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
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

    // 메세지 보내기
    @Transactional
    public ChatMessageResult sendMessage(
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
                    images
            );
        }

        // 이미지가 없으면 텍스트 메세지 보내기
        return sendTextMessage(
                sender,
                chatRoom,
                request.getContent()
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
    private ChatMessageResult sendTextMessage(
            User sender,
            ChatRoom chatRoom,
            String content
    ) {
        ChatMessage chatMessage =
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .content(content)
                        .messageType(MessageType.TEXT)
                        .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return ChatMessageResult.from(
                chatRoom,
                savedMessage
        );
    }

    // 이미지 메세지 보내기
    private ChatMessageResult sendImageMessage(
            User sender,
            ChatRoom chatRoom,
            List<MultipartFile> images
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

            return ChatMessageResult.from(
                    chatRoom,
                    savedMessages
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

    // 메세지 읽음 처리
    @Transactional
    public void readMessage(
            Long userId,
            Long chatRoomId
    ) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자인지 확인
        findSender(chatRoom, userId);

        // 채팅방 내 상대방이 보낸 모든 미읽음 메세지를 읽음으로 처리
        chatMessageRepository.markAllUnreadMessagesAsRead(chatRoomId, userId);
    }

    // 메세지 히스토리 조회
    public MessageHistoryListResponse<Long> getMessageHistoryList(
            MessageHistoryRequest request,
            Long chatRoomId,
            Long userId
    ) {

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자인지 확인
        findSender(chatRoom, userId);

        Pageable pageable = PageRequest.of(0, request.getSize() + 1);

        List<ChatMessage> chatMessageList = chatMessageRepository.findMessageHistory(
                chatRoomId,
                request.getCursor(),
                pageable
        );

        // 요청한 개수보다 한 개 더 조회됐다면 다음 페이지가 존재
        boolean hasNext = chatMessageList.size() > request.getSize();

        // hasNext 확인용으로 추가 조회한 마지막 메시지는 응답에서 제외
        List<ChatMessage> pageMessages = new ArrayList<>(
                hasNext
                        ? chatMessageList.subList(0, request.getSize())
                        : chatMessageList
        );

        // 현재 페이지에서 가장 오래된 메시지 ID를 다음 조회의 커서로 사용
        Long nextCursor = hasNext && !pageMessages.isEmpty()
                ? pageMessages.get(pageMessages.size() - 1).getId()
                : null;

        // Repository는 최신순으로 조회하므로 화면 표시를 위해 오래된순으로 변경
        Collections.reverse(pageMessages);

        List<MessageResponse> messageResponses = pageMessages.stream()
                .map(message -> MessageResponse.from(message, userId))
                .toList();

        CursorPageResponse<Long> pageResponse =
                CursorPageResponse.of(nextCursor, hasNext);

        return new MessageHistoryListResponse<>(
                messageResponses,
                pageResponse
        );
    }

    @Transactional
    public ChatMessageResult sendTextMessage(
          Long userId,
          Long chatRoomId,
          String content
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        User sender = findSender(chatRoom, userId);

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return ChatMessageResult.from(
                chatRoom,
                savedMessage
        );
    }
}
