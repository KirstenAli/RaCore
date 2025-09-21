import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void bootOnce() throws Exception {
        TestApp.startServer();

        boolean up = false;
        for (int i = 0; i < 30 && !up; i++) {
            try {
                var req = HttpRequest.newBuilder(URI.create(BASE_URL + "/getQueryParameters")).GET().build();
                client.send(req, HttpResponse.BodyHandlers.ofString());
                up = true;
            } catch (Exception e) {
                Thread.sleep(100);
            }
        }
        if (!up) fail("Server did not start on " + BASE_URL);
    }

    @Test
    @DisplayName("GET /getPerson/{id} returns Alice and sets interceptor header")
    void getPerson() throws Exception {
        var res = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/getPerson/123")).GET().build());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Alice"));
        assertTrue(res.body().contains("30"));
        assertEquals("Ra Framework", res.headers().firstValue("X-Processed-By").orElse(null));
    }

    @Test
    @DisplayName("GET /getPathVariables/{id}/{name} echoes path variables")
    void getPathVariables() throws Exception {
        var res = send(HttpRequest.newBuilder(
                URI.create(BASE_URL + "/getPathVariables/42/Bob")).GET().build());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("42"));
        assertTrue(res.body().contains("Bob"));
    }

    @Test
    @DisplayName("GET /getQueryParameters echoes query params")
    void getQueryParameters() throws Exception {
        var res = send(HttpRequest.newBuilder(
                URI.create(BASE_URL + "/getQueryParameters?foo=bar&x=1")).GET().build());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("foo"));
        assertTrue(res.body().contains("bar"));
        assertTrue(res.body().contains("x"));
        assertTrue(res.body().contains("1"));
    }

    @Test
    @DisplayName("POST /addPerson parses JSON body into Person")
    void post_addPerson() throws Exception {
        String json = "{\"name\":\"Kirsten\",\"age\":19}";
        var res = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/addPerson"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Kirsten"));
        assertTrue(res.body().contains("19"));
    }

    @Test
    @DisplayName("PUT /updatePerson updates Person")
    void put_updatePerson() throws Exception {
        String json = "{\"name\":\"Carol\",\"age\":31}";
        var res = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/updatePerson"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Carol"));
        assertTrue(res.body().contains("31"));
    }

    @Test
    @DisplayName("PATCH /patchPerson partially updates Person")
    void patch_patchPerson() throws Exception {
        String json = "{\"name\":\"Dana\",\"age\":28}";
        var res = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/patchPerson"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Dana"));
        assertTrue(res.body().contains("28"));
    }

    @Test
    @DisplayName("DELETE /deletePerson confirms deletion")
    void delete_deletePerson() throws Exception {
        var res = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/deletePerson"))
                .DELETE()
                .build());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().toLowerCase().contains("deleted"));
    }

    private static HttpResponse<String> send(HttpRequest req) throws Exception {
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
