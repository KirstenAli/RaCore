### View the Java Docs
You can explore the complete Java documentation for the Ra framework [here](https://kirstenali.github.io/RaCore/).

# Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.racore</groupId>
  <artifactId>ra-core</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

# Examples

This tutorial provides an overview of REST API endpoints using the Ra framework. It demonstrates various HTTP methods such as GET, POST, PUT, DELETE, as well as handling file uploads and serving static files. For illustration, we'll use a Person object as an example.

---

## Defining an Endpoint

When using Ra, you can define an endpoint with the following syntax:

```java
verb("/endpoint", request -> {
    // Do something cool with the request
    // For example, parse data into an object, access query parameters, path variables, files, or form data, etc.

    // Then return an object or a Path to a resource
    return new MyObject();
});
```

---

### Start a web server, register an endpoint, and return a JSON response with a single line of Java
```java
get("/getPerson/{id}", _ -> new Person("Alice", 30));
```
- **Description**: Return a person object in JSON format.
- **Output**: `{"name": "Alice", "age": 30}`

---

### Retrieve Path Variables
```java
get("/getPathVariables/{id}/{name}", Request::getPathVariables);
```
- **Description**: Extracts path variables from the URL.
- **Example**:
    - Request: `/getPathVariables/12/Jay`
    - Response: `{"param0": "12", "param1": "Jay"}`
---

### Retrieve Query Parameters
```java
get("/getQueryParameters", Request::getQueryParams);
```
- **Description**: Extracts query parameters from the request URL.
- **Example**:
    - Request: `/getQueryParameters?name=John&age=25`
    - Response: `{"name": "John", "age": "30"}`

---

### Add a New Person
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

### Update a Person
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

### Delete a Person
```java
delete("/deletePerson", _ -> "Person deleted");
```
- **Description**: Deletes a person.
- **Output**: Returns a confirmation message: `Person deleted`.

---

### Upload a File
```java
post("/uploadFile", request -> "Files Received: " + request.getUploadedFiles().size());
```
- **Description**: Handles file uploads and returns the number of files received.
- **Example**:
    - Request: Upload multiple files.
    - Response: `Files Received: 3`

---

### Submit Form Data
```java
post("/sendForm", request -> "Form data received: " + request.getFormFields());
```
- **Description**: Processes form data submitted via a `POST` request.
- **Example**:
    - Request Body: Form data with fields `name=John` and `age=25`.
    - Response: `Form data received: {name=John, age=25}`

---

### Serve Static Files
```java
serveStatic(); // Enable serving resources from the static folder in resources
get("/getFile/info.zip", _ -> resolvePath("/info.zip"));
```
- **Description**:
    - Enables serving static files from a predefined `resources/static` folder.
    - Serves a specific file, `info.zip`, when requested.

---
