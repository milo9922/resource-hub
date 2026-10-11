package ovh.milosztura.response;

import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.Type;

public record RentalItemResponse(Long id, String name, Type type, RentalItemStatus status) {
}
