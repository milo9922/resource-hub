package ovh.milosztura.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ovh.milosztura.mapper.RentalItemMapper;
import ovh.milosztura.repository.RentalItemRepository;
import ovh.milosztura.response.RentalItemResponse;

import java.util.List;

@ApplicationScoped
public class RentalItemService {

    @Inject
    RentalItemRepository rentalItemRepository;

    @Inject
    RentalItemMapper rentalItemMapper;

    public List<RentalItemResponse> findAll() {
        return rentalItemRepository.findAll().stream()
                .map(rentalItemMapper::toResponse)
                .toList();
    }

}
