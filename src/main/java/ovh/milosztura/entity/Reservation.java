package ovh.milosztura.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ovh.milosztura.util.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
public class Reservation extends PanacheEntity {

    @ManyToOne
    public RentalItem rentalItem;

    @Enumerated(EnumType.STRING)
    public ReservationStatus status;

    @Column(name = "created_at")
    public LocalDateTime creationTime;

    @Column(name = "starts_at")
    public LocalDateTime startsAt;

    @Column(name = "ends_at")
    public LocalDateTime endsAt;

    public String reservedBy;

    public static List<Reservation> findByStatus(ReservationStatus status) {
        return list("status", status);
    }

}
