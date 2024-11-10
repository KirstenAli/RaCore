package org.racore;

import com.sun.net.httpserver.HttpExchange;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Endpoint.get("/getPerson/{id}", _ -> new Person("Alice", 30));

        Endpoint.post("/addPerson", request -> {
            Person person = request.getBodyAs(Person.class);
            return "Received Person: " + person;
        });

        Endpoint.put("/updatePerson", request -> {
            Person person = request.getBodyAs(Person.class);
            return "Updated Person: " + person;
        });

        Endpoint.patch("/patchPerson", request -> {
            Person person = request.getBodyAs(Person.class);
            return "Partially updated Person: " + person;
        });

        Endpoint.delete("/deletePerson", _ -> "Person deleted");

        Endpoint.serveStatic("/index.html");

        Endpoint.addInterceptor(new LoggingInterceptor());
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
