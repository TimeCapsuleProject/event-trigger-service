package br.com.timecapsuleproject.timeCapsuleTriggerService.notification;

import br.com.timecapsuleproject.timeCapsuleService.dto.CapsuleOpenedNotification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static br.com.timecapsuleproject.timeCapsuleService.constants.RabbitMqConstants.EXCHANGE;
import static br.com.timecapsuleproject.timeCapsuleService.constants.RabbitMqConstants.ROUTING_KEY_CAPSULE_OPENED;

@Component
public class NotificationProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendNotificationForCapsule(CapsuleOpenedNotification payload) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CAPSULE_OPENED, payload);
    }

}

