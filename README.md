# Smart Campus RESTful API

This project is a RESTful API for managing rooms, sensors, and sensor readings within a Smart Campus environment, built using **JAX-RS (Jersey)** and deployed on **Apache Tomcat**.

---

## API Design Overview

The Smart Campus API is designed following RESTful principles, providing endpoints for managing spatial entities (Rooms) and IoT devices (Sensors).
Key design decisions include:
- **Resource Naming**: Plural nouns for collections (`/rooms`, `/sensors`).
- **Standard HTTP Methods**: `GET` for retrieval, `POST` for creation, `DELETE` for removal.
- **Hierarchical Structure**: Utilizing Sub-Resource Locators to represent relationships, e.g., `/sensors/{id}/readings`.
- **Content Negotiation**: Uses JSON (`application/json`) for all requests and responses.
- **Statelessness**: A singleton `DataStore` mimics database persistence in memory, ensuring thread-safety and consistent state across requests without relying on server sessions.
- **Clear Status Codes**: Explicit use of standard HTTP status codes like `200 OK`, `201 Created`, `204 No Content`, `404 Not Found`, and `422 Unprocessable Entity` to communicate outcomes effectively.

---

## Tech Stack

- **Language**: Java 11
- **Framework**: JAX-RS (Jersey 2.41)
- **JSON Processing**: Jackson
- **Server**: Apache Tomcat (via NetBeans) / Eclipse Jetty (via Maven)
- **Build Tool**: Maven

---

## Build & Run Instructions

You can run this project using two distinct methods: through the NetBeans IDE or directly via the command line using Maven and Jetty. 

### Prerequisites

- Java JDK 11 or higher installed
- Apache Maven installed (if running via command line)
- NetBeans IDE configured with Apache Tomcat (if running via IDE)

### Option 1: Running via Command Line (Maven + Jetty)

1. Clone the repository and navigate to the project root:
   ```bash
   git clone https://github.com/ChaliniKK/w2153565SmartCampus.git
   cd w2153565SmartCampus
   ```
2. Run the application using the Jetty Maven plugin:
   ```bash
   mvn jetty:run
   ```
3. The server will start, and the API will be accessible at: `http://localhost:8080/api/v1/`

### Option 2: Running via NetBeans IDE (Tomcat)

1. Open NetBeans IDE.
2. Select **File > Open Project** and choose the cloned `w2153565SmartCampus` directory.
3. In the Projects window, right-click the project node and select **Run**.
4. NetBeans will automatically build the project, deploy the application to its configured Tomcat server, and start the API.
5. The API will be accessible at: `http://localhost:8080/api/v1/`

---

## Sample cURL Commands (Postman Alternative)

**1. Discovery Endpoint**

```bash
curl -X GET http://localhost:8080/api/v1/
```

**2. List All Rooms**

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

**3. Create a Room**

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Lecture Theatre 1", "building": "Drama and Theatre", "floor": 2}'
```

**4. Register a Sensor**

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "roomId": "LIB-301"}'
```

