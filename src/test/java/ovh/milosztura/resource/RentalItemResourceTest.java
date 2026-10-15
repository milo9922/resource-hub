package ovh.milosztura.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ovh.milosztura.entity.RentalItem;
import ovh.milosztura.repository.RentalItemRepository;
import ovh.milosztura.util.RentalItemStatus;
import ovh.milosztura.util.RentalItemType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class RentalItemResourceTest {

    @Inject
    RentalItemRepository rentalItemRepository;

    private Long rentalItemId;

    @AfterEach
    void cleanUp() {
        if (rentalItemId != null) {
            given()
                    .when().delete("/api/v1/rental-item/{id}", rentalItemId)
                    .then().statusCode(anyOf(is(204), is(404)));
        }
    }

    @Test
    void givenValidRentalItem_whenCreate_thenPersistsRentalItem() {
        rentalItemId = ((Number) given()
                .contentType("application/json")
                .body("""
                        {"name":"Test room","type":"ROOM","status":"AVAILABLE"}
                        """)
                .when()
                .post("/api/v1/rental-item")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test room"))
                .body("type", equalTo("ROOM"))
                .body("status", equalTo("AVAILABLE"))
                .extract().path("id")).longValue();
    }

    @Test
    void givenRentalItems_whenGetAll_thenReturnsRentalItems() {
        rentalItemId = persistRentalItem("List test room");

        given()
                .when().get("/api/v1/rental-item/all")
                .then()
                .statusCode(200)
                .body("id", hasItem(rentalItemId.intValue()));
    }

    @Test
    void givenValidRentalItemId_whenGet_thenReturnsRentalItem() {
        rentalItemId = persistRentalItem("Get test room");

        given()
                .when().get("/api/v1/rental-item/{id}", rentalItemId)
                .then()
                .statusCode(200)
                .body("id", equalTo(rentalItemId.intValue()))
                .body("name", equalTo("Get test room"))
                .body("type", equalTo("ROOM"))
                .body("status", equalTo("AVAILABLE"));
    }

    @Test
    void givenUnknownRentalItemId_whenGet_thenReturns404NotFound() {
        given()
                .when().get("/api/v1/rental-item/{id}", Long.MAX_VALUE)
                .then().statusCode(404);
    }

    @Test
    void givenValidRentalItemIdAndUpdate_whenUpdate_thenPersistsUpdatedRentalItem() {
        rentalItemId = persistRentalItem("Original room");

        given()
                .contentType("application/json")
                .body("""
                        {"name":"Updated equipment","type":"EQUIPMENT","status":"RENTED"}
                        """)
                .when().put("/api/v1/rental-item/{id}", rentalItemId)
                .then()
                .statusCode(200)
                .body("id", equalTo(rentalItemId.intValue()))
                .body("name", equalTo("Updated equipment"))
                .body("type", equalTo("EQUIPMENT"))
                .body("status", equalTo("RENTED"));

        given()
                .when().get("/api/v1/rental-item/{id}", rentalItemId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated equipment"))
                .body("type", equalTo("EQUIPMENT"))
                .body("status", equalTo("RENTED"));
    }

    @Test
    void givenUnknownRentalItemId_whenUpdate_thenReturns404NotFound() {
        given()
                .contentType("application/json")
                .body("""
                        {"name":"Updated room","type":"ROOM","status":"AVAILABLE"}
                        """)
                .when().put("/api/v1/rental-item/{id}", Long.MAX_VALUE)
                .then().statusCode(404);
    }

    @Test
    void givenStatusOnlyUpdate_whenUpdate_thenReturns400AndKeepsRentalItemUnchanged() {
        rentalItemId = persistRentalItem("Status test room");

        given()
                .contentType("application/json")
                .body("""
                        {"status":"RENTED"}
                        """)
                .when().put("/api/v1/rental-item/{id}", rentalItemId)
                .then().statusCode(400);

        given()
                .when().get("/api/v1/rental-item/{id}", rentalItemId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Status test room"))
                .body("type", equalTo("ROOM"))
                .body("status", equalTo("AVAILABLE"));
    }

    @Test
    void givenValidRentalItemId_whenDelete_thenDeletesRentalItem() {
        RentalItem rentalItem = new RentalItem();
        rentalItem.name = "Delete test room";
        rentalItem.type = RentalItemType.ROOM;
        rentalItem.status = RentalItemStatus.AVAILABLE;
        QuarkusTransaction.requiringNew().run(() -> rentalItemRepository.persist(rentalItem));
        rentalItemId = rentalItem.id;

        given()
                .when().delete("/api/v1/rental-item/{id}", rentalItemId)
                .then().statusCode(204);

        given()
                .when().get("/api/v1/rental-item/{id}", rentalItemId)
                .then().statusCode(404);
    }

    @Test
    void givenRentalItemWithoutName_whenCreate_thenReturns400BadRequest() {
        given()
                .contentType("application/json")
                .body("""
                        {"name":" ","type":"ROOM","status":"AVAILABLE"}
                        """)
                .when().post("/api/v1/rental-item")
                .then().statusCode(400);
    }

    @Test
    void givenRentalItemWithoutStatus_whenCreate_thenReturns400BadRequest() {
        given()
                .contentType("application/json")
                .body("""
                        {"name":"Missing status room","type":"ROOM"}
                        """)
                .when().post("/api/v1/rental-item")
                .then().statusCode(400);
    }

    @Test
    void givenUnknownRentalItemId_whenDelete_thenReturns404NotFound() {
        given()
                .when().delete("/api/v1/rental-item/{id}", Long.MAX_VALUE)
                .then().statusCode(404);
    }

    private Long persistRentalItem(String name) {
        RentalItem rentalItem = new RentalItem();
        rentalItem.name = name;
        rentalItem.type = RentalItemType.ROOM;
        rentalItem.status = RentalItemStatus.AVAILABLE;
        QuarkusTransaction.requiringNew().run(() -> rentalItemRepository.persist(rentalItem));
        return rentalItem.id;
    }
}
