package br.com.timecapsuleproject.timeCapsuleTriggerService.services;

import br.com.timecapsuleproject.timeCapsuleService.dto.CapsuleCreationNotification;
import br.com.timecapsuleproject.timeCapsuleService.dto.CapsuleOpenedNotification;
import br.com.timecapsuleproject.timeCapsuleService.dto.UserLeaveNotification;
import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.UsersTimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.notification.NotificationProducer;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.TimeCapsuleRepository;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.UserTimeCapsuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TimeCapsuleService {

    @Autowired
    private TimeCapsuleRepository timeCapsuleRepository;

    @Autowired
    private UserTimeCapsuleRepository userTimeCapsuleRepository;

    @Autowired
    private NotificationProducer notificationProducer;

    @Transactional
    public TimeCapsuleEntity createTimeCapsule(CapsuleCreationNotification capsuleCreationNotification) {

        TimeCapsuleEntity entity = new TimeCapsuleEntity(
            capsuleCreationNotification.getTimeCapsuleId(),
            capsuleCreationNotification.getTimeCapsuleName(),
            capsuleCreationNotification.getOpeningDate()
        );

        return timeCapsuleRepository.save(entity);
    }

    @Transactional
    public void addUserInTimeCapsule(String userName, String userEmail, TimeCapsuleEntity timeCapsuleEntity) {

        UsersTimeCapsuleEntity userEntity = new UsersTimeCapsuleEntity(
            timeCapsuleEntity,
            userName,
            userEmail
        );

        userTimeCapsuleRepository.save(userEntity);
    }

    @Transactional
    public void removeUserInTimeCapsule(UserLeaveNotification userLeaveNotification) {

        TimeCapsuleEntity timeCapsuleEntity = findTimeCapsuleById(userLeaveNotification.getTimeCapsuleId());

        userTimeCapsuleRepository.deleteByUserEmailAndTimeCapsule(userLeaveNotification.getUserEmail(), timeCapsuleEntity);
    }

    public TimeCapsuleEntity findTimeCapsuleById(String timeCapsuleId) {
        return timeCapsuleRepository.findById(timeCapsuleId)
                .orElseThrow(() -> new RuntimeException("Time capsule not found"));
    }

    public boolean userAlreadyInTimeCapsule(String userEmail, TimeCapsuleEntity timeCapsuleEntity) {
        return userTimeCapsuleRepository.findByUserEmailAndTimeCapsule(userEmail, timeCapsuleEntity).isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleCapsule(TimeCapsuleEntity capsule) {
        List<UsersTimeCapsuleEntity> users = userTimeCapsuleRepository.findByTimeCapsuleId(capsule.getId());

        for (UsersTimeCapsuleEntity user : users) {

            CapsuleOpenedNotification capsuleOpenedNotification = new CapsuleOpenedNotification();
            capsuleOpenedNotification.setTimeCapsuleId(capsule.getId());
            capsuleOpenedNotification.setTimeCapsuleName(capsule.getTimeCapsuleName());
            capsuleOpenedNotification.setOpeningDate(capsule.getOpeningDate());
            capsuleOpenedNotification.setUserEmail(user.getUserEmail());
            capsuleOpenedNotification.setUserName(user.getUserName());

            notificationProducer.sendNotificationForCapsule(capsuleOpenedNotification);
        }

        // After successful send, delete users and capsule in a transaction
        deleteCapsuleAndUsers(capsule.getId());
    }

    protected void deleteCapsuleAndUsers(String capsuleId) {
        userTimeCapsuleRepository.deleteByTimeCapsuleId(capsuleId);
        timeCapsuleRepository.deleteById(capsuleId);
    }

}
