package br.com.timecapsuleproject.timeCapsuleTriggerService.scheduling;

import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.TimeCapsuleRepository;
import br.com.timecapsuleproject.timeCapsuleTriggerService.services.TimeCapsuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

@Component
@Slf4j
public class ExpiryScheduler {

    @Autowired
    private TimeCapsuleService timeCapsuleService;

    @Autowired
    private TimeCapsuleRepository timeCapsuleRepository;

    private static final int MAX_ATTEMPTS = 3;
    private static final int RETRY_MAX_ATTEMPTS = 10;

    @Scheduled(cron = "${app.scheduler.cron.daily:0 0 0 * * *}")
    public void runDailyExpiryJob() {
        log.info("Starting daily expiry job...");
        LocalDate today = LocalDate.now();

        processExpiredCapsules(() ->
            timeCapsuleRepository.findFirstByOpeningDateLessThanEqualAndRetryAttemptsLessThanOrderByOpeningDateAsc(today, MAX_ATTEMPTS)
        );
        log.info("Daily expiry job finished.");
    }

    @Scheduled(cron = "${app.retry.cron:0 2 * * * *}")
    public void runRetry() {
        log.info("Starting retry job for capsules with failed attempts...");
        processExpiredCapsules(() ->
            timeCapsuleRepository.findFirstByRetryAttemptsGreaterThanEqualOrderByRetryAttemptsAsc(MAX_ATTEMPTS)
        );
        log.info("Retry job finished.");
    }

    private void processExpiredCapsules(Supplier<Optional<TimeCapsuleEntity>> supplier) {
        int count = 0;
        while (true) {
            Optional<TimeCapsuleEntity> maybe = supplier.get();
            if (maybe.isEmpty() || maybe.get().getRetryAttempts() >= RETRY_MAX_ATTEMPTS) {
                break;
            }

            TimeCapsuleEntity capsule = maybe.get();
            log.info("Processing capsule ID: {}, Name: {}, Attempt: {}", 
                capsule.getId(), capsule.getTimeCapsuleName(), capsule.getRetryAttempts());
            
            try {
                timeCapsuleService.processSingleCapsule(capsule);
                log.info("Successfully processed capsule ID: {}", capsule.getId());
                count++;
            } catch (Exception e) {
                capsule.setRetryAttempts(capsule.getRetryAttempts() + 1);
                timeCapsuleRepository.save(capsule);
                log.error("Error processing capsule ID: {}. Current attempt: {}. Error: {}", 
                    capsule.getId(), capsule.getRetryAttempts(), e.getMessage());
            }
        }
        log.info("Total capsules processed in this batch: {}", count);
    }

}
