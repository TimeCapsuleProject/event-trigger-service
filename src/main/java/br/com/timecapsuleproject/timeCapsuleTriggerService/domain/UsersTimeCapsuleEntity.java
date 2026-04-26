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

    public UsersTimeCapsuleEntity(TimeCapsuleEntity timeCapsule, String userName, String userEmail) {
        this.timeCapsule = timeCapsule;
        this.userName = userName;
        this.userEmail = userEmail;
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
