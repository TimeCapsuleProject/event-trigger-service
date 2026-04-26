package br.com.timecapsuleproject.timeCapsuleTriggerService.integration;

import br.com.timecapsuleproject.timeCapsuleService.dto.CapsuleCreationNotification;
import br.com.timecapsuleproject.timeCapsuleService.dto.NewMessageNotification;
import br.com.timecapsuleproject.timeCapsuleService.dto.UserLeaveNotification;
import br.com.timecapsuleproject.timeCapsuleTriggerService.consumer.EventConsumer;
import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.UsersTimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.TimeCapsuleRepository;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.UserTimeCapsuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class EventConsumerIntegrationTest {

    @Autowired
    private EventConsumer eventConsumer;

    @Autowired
    private TimeCapsuleRepository timeCapsuleRepository;

    @Autowired
    private UserTimeCapsuleRepository userTimeCapsuleRepository;

    @Test
    void testCapsuleCreationFlow() {
        // Given
        String timeCapsuleId = "test-capsule-1";
        String timeCapsuleName = "Test Capsule";
        String ownerName = "owner";
        String ownerEmail = "owner@example.com";
        LocalDate openingDate = LocalDate.now().plusDays(1);

        CapsuleCreationNotification notification = new CapsuleCreationNotification();
        notification.setTimeCapsuleId(timeCapsuleId);
        notification.setTimeCapsuleName(timeCapsuleName);
        notification.setOwnerName(ownerName);
        notification.setOwnerEmail(ownerEmail);
        notification.setOpeningDate(openingDate);

        // When
        eventConsumer.consumeEventCreationNotification(notification);

        // Then
        await().until(() -> timeCapsuleRepository.findById(timeCapsuleId).isPresent());

        Optional<TimeCapsuleEntity> capsuleOpt = timeCapsuleRepository.findById(timeCapsuleId);
        assertThat(capsuleOpt).isPresent();
        TimeCapsuleEntity capsule = capsuleOpt.get();
        assertThat(capsule.getTimeCapsuleName()).isEqualTo(timeCapsuleName);
        assertThat(capsule.getOpeningDate()).isEqualTo(openingDate);

        Optional<UsersTimeCapsuleEntity> userOpt = userTimeCapsuleRepository.findByUserEmailAndTimeCapsule(ownerEmail, capsule);
        assertThat(userOpt).isPresent();
    }

    @Test
    void testNewMessageAddsUserIfNotPresent() {
        // First, create a capsule
        String timeCapsuleId = "test-capsule-2";
        String timeCapsuleName = "Test Capsule 2";
        String ownerName = "owner2";
        String ownerEmail = "owner2@example.com";
        LocalDate openingDate = LocalDate.now().plusDays(1);

        CapsuleCreationNotification creationNotification = new CapsuleCreationNotification();
        creationNotification.setTimeCapsuleId(timeCapsuleId);
        creationNotification.setTimeCapsuleName(timeCapsuleName);
        creationNotification.setOwnerName(ownerName);
        creationNotification.setOwnerEmail(ownerEmail);
        creationNotification.setOpeningDate(openingDate);

        eventConsumer.consumeEventCreationNotification(creationNotification);
        await().until(() -> timeCapsuleRepository.findById(timeCapsuleId).isPresent());

        // Now, send new message from a new user
        String senderUsername = "sender";
        String senderEmail = "sender@example.com";

        NewMessageNotification messageNotification = new NewMessageNotification();
        messageNotification.setTimeCapsuleId(timeCapsuleId);
        messageNotification.setSenderUsername(senderUsername);
        messageNotification.setSenderEmail(senderEmail);

        // When
        eventConsumer.consumeMessageSentNotification(messageNotification);

        // Then
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(timeCapsuleId).get();
        await().until(() -> userTimeCapsuleRepository.findByUserEmailAndTimeCapsule(senderEmail, capsule).isPresent());

        Optional<UsersTimeCapsuleEntity> userOpt = userTimeCapsuleRepository.findByUserEmailAndTimeCapsule(senderEmail, capsule);
        assertThat(userOpt).isPresent();
    }

    @Test
    void testNewMessageDoesNotAddUserIfAlreadyPresent() {
        // First, create a capsule and add a user
        String timeCapsuleId = "test-capsule-3";
        String timeCapsuleName = "Test Capsule 3";
        String ownerName = "owner3";
        String ownerEmail = "owner3@example.com";
        LocalDate openingDate = LocalDate.now().plusDays(1);

        CapsuleCreationNotification creationNotification = new CapsuleCreationNotification();
        creationNotification.setTimeCapsuleId(timeCapsuleId);
        creationNotification.setTimeCapsuleName(timeCapsuleName);
        creationNotification.setOwnerName(ownerName);
        creationNotification.setOwnerEmail(ownerEmail);
        creationNotification.setOpeningDate(openingDate);

        eventConsumer.consumeEventCreationNotification(creationNotification);
        await().until(() -> timeCapsuleRepository.findById(timeCapsuleId).isPresent());

        // Send new message from the same user (owner)
        NewMessageNotification messageNotification = new NewMessageNotification();
        messageNotification.setTimeCapsuleId(timeCapsuleId);
        messageNotification.setSenderEmail(ownerEmail);

        // When
        eventConsumer.consumeMessageSentNotification(messageNotification);

        // Then - user should still be there, no duplicate
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(timeCapsuleId).get();
        long userCount = userTimeCapsuleRepository.findAll().stream()
                .filter(u -> u.getTimeCapsule().getId().equals(timeCapsuleId) && u.getUserEmail().equals(ownerEmail))
                .count();
        assertThat(userCount).isEqualTo(1);
    }

    @Test
    void testUserLeaveRemovesUser() {
        // First, create a capsule and add a user
        String timeCapsuleId = "test-capsule-4";
        String timeCapsuleName = "Test Capsule 4";
        String ownerName = "owner4";
        String ownerEmail = "owner4@example.com";
        LocalDate openingDate = LocalDate.now().plusDays(1);

        CapsuleCreationNotification creationNotification = new CapsuleCreationNotification();
        creationNotification.setTimeCapsuleId(timeCapsuleId);
        creationNotification.setTimeCapsuleName(timeCapsuleName);
        creationNotification.setOwnerName(ownerName);
        creationNotification.setOwnerEmail(ownerEmail);
        creationNotification.setOpeningDate(openingDate);

        eventConsumer.consumeEventCreationNotification(creationNotification);
        await().until(() -> timeCapsuleRepository.findById(timeCapsuleId).isPresent());

        // Now, send user leave
        UserLeaveNotification leaveNotification = new UserLeaveNotification();
        leaveNotification.setTimeCapsuleId(timeCapsuleId);
        leaveNotification.setUserEmail(ownerEmail);

        // When
        eventConsumer.consumeUserLeaveNotification(leaveNotification);

        // Then
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(timeCapsuleId).get();
        await().until(() -> userTimeCapsuleRepository.findByUserEmailAndTimeCapsule(ownerEmail, capsule).isEmpty());

        Optional<UsersTimeCapsuleEntity> userOpt = userTimeCapsuleRepository.findByUserEmailAndTimeCapsule(ownerEmail, capsule);
        assertThat(userOpt).isEmpty();
    }
}
