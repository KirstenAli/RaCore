import com.sun.net.httpserver.HttpExchange;
import org.racore.Interceptor;
import org.racore.Request;

import java.time.LocalDateTime;
import static org.racore.Endpoint.*;

public class TestApp {
    public static void startServer() {
        defineRoutes();
        registerInterceptors();
        serveStatic(); // enable static file serving
    }

    private static void defineRoutes() {
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
    }

    private static void registerInterceptors() {
        addInterceptor(new LoggingInterceptor());
    }

    public static class Person {
        private String name;
        private int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

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
