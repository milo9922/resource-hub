package ovh.milosztura.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ReservationResourceTest {

    private Long rentalItemId;
    private Long reservationId;

    @AfterEach
    void cleanUp() {
        if (reservationId != null) {
            given()
                    .when().delete("/api/v1/reservation/{id}", reservationId)
                    .then().statusCode(anyOf(is(204), is(404)));
        }
        if (rentalItemId != null) {
            given()
                    .when().delete("/api/v1/rental-item/{id}", rentalItemId)
                    .then().statusCode(anyOf(is(204), is(404)));
        }
    }

    @Test
    void givenValidReservation_whenCreate_thenPersistsReservation() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        var end = start.plusHours(2);

        reservationId = ((Number) given()
                .contentType("application/json")
                .body(reservationRequest(rentalItemId, start, end, "Taylor"))
                .when().post("/api/v1/reservation")
                .then()
                .statusCode(201)
                .body("rentalItemId", equalTo(rentalItemId.intValue()))
                .body("status", equalTo("PENDING"))
                .body("reservedBy", equalTo("Taylor"))
                .body("startsAt", equalTo(start.toString()))
                .body("endsAt", equalTo(end.toString()))
                .extract().path("id")).longValue();
    }

    @Test
    void givenReservations_whenGetAll_thenReturnsReservations() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        reservationId = createReservation(rentalItemId, start, start.plusHours(2));

        given()
                .when().get("/api/v1/reservation/all")
                .then()
                .statusCode(200)
                .body("id", hasItem(reservationId.intValue()));
    }

    @Test
    void givenValidReservationId_whenGet_thenReturnsReservation() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        reservationId = createReservation(rentalItemId, start, start.plusHours(2));

        given()
                .when().get("/api/v1/reservation/{id}", reservationId)
                .then()
                .statusCode(200)
                .body("id", equalTo(reservationId.intValue()))
                .body("rentalItemId", equalTo(rentalItemId.intValue()))
                .body("status", equalTo("PENDING"))
                .body("reservedBy", equalTo("Taylor"));
    }

    @Test
    void givenUnknownReservationId_whenGet_thenReturns404NotFound() {
        given()
                .when().get("/api/v1/reservation/{id}", Long.MAX_VALUE)
                .then().statusCode(404);
    }

    @Test
    void givenValidReservationIdAndUpdate_whenUpdate_thenPersistsUpdatedReservation() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        reservationId = createReservation(rentalItemId, start, start.plusHours(2));
        var updatedStart = start.plusDays(1);

        given()
                .contentType("application/json")
                .body(reservationRequest(rentalItemId, updatedStart, updatedStart.plusHours(3), "Jordan"))
                .when().put("/api/v1/reservation/{id}", reservationId)
                .then()
                .statusCode(200)
                .body("rentalItemId", equalTo(rentalItemId.intValue()))
                .body("reservedBy", equalTo("Jordan"))
                .body("startsAt", equalTo(updatedStart.toString()))
                .body("endsAt", equalTo(updatedStart.plusHours(3).toString()))
                .body("status", equalTo("PENDING"));

        given()
                .when().get("/api/v1/reservation/{id}", reservationId)
                .then()
                .statusCode(200)
                .body("reservedBy", equalTo("Jordan"))
                .body("startsAt", equalTo(updatedStart.toString()))
                .body("status", equalTo("PENDING"));
    }

    @Test
    void givenUnknownReservationId_whenUpdate_thenReturns404NotFound() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);

        given()
                .contentType("application/json")
                .body(reservationRequest(rentalItemId, start, start.plusHours(2), "Taylor"))
                .when().put("/api/v1/reservation/{id}", Long.MAX_VALUE)
                .then().statusCode(404);
    }

    @Test
    void givenUpdateWithoutReservedBy_whenUpdate_thenReturns400BadRequest() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        reservationId = createReservation(rentalItemId, start, start.plusHours(2));

        given()
                .contentType("application/json")
                .body("""
                        {"rentalItemId":%d,"startsAt":"%s","endsAt":"%s"}
                        """.formatted(rentalItemId, start.plusDays(1), start.plusDays(1).plusHours(2)))
                .when().put("/api/v1/reservation/{id}", reservationId)
                .then().statusCode(400);

        given()
                .when().get("/api/v1/reservation/{id}", reservationId)
                .then()
                .statusCode(200)
                .body("reservedBy", equalTo("Taylor"))
                .body("status", equalTo("PENDING"));
    }

    @Test
    void givenStatusOnlyUpdate_whenPatchStatus_thenChangesOnlyReservationStatus() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        var end = start.plusHours(2);
        reservationId = createReservation(rentalItemId, start, end);

        given()
                .when().patch("/api/v1/reservation/{id}/CONFIRMED", reservationId)
                .then()
                .statusCode(200)
                .body("id", equalTo(reservationId.intValue()))
                .body("status", equalTo("CONFIRMED"))
                .body("rentalItemId", equalTo(rentalItemId.intValue()))
                .body("reservedBy", equalTo("Taylor"))
                .body("startsAt", equalTo(start.toString()))
                .body("endsAt", equalTo(end.toString()));

        given()
                .when().get("/api/v1/reservation/{id}", reservationId)
                .then()
                .statusCode(200)
                .body("status", equalTo("CONFIRMED"))
                .body("reservedBy", equalTo("Taylor"))
                .body("startsAt", equalTo(start.toString()));
    }

    @Test
    void givenUnknownReservationId_whenPatchStatus_thenReturns404NotFound() {
        given()
                .when().patch("/api/v1/reservation/{id}/CONFIRMED", Long.MAX_VALUE)
                .then().statusCode(404);
    }

    @Test
    void givenInvalidReservationStatus_whenPatchStatus_thenReturns404NotFound() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        reservationId = createReservation(rentalItemId, start, start.plusHours(2));

        given()
                .when().patch("/api/v1/reservation/{id}/{status}", reservationId, "INVALID")
                .then().statusCode(404);

        given()
                .when().get("/api/v1/reservation/{id}", reservationId)
                .then().statusCode(200)
                .body("status", equalTo("PENDING"));
    }

    @Test
    void givenValidReservationId_whenDelete_thenDeletesReservation() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);
        reservationId = createReservation(rentalItemId, start, start.plusHours(2));

        given()
                .when().delete("/api/v1/reservation/{id}", reservationId)
                .then().statusCode(204);

        given()
                .when().get("/api/v1/reservation/{id}", reservationId)
                .then().statusCode(404);
    }

    @Test
    void givenUnknownReservationId_whenDelete_thenReturns404NotFound() {
        given()
                .when().delete("/api/v1/reservation/{id}", Long.MAX_VALUE)
                .then().statusCode(404);
    }

    @Test
    void givenReservationWithEndBeforeStart_whenCreate_thenReturns400BadRequest() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);

        given()
                .contentType("application/json")
                .body(reservationRequest(rentalItemId, start, start.minusHours(1), "Taylor"))
                .when().post("/api/v1/reservation")
                .then().statusCode(400);
    }

    @Test
    void givenReservationWithoutReservedBy_whenCreate_thenReturns400BadRequest() {
        rentalItemId = createAvailableRentalItem();
        var start = LocalDateTime.now().plusDays(2).withNano(0);

        given()
                .contentType("application/json")
                .body("""
                        {"rentalItemId":%d,"startsAt":"%s","endsAt":"%s"}
                        """.formatted(rentalItemId, start, start.plusHours(2)))
                .when().post("/api/v1/reservation")
                .then().statusCode(400);
    }

    @Test
    void givenUnavailableRentalItem_whenCreateReservation_thenReturns400BadRequest() {
        rentalItemId = createAvailableRentalItem();
        given()
                .contentType("application/json")
                .body("""
                        {"name":"Reservation test item","type":"EQUIPMENT","status":"RENTED"}
                        """)
                .when().put("/api/v1/rental-item/{id}", rentalItemId)
                .then().statusCode(200);
        var start = LocalDateTime.now().plusDays(2).withNano(0);

        given()
                .contentType("application/json")
                .body(reservationRequest(rentalItemId, start, start.plusHours(2), "Taylor"))
                .when().post("/api/v1/reservation")
                .then().statusCode(400);
    }

    private Long createAvailableRentalItem() {
        return ((Number) given()
                .contentType("application/json")
                .body("""
                        {"name":"Reservation test item","type":"EQUIPMENT","status":"AVAILABLE"}
                        """)
                .when().post("/api/v1/rental-item")
                .then().statusCode(201)
                .extract().path("id")).longValue();
    }

    private Long createReservation(Long itemId, LocalDateTime start, LocalDateTime end) {
        return ((Number) given()
                .contentType("application/json")
                .body(reservationRequest(itemId, start, end, "Taylor"))
                .when().post("/api/v1/reservation")
                .then().statusCode(201)
                .extract().path("id")).longValue();
    }

    private String reservationRequest(Long itemId, LocalDateTime start, LocalDateTime end, String reservedBy) {
        return """
                {"rentalItemId":%d,"startsAt":"%s","endsAt":"%s","reservedBy":"%s"}
                """.formatted(itemId, start, end, reservedBy);
    }
}
