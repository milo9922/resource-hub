package ovh.milosztura.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.validation.Valid;
import ovh.milosztura.request.AddRentalItemRequest;
import ovh.milosztura.response.RentalItemResponse;
import ovh.milosztura.service.RentalItemService;

import java.util.List;

@Path("/api/v1/rental-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RentalItemResource {

    @Inject
    RentalItemService rentalItemService;

    @GET
    @Path("/all")
    public List<RentalItemResponse> list() {
        return rentalItemService.findAll();
    }

    @GET
    @Path("/{id}")
    public RentalItemResponse get(@PathParam("id") Long id) {
        return rentalItemService.findById(id);
    }

    @POST
    public Response create(@Valid AddRentalItemRequest request) {
        var created = rentalItemService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public RentalItemResponse update(@PathParam("id") Long id, @Valid AddRentalItemRequest request) {
        return rentalItemService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        rentalItemService.delete(id);
    }
}
