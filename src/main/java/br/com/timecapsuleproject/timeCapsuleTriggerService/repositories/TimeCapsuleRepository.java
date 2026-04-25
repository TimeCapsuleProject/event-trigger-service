package br.com.timecapsuleproject.timeCapsuleTriggerService.repositories;

import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TimeCapsuleRepository extends JpaRepository<TimeCapsuleEntity, String> {

    Optional<TimeCapsuleEntity> findFirstByOpeningDateLessThanEqualAndRetryAttemptsLessThanOrderByOpeningDateAsc(LocalDate date, int maxRetryAttempts);

    Optional<TimeCapsuleEntity> findFirstByRetryAttemptsGreaterThanEqualOrderByRetryAttemptsAsc(int minRetryAttempts);

}
