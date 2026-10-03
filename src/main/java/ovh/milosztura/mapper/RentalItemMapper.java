package ovh.milosztura.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ovh.milosztura.entity.RentalItem;
import ovh.milosztura.request.AddRentalItemRequest;
import ovh.milosztura.response.RentalItemResponse;

@Mapper(componentModel = "jakarta-cdi")
public interface RentalItemMapper {

    RentalItemResponse toResponse(RentalItem rentalItem);

    @Mapping(target = "id", ignore = true)
    RentalItem toEntity(AddRentalItemRequest rentalItemRequest);


}
