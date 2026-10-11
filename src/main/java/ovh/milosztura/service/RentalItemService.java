package ovh.milosztura.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import ovh.milosztura.mapper.RentalItemMapper;
import ovh.milosztura.repository.RentalItemRepository;
import ovh.milosztura.request.AddRentalItemRequest;
import ovh.milosztura.response.RentalItemResponse;

import java.util.List;

@ApplicationScoped
public class RentalItemService {

    @Inject
    RentalItemRepository rentalItemRepository;

    @Inject
    RentalItemMapper rentalItemMapper;
    public static final String RENTAL_ITEM_NOT_FOUND_MESSAGE = "Rental item not found";
    public static final String RENTAL_ITEM_ID_NULL_MESSAGE = "Rental item ID is null!";


    public List<RentalItemResponse> findAll() {
        return rentalItemRepository.findAll().stream()
                .map(rentalItemMapper::toResponse)
                .toList();
    }

    public RentalItemResponse findById(Long id) {
        return rentalItemMapper.toResponse(rentalItemRepository.findOptionalById(id)
                .orElseThrow(() -> new NotFoundException("Rental item " + id + " not found")));
    }

    @Transactional
    public RentalItemResponse create(AddRentalItemRequest request) {
        var rentalItem = rentalItemMapper.toEntity(request);
        rentalItemRepository.persist(rentalItem);
        return rentalItemMapper.toResponse(rentalItem);
    }

    @Transactional
    public RentalItemResponse update(Long id, AddRentalItemRequest request) {
        var rentalItem = rentalItemRepository.findOptionalById(id)
                .orElseThrow(() -> new NotFoundException(RENTAL_ITEM_NOT_FOUND_MESSAGE));
        rentalItem.name = request.name();
        rentalItem.type = request.type();
        rentalItem.status = request.status();
        return rentalItemMapper.toResponse(rentalItem);
    }

    @Transactional
    public void delete(Long id) {
        var rentalItem = rentalItemRepository.findOptionalById(id)
                .orElseThrow(() -> new NotFoundException(RENTAL_ITEM_NOT_FOUND_MESSAGE));
        rentalItemRepository.delete(rentalItem);
    }

}