**5. Add a Sensor Reading**

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 22.5}'
```

---

## Conceptual Report 

### Part 1.1: JAX-RS Resource lifecycle

By default, the JAX-RS runtime treats Resource classes as **per-request** instances. This means a new instance of the resource class (e.g., `RoomResource`) is created for every incoming HTTP request and destroyed after the response is sent. Because of this architectural decision, we cannot store state (like our lists of rooms or sensors) as instance variables within the resource classes, as they would be lost between requests. To prevent data loss and race conditions, we must manage our in-memory data structures using a Singleton pattern (`DataStore`) combined with thread-safe collections like `ConcurrentHashMap`, ensuring all requests interact with the same synchronized, persistent state.

### Part 1.2: HATEOAS explanation

Providing "Hypermedia as the Engine of Application State" (HATEOAS) through links in responses is considered advanced RESTful design because it decouples the client from hardcoded API URIs. Instead of a client developer reading static documentation to figure out that the rooms endpoint is `/api/v1/rooms`, the root discovery endpoint dynamically provides that link. This benefits client developers by allowing the API routing structure to evolve over time without breaking client applications; the client simply follows the navigational links provided by the server, much like a human navigates a website clicking on hyperlinks.

### Part 2.1: Full objects vs IDs

When returning a list of rooms, returning only IDs reduces network bandwidth usage and speeds up transmission, but it requires the client to make multiple follow-up `GET` requests (the "N+1 problem") to fetch the full details for each room, increasing latency and server load. Returning full room objects increases the payload size and bandwidth used per request, but eliminates the need for follow-up requests, simplifying client-side processing. The choice depends on the client's needs; for a high-level summary view, IDs or partial objects are better, while full objects are better when all data is immediately required on screen.

### Part 2.2: DELETE idempotency

Yes, the `DELETE` operation in this implementation is **idempotent**. An operation is idempotent if making the identical request multiple times has the same effect on the server's state as making it once. If a client mistakenly sends the exact same `DELETE /api/v1/rooms/LIB-301` request multiple times, the first request will remove the room from the data store and return a `204 No Content`. Subsequent identical requests will find that the room is already null (not found in the map) and will still return a `204 No Content` without modifying any state or throwing an error. The server's state remains exactly the same after the first request as it does after the 100th request.

### Part 3.1: @Consumes mismatch consequences

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells the JAX-RS runtime that the `POST` method can only accept requests where the `Content-Type` header is `application/json`. If a client attempts to send data in a different format (like `text/plain` or `application/xml`), the JAX-RS runtime intercepts the request before it even reaches our resource method. It recognizes that no matching method exists for that media type and automatically returns an **HTTP 415 Unsupported Media Type** error to the client, protecting our method from parsing errors.

### Part 3.2: @QueryParam vs path segment

Using `@QueryParam` (e.g., `/sensors?type=CO2`) is considered superior to path segments (e.g., `/sensors/type/CO2`) for filtering and searching because the URL path is meant to uniquely identify a specific resource or a hierarchical collection of resources, whereas query parameters are meant to modify, sort, or filter that existing collection. A collection of sensors is still fundamentally the `/sensors` resource, just filtered. Furthermore, query parameters are optional and stackable (e.g., `?type=CO2&status=ACTIVE`), whereas making them path segments would require creating complex, rigid, and deeply nested routing paths that are difficult to maintain.

### Part 4.1: Sub-Resource Locator benefits

The Sub-Resource Locator pattern delegates the handling of nested paths (like `{sensorId}/readings`) to a separate resource class (`SensorReadingResource`). The architectural benefit is a significant reduction in code complexity and improved separation of concerns. Instead of defining every possible path for sensors and their nested resources inside one massive `SensorResource` controller—which would become bloated and hard to maintain—the logic is modularized. The parent class only handles locating the sub-resource context, and the sub-resource class cleanly handles its own specific CRUD operations, making the API much easier to scale, test, and read.

### Part 5.2: 422 vs 404 semantics

When a client sends a valid JSON payload to create a Sensor, but specifies a `roomId` that doesn't exist, returning a **422 Unprocessable Entity** is semantically more accurate than a standard **404 Not Found**. A 404 implies that the target URI itself (`POST /sensors`) doesn't exist. However, the URI is correct, and the JSON syntax is perfectly valid (so it's not a 400 Bad Request). The issue is that the server understands the content type and syntax, but cannot process the contained instructions due to a semantic dependency error (the missing foreign key reference). 422 perfectly describes this scenario.

### Part 5.4: Stack trace security risks

Exposing internal Java stack traces to external API consumers is a significant cybersecurity risk because it leaks sensitive implementation details about the server. An attacker reading a stack trace can discover the exact framework versions being used (e.g., Jersey 2.41, Jackson 2.15), the internal package and class structures, database drivers, and the specific lines of code where failure occurs. Armed with this knowledge, attackers can cross-reference the exposed technologies against public CVE (Common Vulnerabilities and Exposures) databases to identify specific exploits tailored to the server's exact tech stack, turning a generic error into a targeted attack vector.

### Part 5.5: Filters vs manual logging

Using JAX-RS filters for cross-cutting concerns like logging is advantageous over manual `Logger.info()` statements because it adheres to the DRY (Don't Repeat Yourself) principle and separates infrastructure logic from business logic. If logging were manually inserted, every single resource method would need identical boilerplate code, cluttering the controller and making it easy to forget. By using a filter, the logging logic is written in one central location and automatically applied globally to all incoming requests and outgoing responses by the runtime, ensuring consistent observability without polluting the application's core functionality.
