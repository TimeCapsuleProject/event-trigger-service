package br.com.timecapsuleproject.timeCapsuleTriggerService.consumer;

import br.com.timecapsuleproject.timeCapsuleService.dto.NewMessageNotification;
import br.com.timecapsuleproject.timeCapsuleService.dto.UserLeaveNotification;
import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.services.TimeCapsuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import br.com.timecapsuleproject.timeCapsuleService.dto.CapsuleCreationNotification;

import static br.com.timecapsuleproject.timeCapsuleService.constants.RabbitMqConstants.*;

@Component
@Slf4j
public class EventConsumer {

    @Autowired
    private TimeCapsuleService timeCapsuleService;

    @RabbitListener(queues = QUEUE_CAPSULE_CREATION_TRIGGER)
    public void consumeEventCreationNotification(CapsuleCreationNotification capsuleCreationNotification) {
        log.info("Received capsule creation notification: {}", capsuleCreationNotification.getTimeCapsuleName());

        TimeCapsuleEntity timeCapsuleEntity = timeCapsuleService.createTimeCapsule(capsuleCreationNotification);
        timeCapsuleService.addUserInTimeCapsule(
            capsuleCreationNotification.getOwnerName(),
            capsuleCreationNotification.getOwnerEmail(),
            timeCapsuleEntity
        );
    }

    @RabbitListener(queues = QUEUE_NEW_MESSAGE_TRIGGER)
    public void consumeMessageSentNotification(NewMessageNotification newMessageNotification) {
        log.info("Received new message notification for capsule ID: {}", newMessageNotification.getTimeCapsuleId());

        TimeCapsuleEntity timeCapsuleEntity = timeCapsuleService.findTimeCapsuleById(newMessageNotification.getTimeCapsuleId());

        if(timeCapsuleService.userAlreadyInTimeCapsule(newMessageNotification.getSenderEmail(), timeCapsuleEntity)) {
            log.info("User {} already in time capsule {}, skipping adding user.", newMessageNotification.getSenderEmail(), timeCapsuleEntity.getId());
            return;
        }

        timeCapsuleService.addUserInTimeCapsule(
            newMessageNotification.getSenderUsername(),
            newMessageNotification.getSenderEmail(),
            timeCapsuleEntity
        );
    }

    @RabbitListener(queues = QUEUE_USER_LEAVE_TRIGGER)
    public void consumeUserLeaveNotification(UserLeaveNotification userLeaveNotification) {
        log.info("Received user leave notification: {} from capsule: {}", userLeaveNotification.getUserEmail(), userLeaveNotification.getTimeCapsuleId());
        timeCapsuleService.removeUserInTimeCapsule(userLeaveNotification);
    }
}
