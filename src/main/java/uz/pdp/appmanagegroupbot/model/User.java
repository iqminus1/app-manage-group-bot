package uz.pdp.appmanagegroupbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import uz.pdp.appmanagegroupbot.enums.Lang;
import uz.pdp.appmanagegroupbot.enums.State;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity(name = "users")
@Builder
public class User {
    @Id
    private Long id;

    private String contactNumber;

    private int admin;

    private boolean subscribed;

    private LocalDateTime subscriptionEndTime;

    private Lang lang;

    @Enumerated(EnumType.STRING)
    private State state;

}
