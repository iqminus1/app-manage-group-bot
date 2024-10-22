package uz.pdp.appmanagegroupbot.model;

import jakarta.persistence.*;
import lombok.*;
import uz.pdp.appmanagegroupbot.enums.Status;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sendUserId;

    private String path;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime sentAt;

    private LocalDateTime changedStatus;

    private Long price;
}
