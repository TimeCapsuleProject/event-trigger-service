package br.com.timecapsuleproject.timeCapsuleTriggerService.scheduling;

import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.TimeCapsuleRepository;
import br.com.timecapsuleproject.timeCapsuleTriggerService.services.TimeCapsuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class ExpiryScheduler {

    @Autowired
    private TimeCapsuleService timeCapsuleService;

    @Autowired
    private TimeCapsuleRepository timeCapsuleRepository;

    private static final int MAX_ATTEMPTS = 3;
    private static final int RETRY_MAX_ATTEMPTS = 10;

    @Scheduled(cron = "${app.scheduler.cron.daily:0 0 0 * * *}")
    public void runDailyExpiryJob() {
        LocalDate today = LocalDate.now();

        processExpiredCapsules(() ->
            timeCapsuleRepository.findFirstByOpeningDateLessThanEqualAndRetryAttemptsLessThanOrderByOpeningDateAsc(today, MAX_ATTEMPTS)
        );
    }

    @Scheduled(cron = "${app.retry.cron:0 2 * * * *}")
    public void runRetry() {
        processExpiredCapsules(() ->
            timeCapsuleRepository.findFirstByRetryAttemptsGreaterThanEqualOrderByRetryAttemptsAsc(MAX_ATTEMPTS)
        );
    }

    private void processExpiredCapsules(Supplier<Optional<TimeCapsuleEntity>> supplier) {
        while (true) {
            Optional<TimeCapsuleEntity> maybe = supplier.get();
            if (maybe.isEmpty() || maybe.get().getRetryAttempts() >= RETRY_MAX_ATTEMPTS) {
                break;
            }

            TimeCapsuleEntity capsule = maybe.get();
            try {
                timeCapsuleService.processSingleCapsule(capsule);
            } catch (Exception e) {
                capsule.setRetryAttempts(capsule.getRetryAttempts() + 1);
                timeCapsuleRepository.save(capsule);
            }
        }
    }

}

