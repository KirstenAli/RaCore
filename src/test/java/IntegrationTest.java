import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    @DisplayName("Session cookie is set and reused across requests (count increments)")
    void sessionCookie_roundTrip() throws Exception {
        HttpResponse<String> r1 = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/session/count"))
                .GET()
                .build());

        assertEquals(200, r1.statusCode());

        String sidCookie = extractCookie(r1.headers(), "SID");
        assertNotNull(sidCookie, "Expected Set-Cookie: SID=... on first request");

        assertTrue(r1.body().contains("count=1"), "First request should start count at 1");

        HttpResponse<String> r2 = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/session/count"))
                .header("Cookie", sidCookie)
                .GET()
                .build());

        assertEquals(200, r2.statusCode());
        assertTrue(r2.body().contains("count=2"), "Second request should increment within same session");

        HttpResponse<String> r3 = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/session/count"))
                .header("Cookie", sidCookie)
                .GET()
                .build());

        assertEquals(200, r3.statusCode());
        assertTrue(r3.body().contains("count=3"), "Third request should increment within same session");
    }

    @Test
    @DisplayName("New client (no Cookie header) gets a new session")
    void sessionCookie_newClientGetsNewSession() throws Exception {
        HttpResponse<String> r1 = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/session/count")).GET().build());
        assertEquals(200, r1.statusCode());
        String sid1 = extractCookie(r1.headers(), "SID");
        assertNotNull(sid1);
        assertTrue(r1.body().contains("count=1"));

        HttpResponse<String> r2 = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/session/count")).GET().build());
        assertEquals(200, r2.statusCode());
        String sid2 = extractCookie(r2.headers(), "SID");
        assertNotNull(sid2);
        assertTrue(r2.body().contains("count=1"), "New client should start at 1");

        assertNotEquals(cookieValueOnly(sid1), cookieValueOnly(sid2), "Different clients should get different SID");
    }

    @Test
    @DisplayName("GET /stream streams chunks incrementally")
    void streaming_chunks() throws Exception {
        List<String> got = readFirstLines("/stream", 3);
        assertEquals(List.of("chunk-0", "chunk-1", "chunk-2"), got);
    }

    private static List<String> readFirstLines(String path, int n) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<java.io.InputStream> res =
                client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        assertEquals(200, res.statusCode());

        try (var br = new BufferedReader(new InputStreamReader(res.body(), StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            while (lines.size() < n) {
                String line = br.readLine();
                assertNotNull(line, "Stream ended early");
                if (!line.isEmpty()) lines.add(line);
            }
            return lines;
        }
    }

    private static String extractCookie(HttpHeaders headers, String cookieName) {
        List<String> setCookies = headers.allValues("Set-Cookie");
        for (String sc : setCookies) {
            if (sc.startsWith(cookieName + "=")) {
                return sc.split(";", 2)[0].trim();
            }
        }
        return null;
    }

    private static String cookieValueOnly(String cookieHeaderValue) {
        String[] kv = cookieHeaderValue.split("=", 2);
        return kv.length == 2 ? kv[1] : cookieHeaderValue;
    }
}
