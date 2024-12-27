# Examples

This tutorial demonstrates various REST API endpoints using the **Ra** framework. Each endpoint showcases a specific HTTP method and use case, including GET, POST, PUT, PATCH, DELETE, uploading, and serving static files. Let’s use a `Person` object as an example.

---

### 1. Get a Person by ID
```java
get("/getPerson/{id}", _ -> new Person("Alice", 30));
```
- **Description**: Retrieves a person object by their ID (placeholder functionality).
- **Output**: Returns a `Person` object with name `Alice` and age `30`.

---

### 2. Retrieve Path Variables
```java
get("/getPathVariable/{id}", request -> request.getPathVariables().toString());
```
- **Description**: Extracts path variables from the URL.
- **Example**:
    - Request: `/getPathVariable/123`
    - Response: `{id=123}`

---

### 3. Retrieve Query Parameters
```java
get("/getQueryParameters", request -> request.getQueryParams().toString());
```
- **Description**: Extracts query parameters from the request URL.
- **Example**:
    - Request: `/getQueryParameters?name=John&age=25`
    - Response: `{name=John, age=25}`

---

### 4. Add a New Person
```java
post("/addPerson", request -> {
    Person person = request.getBodyAs(Person.class);
    return "Received Person: " + person;
});
```
- **Description**: Accepts a JSON payload to create a new person.
- **Example**:
    - Request Body: `{ "name": "Alice", "age": 30 }`
    - Response: `Received Person: Person{name='Alice', age=30}`

---

### 5. Upload a File
```java
post("/uploadFile", request -> "Files Received: " + request.getUploadedFiles().size());
```
- **Description**: Handles file uploads and returns the number of files received.
- **Example**:
    - Request: Upload multiple files.
    - Response: `Files Received: 3`

---

### 6. Submit Form Data
```java
post("/sendForm", request -> "Form data received: " + request.getFormFields().toString());
```
- **Description**: Processes form data submitted via a `POST` request.
- **Example**:
    - Request Body: Form data with fields `name=John` and `age=25`.
    - Response: `Form data received: {name=John, age=25}`

---

### 7. Update a Person
```java
put("/updatePerson", request -> {
    Person person = request.getBodyAs(Person.class);
    return "Updated Person: " + person;
});
```
- **Description**: Updates an entire `Person` object using a `PUT` request.
- **Example**:
    - Request Body: `{ "name": "Alice", "age": 35 }`
    - Response: `Updated Person: Person{name='Alice', age=35}`

---

### 8. Partially Update a Person
```java
patch("/patchPerson", request -> {
    Person person = request.getBodyAs(Person.class);
    return "Partially updated Person: " + person;
});
```
- **Description**: Updates specific fields of a `Person` object using a `PATCH` request.
- **Example**:
    - Request Body: `{ "age": 40 }`
    - Response: `Partially updated Person: Person{name='Alice', age=40}`

---

### 9. Delete a Person
```java
delete("/deletePerson", _ -> "Person deleted");
```
- **Description**: Deletes a person (placeholder functionality).
- **Output**: Returns a confirmation message: `Person deleted`.

---

### 10. Serve Static Files
```java
serveStatic(); // Enable serving resources from the static folder in resources
get("/getFile/info.zip", _ -> resolvePath("/info.zip"));
```
- **Description**:
    - Enables serving static files from a predefined `resources/static` folder.
    - Serves a specific file, `info.zip`, when requested.

---

### 11. Logging Interceptor
```java
addInterceptor(new LoggingInterceptor());
```
- **Description**: Adds a logging interceptor to log incoming requests for debugging or monitoring purposes.

---
