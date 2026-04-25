package br.com.timecapsuleproject.timeCapsuleTriggerService.scheduling;

import br.com.timecapsuleproject.timeCapsuleTriggerService.domain.TimeCapsuleEntity;
import br.com.timecapsuleproject.timeCapsuleTriggerService.repositories.TimeCapsuleRepository;
import br.com.timecapsuleproject.timeCapsuleTriggerService.services.TimeCapsuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExpirySchedulerTest {

    private ExpiryScheduler scheduler;
    private TimeCapsuleRepository repository;
    private TimeCapsuleService service;

    @BeforeEach
    void setUp() throws Exception {
        scheduler = new ExpiryScheduler();
        repository = Mockito.mock(TimeCapsuleRepository.class);
        service = Mockito.mock(TimeCapsuleService.class);

        // inject mocks into private fields
        setField(scheduler, "timeCapsuleRepository", repository);
        setField(scheduler, "timeCapsuleService", service);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void dailySchedulerProcessesOnlyUpToThreeAttempts() {
        // Given: repository will return one capsule (retryAttempts < 3) then empty
        TimeCapsuleEntity c1 = new TimeCapsuleEntity("daily-1", "Processable", LocalDate.now());
        c1.setRetryAttempts(2);

        when(repository.findFirstByOpeningDateLessThanEqualAndRetryAttemptsLessThanOrderByOpeningDateAsc(any(LocalDate.class), eq(3)))
                .thenReturn(Optional.of(c1))
                .thenReturn(Optional.empty());

        // When
        scheduler.runDailyExpiryJob();

        // Then: service.processSingleCapsule called once with c1
        ArgumentCaptor<TimeCapsuleEntity> captor = ArgumentCaptor.forClass(TimeCapsuleEntity.class);
        verify(service, times(1)).processSingleCapsule(captor.capture());
        TimeCapsuleEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo("daily-1");
    }

    @Test
    void retrySchedulerProcessesFromThreeUpToTenAttemptsAndIncrementsOnFailure() {
        // Given: repository will return capsules with retryAttempts 3..9 sequentially
        // create capsule instances representing each state (3..9)
        TimeCapsuleEntity[] states = new TimeCapsuleEntity[8]; // indexes 0->attempt3 ... 6->attempt9, 7->attempt10 (stop)
        for (int i = 0; i < 8; i++) {
            int attempt = 3 + i; // 3..10
            states[i] = new TimeCapsuleEntity("retry-1", "RetryCapsule", LocalDate.now().minusDays(10));
            states[i].setRetryAttempts(attempt);
        }

        // repository should return attempts 3..9 (indices 0..6) when called; when it returns attempt 10, loop breaks
        when(repository.findFirstByRetryAttemptsGreaterThanEqualOrderByRetryAttemptsAsc(eq(3)))
                .thenReturn(Optional.of(states[0]))
                .thenReturn(Optional.of(states[1]))
                .thenReturn(Optional.of(states[2]))
                .thenReturn(Optional.of(states[3]))
                .thenReturn(Optional.of(states[4]))
                .thenReturn(Optional.of(states[5]))
                .thenReturn(Optional.of(states[6]))
                .thenReturn(Optional.of(states[7]));

        // Make service always throw to trigger retry increment
        doThrow(new RuntimeException("fail")).when(service).processSingleCapsule(any());

        // When
        scheduler.runRetry();

        // Then: processSingleCapsule should be called exactly 7 times (for attempts 3..9)
        verify(service, times(7)).processSingleCapsule(any());

        // And repository.save should have been called 7 times to persist incremented attempts
        verify(repository, atLeast(7)).save(any(TimeCapsuleEntity.class));
    }
}
