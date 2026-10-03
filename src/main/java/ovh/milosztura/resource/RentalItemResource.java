package ovh.milosztura.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ovh.milosztura.response.RentalItemResponse;
import ovh.milosztura.service.RentalItemService;

import java.util.List;

@Path("/rental-item")
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
}
