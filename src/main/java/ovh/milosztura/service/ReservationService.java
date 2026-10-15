package ovh.milosztura.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import ovh.milosztura.entity.RentalItem;
import ovh.milosztura.entity.Reservation;
import ovh.milosztura.mapper.ReservationMapper;
import ovh.milosztura.repository.RentalItemRepository;
import ovh.milosztura.repository.ReservationRepository;
import ovh.milosztura.request.CreateReservationRequest;
import ovh.milosztura.request.UpdateReservationRequest;
import ovh.milosztura.response.ReservationResponse;
import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@ApplicationScoped
public class ReservationService {

    private static final String RESERVATION_NOT_FOUND_MESSAGE = "Reservation not found";
    private static final String RENTAL_ITEM_RESERVED_MESSAGE = "Rental item already reserved!";
    private static final String END_TIME_BEFORE_START_TIME_ERROR_MESSAGE = "Start time must be before end time";
    private static final String RESERVATION_ID_REQUIRED_MESSAGE = "Reservation id is required";
    private static final String RESERVATION_STATUS_REQUIRED_MESSAGE = "Reservation status is required";

    private final ReservationRepository reservationRepository;
    private final RentalItemRepository rentalItemRepository;
    private final ReservationMapper reservationMapper;

    @Inject
    public ReservationService(ReservationRepository reservationRepository,
                              RentalItemRepository rentalItemRepository,
                              ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.rentalItemRepository = rentalItemRepository;
        this.reservationMapper = reservationMapper;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    public ReservationResponse findById(Long id) {
        return reservationMapper.toResponse(getReservationById(id));
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest request) {
        validateTimeRange(request.startsAt(), request.endsAt());
        var rentalItemId = request.rentalItemId();
        var rentalItem = validateAndGetRentalItemIfAvailable(rentalItemId);
        Reservation reservation = new Reservation();
        reservation.rentalItem = rentalItem;
        reservation.creationTime = LocalDateTime.now();
        reservation.startsAt = request.startsAt();
        reservation.endsAt = request.endsAt();
        reservation.reservedBy = request.reservedBy();
        reservation.status = ReservationStatus.PENDING;
        reservationRepository.persist(reservation);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse update(Long id, UpdateReservationRequest request) {
        var reservation = getReservationById(id);

        if (request.rentalItemId() != null) {
            reservation.rentalItem = validateAndGetRentalItemIfAvailable(request.rentalItemId());
        }

        validateTimeRange(request.startsAt(), request.endsAt());
        reservation.startsAt = request.startsAt();
        reservation.endsAt = request.endsAt();

        reservation.reservedBy = request.reservedBy();
        reservation.status = ReservationStatus.PENDING;

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateStatus(Long id, ReservationStatus status) {
        var reservation = getReservationById(id);
        if (status == null) {
            throw new BadRequestException(RESERVATION_STATUS_REQUIRED_MESSAGE);
        }
        reservation.status = status;
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public void delete(Long id) {
        var reservation = getReservationById(id);
        reservationRepository.delete(reservation);
    }

    private Reservation getReservationById(Long id) {
        if (id == null) {
            throw new BadRequestException(RESERVATION_ID_REQUIRED_MESSAGE);
        }
        var reservationOptional = reservationRepository.findByIdOptional(id);
        return reservationOptional.orElseThrow(() -> new NotFoundException(RESERVATION_NOT_FOUND_MESSAGE));
    }

    private void validateTimeRange(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (!startsAt.isBefore(endsAt)) {
            throw new BadRequestException(END_TIME_BEFORE_START_TIME_ERROR_MESSAGE);
        }
    }

    private RentalItem validateAndGetRentalItemIfAvailable(Long rentalItemId) {
        if (rentalItemId == null) {
            throw new BadRequestException(RentalItemService.RENTAL_ITEM_ID_NULL_MESSAGE);
        }
        var rentalItem = rentalItemRepository.findByIdOptional(rentalItemId);
        if (rentalItem.isEmpty()) {
            throw new BadRequestException(RentalItemService.RENTAL_ITEM_NOT_FOUND_MESSAGE);
        }
        if (!rentalItem.get().status.equals(RentalItemStatus.AVAILABLE)) {
            throw new BadRequestException(RENTAL_ITEM_RESERVED_MESSAGE);
        }
        return rentalItem.get();
    }
}
