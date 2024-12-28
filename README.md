# ☀️ Ra Framework Documentation

Explore the **complete Java documentation** for the Ra framework [here](https://kirstenali.github.io/RaCore/).

---

## 🚀 Installation

To get started, add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.racore</groupId>
  <artifactId>ra-core</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

---

## 📚 Examples

This guide demonstrates how to use the Ra framework for building REST APIs, including handling HTTP methods, file uploads, and serving static files. Let's dive in with examples based on a **Person** object.

---

### 🛠 Defining an Endpoint

Create an endpoint effortlessly with the following syntax:

```java
verb("/endpoint", request -> {
    // Process the request (parse the request body into an object, access query parameters, path variables, files, or form data, etc.)
    // Return an object or a file path to be resolved
    return new MyObject();
});
```

---

### Quick Start: One-Line JSON Response

Start up a web server, register an endpoint, and return a JSON response in one line:

```java
get("/getPerson/{id}", _ -> new Person("Alice", 30));
```
- **Purpose**: Return a JSON response for a `Person` object.
- **Output**: `{ "name": "Alice", "age": 30 }`

---

### Retrieve Path Variables

```java
get("/getPathVariables/{id}/{name}", Request::getPathVariables);
```
- **Purpose**: Extract path variables from the URL.
- **Example**:
  - Request: `/getPathVariables/12/Jay`
  - Response: `{ "param0": "12", "param1": "Jay" }`

---

### Retrieve Query Parameters

```java
get("/getQueryParameters", Request::getQueryParams);
```
- **Purpose**: Extract query parameters from the URL.
- **Example**:
  - Request: `/getQueryParameters?name=John&age=25`
  - Response: `{ "name": "John", "age": "25" }`

---

### Add a New Person

```java
post("/addPerson", request -> {
    Person person = request.getBodyAs(Person.class);
    return "Received Person: " + person;
});
```
- **Purpose**: Accept JSON payloads to create a new person.
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
- **Purpose**: Update an entire `Person` object.
- **Example**:
  - Request Body: `{ "name": "Alice", "age": 35 }`
  - Response: `Updated Person: Person{name='Alice', age=35}`

---

### Delete a Person

```java
delete("/deletePerson", _ -> "Person deleted");
```
- **Purpose**: Delete a person.
- **Output**: `Person deleted`

---

### 📁 Upload Files

```java
post("/uploadFile", request -> "Files Received: " + request.getUploadedFiles().size());
```
- **Purpose**: Handle file uploads.
- **Example**:
  - Request: Upload multiple files.
  - Response: `Files Received: 3`

---

### 📝 Submit Form Data

```java
post("/sendForm", request -> "Form data received: " + request.getFormFields());
```
- **Purpose**: Process form data submitted via `POST`.
- **Example**:
  - Request Body: Form data with fields `name=John` and `age=25`.
  - Response: `Form data received: {name=John, age=25}`

---

### 📂 Serve Static Files

```java
serveStatic(); // Enable serving resources from the static folder in resources
get("/getFile/info.zip", _ -> resolvePath("/info.zip"));
```
- **Purpose**: Serve static files like images, documents, or archives.
- **Example**:
  - Request: `/getFile/info.zip`
  - File Response: `info.zip` from `resources/static`.

---

Happy coding! ✨

