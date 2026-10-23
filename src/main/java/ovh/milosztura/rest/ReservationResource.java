package ovh.milosztura.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ovh.milosztura.request.CreateReservationRequest;
import ovh.milosztura.request.UpdateReservationRequest;
import ovh.milosztura.response.ReservationResponse;
import ovh.milosztura.service.ReservationService;
import ovh.milosztura.util.ReservationStatus;

import java.util.List;

@Path("/api/v1/reservation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationResource {

    @Inject
    ReservationService reservationService;

    @GET
    @Path("/all")
    public List<ReservationResponse> list() {
        return reservationService.findAll();
    }

    @GET
    @Path("/{id}")
    public ReservationResponse get(@PathParam("id") Long id) {
        return reservationService.findById(id);
    }

    @POST
    public Response create(@Valid CreateReservationRequest request) {
        var created = reservationService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public ReservationResponse update(@PathParam("id") Long id, @Valid UpdateReservationRequest request) {
        return reservationService.update(id, request);
    }

    @PATCH
    @Path("/{id}/{status}")
    public ReservationResponse updateStatus(@PathParam("id") Long id, @PathParam("status") ReservationStatus status) {
        return reservationService.updateStatus(id, status);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        reservationService.delete(id);
    }
}
