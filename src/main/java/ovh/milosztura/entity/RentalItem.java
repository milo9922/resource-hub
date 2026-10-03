package ovh.milosztura.entity;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import ovh.milosztura.util.Status;
import ovh.milosztura.util.Type;

import java.util.List;

@Entity
@Table
public class RentalItem extends PanacheEntity {

    public String name;
    @Enumerated(EnumType.STRING)
    public Type type;
    @Enumerated(EnumType.STRING)
    public Status status;

    public static List<RentalItem> findByType(Type type) {
        return list("type", type);
    }

    public static List<RentalItem> findByStatus(Status status) {
        return list("status", status);
    }
}
