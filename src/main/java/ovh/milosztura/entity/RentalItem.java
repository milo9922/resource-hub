package ovh.milosztura.entity;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.RentalItemType;

import java.util.List;

@Entity
@Table
public class RentalItem extends PanacheEntity {

    public String name;
    @Enumerated(EnumType.STRING)
    public RentalItemType type;
    @Enumerated(EnumType.STRING)
    public RentalItemStatus status;

    public static List<RentalItem> findByType(RentalItemType type) {
        return list("type", type);
    }

    public static List<RentalItem> findByStatus(RentalItemStatus status) {
        return list("status", status);
    }
}
