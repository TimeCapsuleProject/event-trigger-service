package br.com.timecapsuleproject.timeCapsuleTriggerService.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "time_capsule", indexes = {
        @Index(name = "idx_time_capsule_opening_date", columnList = "opening_date"),
        @Index(name = "idx_time_capsule_retry_attempts", columnList = "retry_attempts")
})
@Entity(name = "time_capsule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TimeCapsuleEntity {

    public TimeCapsuleEntity(String id, String timeCapsuleName, LocalDate openingDate) {
        this.id = id;
        this.timeCapsuleName = timeCapsuleName;
        this.openingDate = openingDate;
    }

    @Id
    private String id;

    private String timeCapsuleName;

    private LocalDate openingDate;

    @Column(name = "retry_attempts", nullable = false)
    private int retryAttempts = 0;

    @OneToMany(mappedBy = "timeCapsule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsersTimeCapsuleEntity> usersTimeCapsules = new ArrayList<>();

}
