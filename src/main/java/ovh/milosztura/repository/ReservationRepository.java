package ovh.milosztura.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ovh.milosztura.entity.RentalItem;
import ovh.milosztura.entity.Reservation;
import ovh.milosztura.util.ReservationStatus;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> findByStatus(ReservationStatus status) {
        return list("status", status);
    }

    public Optional<Reservation> findOptionalById(Long id) {
        return findByIdOptional(id);
    }
}
