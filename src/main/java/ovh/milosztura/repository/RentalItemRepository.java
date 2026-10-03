package ovh.milosztura.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ovh.milosztura.entity.RentalItem;
import ovh.milosztura.util.Status;
import ovh.milosztura.util.Type;

import java.util.List;

@ApplicationScoped
public class RentalItemRepository implements PanacheRepository<RentalItem> {
    public List<RentalItem> findByType(Type type) {
        return list("type", type);
    }

    public List<RentalItem> findByStatus(Status status) {
        return list("status", status);
    }

}
