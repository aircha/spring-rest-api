package me.aircha.restapi.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.aircha.restapi.accounts.Account;
import me.aircha.restapi.accounts.AccountSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
class Event {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;
    @ManyToOne
    @JsonSerialize(using = AccountSerializer.class)
    private Account manager;

    void update() {
        this.free = this.basePrice == 0 && this.maxPrice == 0;
        this.offline = this.location != null && !this.location.isBlank();
    }
}
