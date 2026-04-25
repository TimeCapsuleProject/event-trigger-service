package br.com.timecapsuleproject.timeCapsuleTriggerService.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_time_capsule")
@Entity(name = "user_time_capsule")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UsersTimeCapsuleEntity {

    public UsersTimeCapsuleEntity(TimeCapsuleEntity timeCapsule, String emailUser) {
        this.timeCapsule = timeCapsule;
        this.userEmail = emailUser;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_time_capsule", referencedColumnName = "id", nullable = false)
    private TimeCapsuleEntity timeCapsule;

    private String userName;

    private String userEmail;

}
