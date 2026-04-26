package br.com.timecapsuleproject.timeCapsuleTriggerService.scheduling;

import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.TimeCapsuleRepository;
import br.com.timecapsuleproject.timeCapsuleTriggerService.services.TimeCapsuleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpirySchedulerTest {

    @Mock
    private TimeCapsuleRepository timeCapsuleRepository;

    @Mock
    private TimeCapsuleService timeCapsuleService;

    @InjectMocks
    private ExpiryScheduler scheduler;

    @Test
    void dailySchedulerProcessesOnlyUpToThreeAttempts() {
        // Given: repository will return one capsule (retryAttempts < 3) then empty
        TimeCapsuleEntity c1 = new TimeCapsuleEntity("daily-1", "Processable", LocalDate.now());
        c1.setRetryAttempts(2);

        when(timeCapsuleRepository.findFirstByOpeningDateLessThanEqualAndRetryAttemptsLessThanOrderByOpeningDateAsc(any(LocalDate.class), eq(3)))
                .thenReturn(Optional.of(c1))
                .thenReturn(Optional.empty());

        // When
        scheduler.runDailyExpiryJob();

        // Then: service.processSingleCapsule called once with c1
        ArgumentCaptor<TimeCapsuleEntity> captor = ArgumentCaptor.forClass(TimeCapsuleEntity.class);
        verify(timeCapsuleService, times(1)).processSingleCapsule(captor.capture());
        TimeCapsuleEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo("daily-1");
    }

    @Test
    void retrySchedulerProcessesFromThreeUpToTenAttemptsAndIncrementsOnFailure() {
        // Given: repository will return capsules with retryAttempts 3..9 sequentially
        TimeCapsuleEntity[] states = new TimeCapsuleEntity[8];
        for (int i = 0; i < 8; i++) {
            int attempt = 3 + i; // 3..10
            states[i] = new TimeCapsuleEntity("retry-1", "RetryCapsule", LocalDate.now().minusDays(10));
            states[i].setRetryAttempts(attempt);
        }

        when(timeCapsuleRepository.findFirstByRetryAttemptsGreaterThanEqualOrderByRetryAttemptsAsc(eq(3)))
                .thenReturn(Optional.of(states[0]))
                .thenReturn(Optional.of(states[1]))
                .thenReturn(Optional.of(states[2]))
                .thenReturn(Optional.of(states[3]))
                .thenReturn(Optional.of(states[4]))
                .thenReturn(Optional.of(states[5]))
                .thenReturn(Optional.of(states[6]))
                .thenReturn(Optional.of(states[7]));

        doThrow(new RuntimeException("fail")).when(timeCapsuleService).processSingleCapsule(any());

        // When
        scheduler.runRetry();

        // Then: processSingleCapsule should be called exactly 7 times (for attempts 3..9)
        verify(timeCapsuleService, times(7)).processSingleCapsule(any());

        // And repository.save should have been called 7 times to persist incremented attempts
        verify(timeCapsuleRepository, atLeast(7)).save(any(TimeCapsuleEntity.class));
    }
}
