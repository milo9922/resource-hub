package ovh.milosztura.response;

import ovh.milosztura.util.Status;
import ovh.milosztura.util.Type;

public record RentalItemResponse(Long id, String name, Type type, Status status) {
}
