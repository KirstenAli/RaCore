import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TomcatIntegrationTest {

    static String BASE_URL;
    static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void bootOnce() throws Exception {
        int port = TestTomcatApp.start();
        BASE_URL = "http://localhost:" + port;

        boolean up = false;
        for (int i = 0; i < 80 && !up; i++) {
            try {
                var req = HttpRequest.newBuilder(URI.create(BASE_URL + "/getPerson/123")).GET().build();
                client.send(req, HttpResponse.BodyHandlers.ofString());
                up = true;
            } catch (Exception e) {
                Thread.sleep(100);
            }
        }
        if (!up) Assertions.fail("Tomcat did not start on " + BASE_URL);
    }

    @AfterAll
    static void shutdown() throws Exception {
        TestTomcatApp.stop();
    }

    @Test
    @DisplayName("GET /getPerson/{id} works under Tomcat")
    void getPerson_underTomcat() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(BASE_URL + "/getPerson/123"))
                .GET()
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, res.statusCode());
        Assertions.assertTrue(res.body().contains("Hello Person"));
    }
}