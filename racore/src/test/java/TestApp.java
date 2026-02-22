import com.sun.net.httpserver.HttpExchange;
import org.racore.core.Endpoint;
import org.racore.core.requests.Request;
import org.racore.core.responses.StreamingResponse;
import org.racore.core.utils.CookieUtil;
import org.racore.interceptors.Interceptor;

import java.time.Duration;
import java.time.LocalDateTime;

import org.racore.core.session.Session;
import org.racore.core.session.SessionManager;
import org.racore.core.session.SessionStore;
import org.racore.interceptors.SessionInterceptor;

import static org.racore.core.Endpoint.*;

public class TestApp {

    private static final SessionStore sessionStore =
            new SessionStore(Duration.ofMinutes(30));

    private static final CookieUtil.CookieOptions cookieOptions =
            new CookieUtil.CookieOptions(
                    "/",     // Path
                    true,    // HttpOnly
                    false,   // Secure (true if HTTPS)
                    "Lax",   // SameSite
                    null     // Max-Age set by SessionManager to match idle timeout
            );

    private static final SessionManager sessions =
            new SessionManager("SID", sessionStore, cookieOptions);

    public static void startServer() {
        Endpoint.initializeServer();
        defineRoutes();
        registerInterceptors();
        serveStatic();
    }

    public static void defineRoutes() {
        get("/getPerson/{id}", _ -> new Person("Alice", 30));
        get("/getPathVariables/{id}/{name}", Request::getPathVariables);
        get("/getQueryParameters", Request::getQueryParams);

        post("/addPerson", request -> {
            Person person = request.getBodyAs(Person.class);
            return "Received Person: " + person;
        });

        post("/uploadFile", request -> "Files Received : " + request.getUploadedFiles().size());
        post("/sendForm", request -> "Form data received: " + request.getFormFields());

        put("/updatePerson", request -> {
            Person person = request.getBodyAs(Person.class);
            return "Updated Person: " + person;
        });

        patch("/patchPerson", request -> {
            Person person = request.getBodyAs(Person.class);
            return "Partially updated Person: " + person;
        });

        delete("/deletePerson", _ -> "Person deleted");

        get("/getFile/info.zip", _ -> resolvePath("/info.zip"));

        get("/session/count", request -> {
            HttpExchange ex = request.getExchange();
            Session session = sessions.current(ex);

            Integer count = (Integer) session.get("count");
            count = (count == null) ? 1 : (count + 1);
            session.put("count", count);

            return "count=" + count;
        });

        post("/session/destroy", request -> {
            sessions.destroy(request.getExchange());
            return "ok";
        });

        get("/stream", _ -> (StreamingResponse) out -> {
            for (int i = 0; i < 3; i++) {
                out.write(("chunk-" + i + "\n").getBytes());
                out.flush();
                Thread.sleep(50);
            }
        });
    }

    private static void registerInterceptors() {
        addInterceptor(new SessionInterceptor(sessions));
        addInterceptor(new LoggingInterceptor());
    }

    public static class Person {
        private String name;
        private int age;

        public Person() {}

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }

    public static class LoggingInterceptor implements Interceptor {

        @Override
        public boolean preHandle(HttpExchange exchange) {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("Received " + method + " request to " + path + " at " + LocalDateTime.now());
            return true;
        }

        @Override
        public Object postHandle(Object response, HttpExchange exchange) {
            exchange.getResponseHeaders().set("X-Processed-By", "Ra Framework");
            return response;
        }
    }
}