package ovh.milosztura.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ovh.milosztura.util.Status;
import ovh.milosztura.util.Type;

public record AddRentalItemRequest(
        @NotNull Long rentalItemId,
        @NotBlank String name,
        @NotNull Type type,
        @NotNull Status status
) {
}
