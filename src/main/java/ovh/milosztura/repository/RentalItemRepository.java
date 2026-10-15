package ovh.milosztura.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ovh.milosztura.entity.RentalItem;
import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.RentalItemType;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RentalItemRepository implements PanacheRepository<RentalItem> {
    public List<RentalItem> findByType(RentalItemType type) {
        return list("type", type);
    }

    public List<RentalItem> findByStatus(RentalItemStatus status) {
        return list("status", status);
    }

    public Optional<RentalItem> findOptionalById(Long id) {
        return findByIdOptional(id);
    }
}
