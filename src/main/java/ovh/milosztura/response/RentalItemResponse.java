package ovh.milosztura.response;

import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.RentalItemType;

public record RentalItemResponse(Long id, String name, RentalItemType type, RentalItemStatus status) {
}
