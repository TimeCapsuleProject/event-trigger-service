package br.com.timecapsuleproject.timeCapsuleTriggerService.repositories;

import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.UsersTimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTimeCapsuleRepository extends JpaRepository<UsersTimeCapsuleEntity, String> {

    Optional<UsersTimeCapsuleEntity> findByUserEmailAndTimeCapsule(String userEmail, TimeCapsuleEntity timeCapsule);

    void deleteByUserEmailAndTimeCapsule(String userEmail, TimeCapsuleEntity timeCapsule);

    List<UsersTimeCapsuleEntity> findByTimeCapsuleId(String timeCapsuleId);

    void deleteByTimeCapsuleId(String timeCapsuleId);

}
