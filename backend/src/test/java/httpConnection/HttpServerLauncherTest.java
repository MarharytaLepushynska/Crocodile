package httpConnection;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.RoomService;
import service.UserService;
import utils.DataBase;

import java.io.IOException;
import java.util.TimeZone;

class HttpServerLauncherTest {
    private HttpServerLauncher server;
    private String adminToken;
    private String playerToken;
    private int roomId;

    private UserService userService;
    private RoomService roomService;

    @BeforeEach
    void setUp() throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
        userService = DataBase.getUserService();
        roomService = DataBase.getRoomService();
        server = new HttpServerLauncher();
        server.start(8081);

        RestAssured.port = 8081;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.defaultParser = Parser.JSON;

        adminToken = RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"admin_test\",\"password\":\"pass123\",\"avatarFileName\":\"avatar1.png\"}")
                .post("/register")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        playerToken = RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"player_test\",\"password\":\"pass123\",\"avatarFileName\":\"avatar1.png\"}")
                .post("/register")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        roomId = RestAssured.given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("{\"numberOfRounds\":3,\"durationOfRound\":60}")
                .post("/rooms")
                .then()
                .statusCode(200)
                .extract()
                .path("roomId");
    }

    @AfterEach
    void tearDown() {
        roomService.getRoomUsers(roomId).forEach(ruser ->
                roomService.leave(roomId, ruser.getUserId()));

        userService.deleteAll();
        server.stop();
    }

    @Test
    void registerShouldReturn200WithToken() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"one_test\",\"password\":\"pass123\",\"avatarFileName\":\"avatar1.png\"}")
                .post("/register")
                .then()
                .statusCode(200)
                .body("token", CoreMatchers.notNullValue())
                .body("username", CoreMatchers.is("one_test"));
    }

    @Test
    void registerShouldReturn409IfUsernameTaken() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"admin_test\",\"password\":\"pass123\",\"avatarFileName\":\"avatar1.png\"}")
                .post("/register")
                .then()
                .statusCode(409);
    }

    @Test
    void loginShouldReturn401WithWrongPassword() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"admin_test\",\"password\":\"pass1235\",\"avatarFileName\":\"avatar1.png\"}")
                .post("/login")
                .then()
                .statusCode(401);
    }

    @Test
    void requestShouldReturn401WithNoToken() {
        RestAssured.given()
                .expect()
                .statusCode(401)
                .when()
                .get("/rooms/" + roomId);
    }

    @Test
    void requestShouldReturn401WithBadToken() {
        RestAssured.given()
                .header("Authorization", "Bearer blabla")
                .expect()
                .statusCode(401)
                .when()
                .get("/rooms/" + roomId);
    }

    @Test
    void createRoomShouldReturn200() {
        RestAssured.given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("{\"numberOfRounds\":3,\"durationOfRound\":60}")
                .post("/rooms")
                .then()
                .statusCode(200)
                .body("roomId", CoreMatchers.notNullValue())
                .body("inviteCode", CoreMatchers.notNullValue())
                .body("status", CoreMatchers.is("WAITING"));
    }

    @Test
    void joinRoomShouldReturn200() {
        RestAssured.given()
                .header("Authorization", "Bearer " + playerToken)
                .post("/rooms/" + roomId + "/join")
                .then()
                .statusCode(200)
                .body("status", CoreMatchers.is("joined"));
    }

    @Test
    void startGameShouldReturn200ForAdmin() {
        RestAssured.given()
                .header("Authorization", "Bearer " + playerToken)
                .post("/rooms/" + roomId + "/join")
                .then()
                .statusCode(200);

        RestAssured.given()
                .header("Authorization", "Bearer " + adminToken)
                .post("/rooms/" + roomId + "/start")
                .then()
                .statusCode(200)
                .body("status", CoreMatchers.is("started"));
    }

    @Test
    void joinByInviteCodeShouldReturn200() {
        String inviteCode = RestAssured.given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/rooms/" + roomId)
                .then()
                .statusCode(200)
                .extract()
                .path("inviteCode");

        RestAssured.given()
                .header("Authorization", "Bearer " + playerToken)
                .post("/join/" + inviteCode)
                .then()
                .statusCode(200)
                .body("roomId", CoreMatchers.notNullValue());
    }

    @Test
    void joinByInvalidCodeShouldReturn404() {
        RestAssured.given()
                .header("Authorization", "Bearer " + playerToken)
                .post("/join/blabla")
                .then()
                .statusCode(404);
    }

}
