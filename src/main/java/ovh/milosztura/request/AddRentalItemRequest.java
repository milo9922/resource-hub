package ovh.milosztura.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.Type;

public record AddRentalItemRequest(
        @NotBlank String name,
        @NotNull Type type,
        @NotNull RentalItemStatus status
) {
}
