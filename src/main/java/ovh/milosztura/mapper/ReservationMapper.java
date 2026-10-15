package ovh.milosztura.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ovh.milosztura.entity.Reservation;
import ovh.milosztura.response.ReservationResponse;

@Mapper(componentModel = "jakarta-cdi")
public interface ReservationMapper {

    @Mapping(target = "rentalItemId", source = "rentalItem.id")
    ReservationResponse toResponse(Reservation reservation);
}
