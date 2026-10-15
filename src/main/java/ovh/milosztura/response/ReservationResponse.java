package ovh.milosztura.response;

import ovh.milosztura.util.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long rentalItemId,
        ReservationStatus status,
        LocalDateTime creationTime,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String reservedBy
) {
}
