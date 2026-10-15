package ovh.milosztura.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.RentalItemType;

public record AddRentalItemRequest(
        @NotBlank String name,
        @NotNull RentalItemType type,
        @NotNull RentalItemStatus status
) {
}
