package io.github.depromeet.knockknockbackend.domain.notification.service;


import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import io.github.depromeet.knockknockbackend.domain.group.domain.Group;
import io.github.depromeet.knockknockbackend.domain.group.domain.vo.GroupBaseInfoVo;
import io.github.depromeet.knockknockbackend.domain.notification.domain.DeviceToken;
import io.github.depromeet.knockknockbackend.domain.notification.domain.NightCondition;
import io.github.depromeet.knockknockbackend.domain.notification.domain.Notification;
import io.github.depromeet.knockknockbackend.domain.notification.domain.NotificationReceiver;
import io.github.depromeet.knockknockbackend.domain.notification.domain.repository.DeviceTokenRepository;
import io.github.depromeet.knockknockbackend.domain.notification.domain.repository.NotificationRepository;
import io.github.depromeet.knockknockbackend.domain.notification.domain.vo.NotificationReactionCountInfoVo;
import io.github.depromeet.knockknockbackend.domain.notification.exception.FcmResponseException;
import io.github.depromeet.knockknockbackend.domain.notification.exception.NotificationForbiddenException;
import io.github.depromeet.knockknockbackend.domain.notification.exception.NotificationNotFoundException;
import io.github.depromeet.knockknockbackend.domain.notification.presentation.dto.request.RegisterFcmTokenRequest;
import io.github.depromeet.knockknockbackend.domain.notification.presentation.dto.request.SendInstanceRequest;
import io.github.depromeet.knockknockbackend.domain.notification.presentation.dto.response.*;
import io.github.depromeet.knockknockbackend.domain.reaction.domain.NotificationReaction;
import io.github.depromeet.knockknockbackend.domain.reaction.domain.repository.NotificationReactionRepository;
import io.github.depromeet.knockknockbackend.domain.user.domain.User;
import io.github.depromeet.knockknockbackend.global.utils.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private static final boolean CREATED_DELETED_STATUS = false;
    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationReactionRepository notificationReactionRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public QueryNotificationListLatestResponse queryListLatest() {
        List<Notification> notifications =
                notificationRepository.findSliceLatestByReceiver(SecurityUtils.getCurrentUserId());

        List<QueryNotificationListResponseElement> notificationListResponseElements =
                getNotificationListResponseElements(notifications);

        return new QueryNotificationListLatestResponse(notificationListResponseElements);
    }

    @Transactional(readOnly = true)
    public QueryNotificationListResponse queryListByGroupId(Pageable pageable, Long groupId) {
        Slice<Notification> notifications =
                notificationRepository.findAllByGroupIdAndDeleted(
                        groupId, CREATED_DELETED_STATUS, pageable);

        List<QueryNotificationListResponseElement> notificationListResponseElements =
                getNotificationListResponseElements(notifications.getContent());

        Optional<GroupBaseInfoVo> groupBaseInfoVo =
                notifications.stream()
                        .findFirst()
                        .map(notification -> notification.getGroup().getGroupBaseInfoVo());

        return new QueryNotificationListResponse(
                groupBaseInfoVo.orElse(null),
                new SliceImpl(
                        notificationListResponseElements,
                        notifications.getPageable(),
                        notifications.hasNext()));
    }

    @Transactional
    public void registerFcmToken(RegisterFcmTokenRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Optional<DeviceToken> deviceTokenOptional =
                deviceTokenRepository.findByDeviceId(request.getDeviceId());

        deviceTokenOptional.ifPresentOrElse(
                deviceToken -> {
                    if (deviceToken.getUserId().equals(currentUserId)) {
                        deviceTokenRepository.save(deviceToken.changeToken(request.getToken()));
                    } else {
                        deviceTokenRepository.deleteById(deviceToken.getId());
                        entityManager.flush();
                        deviceTokenRepository.save(
                                DeviceToken.of(
                                        currentUserId, request.getDeviceId(), request.getToken()));
                    }
                },
                () ->
                        deviceTokenRepository.save(
                                DeviceToken.of(
                                        currentUserId, request.getDeviceId(), request.getToken())));
    }

    @Transactional
    public void sendInstance(SendInstanceRequest request) {
        Long sendUserId = SecurityUtils.getCurrentUserId();

        List<DeviceToken> deviceTokens = getDeviceTokens(request, sendUserId);
        List<String> tokens = getTokens(deviceTokens);
        MulticastMessage multicastMessage = makeMulticastMessageForFcm(request, tokens);

        try {
            BatchResponse batchResponse =
                    FirebaseMessaging.getInstance().sendMulticast(multicastMessage);
            if (batchResponse.getFailureCount() >= 1) {
                logFcmMessagingException(batchResponse);
            }
        } catch (FirebaseMessagingException e) {
            log.error("[**FCM notification sending Error] {} ", e.getMessage());
            throw FcmResponseException.EXCEPTION;
        }

        Notification notification =
                Notification.of(
                        request.getTitle(),
                        request.getContent(),
                        request.getImageUrl(),
                        Group.of(request.getGroupId()),
                        User.of(sendUserId),
                        LocalDateTime.now());
        notification.addReceivers(
                deviceTokens.stream()
                        .map(
                                deviceToken ->
                                        new NotificationReceiver(
                                                notification,
                                                User.of(deviceToken.getUserId()),
                                                deviceToken.getToken()))
                        .collect(Collectors.toList()));
        notificationRepository.save(notification);
    }

    public void deleteByNotificationId(Long notificationId) {
        Notification notification = queryNotificationById(notificationId);
        validateDeletePermission(notification);
        notification.deleteNotification();
        notificationRepository.save(notification);
    }

    public List<QueryNotificationListResponseElement> getNotificationListResponseElements(
            List<Notification> notifications) {

        List<NotificationReaction> notificationReactions =
                retrieveNotificationReactions(notifications);

        return generateQueryNotificationListResponseElements(notifications, notificationReactions);
    }

    private List<QueryNotificationListResponseElement>
            generateQueryNotificationListResponseElements(
                    List<Notification> notifications,
                    List<NotificationReaction> notificationReactions) {
        return notifications.stream()
                .map(
                        notification -> {
                            MyNotificationReactionResponseElement
                                    myNotificationReactionResponseElement = null;
                            Optional<NotificationReaction> myNotificationReaction =
                                    notificationReactions.stream()
                                            .filter(
                                                    notificationReaction ->
                                                            notification.equals(
                                                                    notificationReaction
                                                                            .getNotification()))
                                            .findAny();

                            List<NotificationReactionCountInfoVo> notificationReactionCountInfoVo =
                                    notificationReactionRepository.findAllCountByNotification(
                                            notification);

                            if (myNotificationReaction.isPresent()) {
                                myNotificationReactionResponseElement =
                                        MyNotificationReactionResponseElement.builder()
                                                .notificationReactionId(
                                                        myNotificationReaction.get().getId())
                                                .reactionId(
                                                        myNotificationReaction
                                                                .get()
                                                                .getReaction()
                                                                .getId())
                                                .build();
                            }

                            QueryNotificationReactionResponseElement
                                    notificationReactionResponseElement =
                                            QueryNotificationReactionResponseElement.builder()
                                                    .myReactionInfo(
                                                            myNotificationReactionResponseElement)
                                                    .reactionCountInfos(
                                                            notificationReactionCountInfoVo)
                                                    .build();

                            return QueryNotificationListResponseElement.builder()
                                    .notificationId(notification.getId())
                                    .title(notification.getTitle())
                                    .content(notification.getContent())
                                    .imageUrl(notification.getImageUrl())
                                    .sendAt(notification.getSendAt())
                                    .sendUserId(notification.getSendUser().getId())
                                    .reactions(notificationReactionResponseElement)
                                    .build();
                        })
                .collect(Collectors.toList());
    }

    private void logFcmMessagingException(BatchResponse batchResponse) {
        log.error(
                "[**FCM notification sending Error] successCount : {}, failureCount : {} ",
                batchResponse.getSuccessCount(),
                batchResponse.getFailureCount());
        batchResponse.getResponses().stream()
                .filter(sendResponse -> sendResponse.getException() != null)
                .forEach(
                        sendResponse ->
                                log.error(
                                        "[**FCM notification sending Error] errorCode: {}, errorMessage : {}",
                                        sendResponse.getException().getErrorCode(),
                                        sendResponse.getException().getMessage()));
    }

    private MulticastMessage makeMulticastMessageForFcm(
            SendInstanceRequest request, List<String> tokens) {
        return MulticastMessage.builder()
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(request.getTitle())
                                .setBody(request.getContent())
                                .setImage(request.getImageUrl())
                                .build())
                .addAllTokens(tokens)
                .build();
    }

    private List<DeviceToken> getDeviceTokens(SendInstanceRequest request, Long sendUserId) {
        Boolean nightOption = null;
        if (NightCondition.isNight()) {
            nightOption = true;
        }

        return notificationRepository.findTokenByGroupAndOptionAndNonBlock(
                sendUserId, request.getGroupId(), nightOption);
    }

    private List<String> getTokens(List<DeviceToken> deviceTokens) {
        return deviceTokens.stream().map(DeviceToken::getToken).collect(Collectors.toList());
    }

    private List<NotificationReaction> retrieveNotificationReactions(
            List<Notification> notifications) {
        return notificationReactionRepository.findByUserIdAndNotificationIn(
                SecurityUtils.getCurrentUserId(), notifications);
    }

    private void validateDeletePermission(Notification notification) {
        if (!SecurityUtils.getCurrentUserId().equals(notification.getSendUser().getId())) {
            throw NotificationForbiddenException.EXCEPTION;
        }
    }

    private Notification queryNotificationById(Long notificationId) {
        return notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> NotificationNotFoundException.EXCEPTION);
    }
}
