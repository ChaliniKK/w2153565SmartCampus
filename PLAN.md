# 🗓️ 1-Day Smart Campus Coursework Plan
**Module:** 5COSC022W – Client-Server Architectures  
**Due:** 24th April 2026 at 13:00  
**Tech Stack:** Java · JAX-RS (Jersey) · Apache Tomcat · Maven · Postman  
**Marks:** 100 total (Coding 50% · Video 30% · Report/Questions 20%)

---

## ⏱️ Time Blocks Overview

| Block | Time | Focus | Git Commit |
|-------|------|-------|------------|
| 🔧 Block 1 | 08:00 – 09:30 | Project setup, Maven, Tomcat, Application class, Discovery endpoint | `feat: project bootstrap and discovery endpoint` |
| 🏠 Block 2 | 09:30 – 11:00 | Room Resource (CRUD + delete safety logic) | `feat: room management endpoints` |
| 📡 Block 3 | 11:00 – 12:30 | Sensor Resource (POST with validation, GET with ?type filter) | `feat: sensor resource with type filtering` |
| 🔗 Block 4 | 12:30 – 13:30 | Sub-resource locator + SensorReading endpoints | `feat: sensor readings sub-resource` |
| 🛡️ Block 5 | 13:30 – 15:30 | Exception mappers (409, 422, 403, 500) + Logging filter | `feat: exception mappers and logging filter` |
| 🧪 Block 6 | 15:30 – 17:00 | Postman testing all endpoints, fix bugs | `fix: bug fixes from postman testing` |
| 📝 Block 7 | 17:00 – 18:30 | Write README.md (overview + curl commands + build instructions) | `docs: complete README with report answers` |
| 🎥 Block 8 | 18:30 – 20:00 | Record video demonstration (max 10 min, Postman walkthrough) | `docs: add video demo link to README` |
| 🚀 Block 9 | 20:00 – 20:30 | Final review, push all commits, submit on Blackboard | `chore: final cleanup and submission` |

---

## 🔧 Block 1 — Project Setup (08:00 – 09:30)

### Goal
Bootstrap a Maven WAR project with Jersey + deploy to Tomcat.

### Steps
1. Create Maven project with `war` packaging
2. Add dependencies to `pom.xml`:
   - `jersey-container-servlet` (Jersey JAX-RS implementation)
   - `jersey-media-json-jackson` (JSON support via Jackson)
   - `jakarta.ws.rs-api` (JAX-RS API)
   - Tomcat `servlet-api` (provided scope)
3. Create `src/main/webapp/WEB-INF/web.xml` to register Jersey servlet
4. Create `SmartCampusApplication.java` extending `javax.ws.rs.core.Application` with `@ApplicationPath("/api/v1")`
5. Create POJOs: `Room.java`, `Sensor.java`, `SensorReading.java`
6. Create `DataStore.java` — a singleton holding `Map<String, Room>` and `Map<String, Sensor>` and `Map<String, List<SensorReading>>`
7. Implement `DiscoveryResource.java` → `GET /api/v1` → returns JSON with version, contact, resource links
8. Deploy to Tomcat and verify `GET /api/v1` works in browser/Postman

### Files Created
```
src/
├── main/
│   ├── java/com/smartcampus/
│   │   ├── SmartCampusApplication.java
│   │   ├── model/
│   │   │   ├── Room.java
│   │   │   ├── Sensor.java
│   │   │   └── SensorReading.java
│   │   ├── store/
│   │   │   └── DataStore.java
│   │   └── resource/
│   │       └── DiscoveryResource.java
│   └── webapp/WEB-INF/web.xml
└── pom.xml
```

### ✅ Commit
```
feat: project bootstrap and discovery endpoint
```

---

## 🏠 Block 2 — Room Management (09:30 – 11:00)

### Goal
Full CRUD for rooms + delete safety check.

### Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (blocked if sensors assigned) |

### Steps
1. Create `RoomResource.java` at `@Path("/rooms")`
2. `GET /` → return `Collection<Room>` from DataStore as JSON
3. `POST /` → accept JSON body, generate ID if not provided, add to DataStore, return `201 Created` with `Location` header
4. `GET /{roomId}` → fetch by ID, throw `404` if not found
5. `DELETE /{roomId}` → check `room.getSensorIds().isEmpty()` — if not empty, throw `RoomNotEmptyException` (handled in Part 5); else remove from DataStore, return `204 No Content`

### Files Created/Modified
```
resource/RoomResource.java
exception/RoomNotEmptyException.java   ← (throw only, map it in Block 5)
```

### ✅ Commit
```
feat: room management endpoints with delete safety logic
```

---

## 📡 Block 3 — Sensor Resource (11:00 – 12:30)

### Goal
Register sensors (with roomId validation) and filter by type.

### Endpoints
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/sensors` | Register sensor (validates roomId) |
| GET | `/api/v1/sensors` | List all sensors |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type |

### Steps
1. Create `SensorResource.java` at `@Path("/sensors")`
2. `POST /`:
   - Accept JSON body (Sensor object)
   - Check if `roomId` exists in DataStore → if not, throw `LinkedResourceNotFoundException`
   - Add sensor to DataStore
   - Add sensorId to the Room's `sensorIds` list
   - Return `201 Created`
3. `GET /`:
   - Accept optional `@QueryParam("type") String type`
   - If type is null → return all sensors
   - If type is provided → filter and return matching sensors
   - Return `200 OK`

### Files Created/Modified
```
resource/SensorResource.java
exception/LinkedResourceNotFoundException.java   ← (throw only, map in Block 5)
```

### ✅ Commit
```
feat: sensor resource with roomId validation and type filtering
```

---

## 🔗 Block 4 — Sub-Resource: Sensor Readings (12:30 – 13:30)

### Goal
Implement sub-resource locator pattern for sensor reading history.

### Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (updates sensor's currentValue) |

### Steps
1. In `SensorResource.java`, add sub-resource locator method:
   ```java
   @Path("{sensorId}/readings")
   public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
       return new SensorReadingResource(sensorId);
   }
   ```
2. Create `SensorReadingResource.java`:
   - Constructor takes `sensorId`
   - `GET /` → fetch `List<SensorReading>` for this sensorId from DataStore
   - `POST /`:
     - Check sensor exists → 404 if not
     - Check sensor status is not `"MAINTENANCE"` → throw `SensorUnavailableException` if it is
     - Generate reading UUID, set timestamp
     - Add to readings list in DataStore
     - **Side effect:** Update `sensor.setCurrentValue(reading.getValue())`
     - Return `201 Created`

### Files Created/Modified
```
resource/SensorReadingResource.java
exception/SensorUnavailableException.java   ← (throw only, map in Block 5)
```

### ✅ Commit
```
feat: sensor readings sub-resource with currentValue side effect
```

---

## 🛡️ Block 5 — Exception Mappers & Logging (13:30 – 15:30)

### Goal
Implement all 4 exception mappers + request/response logging filter.

### Exception Mappers

| Exception | HTTP Status | Scenario |
|-----------|------------|---------|
| `RoomNotEmptyException` | `409 Conflict` | Delete room that still has sensors |
| `LinkedResourceNotFoundException` | `422 Unprocessable Entity` | POST sensor with invalid roomId |
| `SensorUnavailableException` | `403 Forbidden` | POST reading to MAINTENANCE sensor |
| `Throwable` (catch-all) | `500 Internal Server Error` | Any unexpected runtime error |

### Steps
1. Create `mapper/RoomNotEmptyExceptionMapper.java` implementing `ExceptionMapper<RoomNotEmptyException>`
2. Create `mapper/LinkedResourceNotFoundExceptionMapper.java` → returns 422
3. Create `mapper/SensorUnavailableExceptionMapper.java` → returns 403
4. Create `mapper/GlobalExceptionMapper.java` implementing `ExceptionMapper<Throwable>` → returns 500 with generic message (NO stack trace exposed)
5. All mappers return JSON body:
   ```json
   {
     "status": 409,
     "error": "Conflict",
     "message": "Room LIB-301 cannot be deleted as it still has sensors assigned."
   }
   ```
6. Create `filter/LoggingFilter.java` implementing both `ContainerRequestFilter` and `ContainerResponseFilter`:
   - On request: log `METHOD URI`
   - On response: log `Response Status: XXX`
   - Use `java.util.logging.Logger`
   - Annotate with `@Provider`

### Files Created
```
exception/RoomNotEmptyException.java
exception/LinkedResourceNotFoundException.java
exception/SensorUnavailableException.java
mapper/RoomNotEmptyExceptionMapper.java
mapper/LinkedResourceNotFoundExceptionMapper.java
mapper/SensorUnavailableExceptionMapper.java
mapper/GlobalExceptionMapper.java
filter/LoggingFilter.java
```

### ✅ Commit
```
feat: exception mappers (409, 422, 403, 500) and logging filter
```

---

## 🧪 Block 6 — Postman Testing (15:30 – 17:00)

### Test Scenarios (all must pass before video recording)

#### Part 1
- [ ] `GET /api/v1` → 200, JSON with version + links

#### Part 2 — Rooms
- [ ] `POST /api/v1/rooms` → 201 Created
- [ ] `GET /api/v1/rooms` → 200, list of rooms
- [ ] `GET /api/v1/rooms/{id}` → 200, single room
- [ ] `GET /api/v1/rooms/INVALID` → 404
- [ ] `DELETE /api/v1/rooms/{id}` (empty) → 204
- [ ] `DELETE /api/v1/rooms/{id}` (has sensors) → 409 Conflict

#### Part 3 — Sensors
- [ ] `POST /api/v1/sensors` (valid roomId) → 201 Created
- [ ] `POST /api/v1/sensors` (invalid roomId) → 422
- [ ] `GET /api/v1/sensors` → 200, all sensors
- [ ] `GET /api/v1/sensors?type=CO2` → 200, filtered

#### Part 4 — Readings
- [ ] `POST /api/v1/sensors/{id}/readings` → 201, sensor currentValue updated
- [ ] `POST /api/v1/sensors/{id}/readings` (MAINTENANCE sensor) → 403
- [ ] `GET /api/v1/sensors/{id}/readings` → 200, list of readings

#### Part 5 — Error Handling
- [ ] Global 500 → trigger with bad data
- [ ] Logging visible in Tomcat console

### ✅ Commit
```
fix: postman testing bug fixes and edge case handling
```

---

## 📝 Block 7 — README.md Report (17:00 – 18:30)

### README Structure
```
# Smart Campus API
## Overview
## Tech Stack
## Project Structure
## Build & Run Instructions (step by step)
## API Endpoints Summary
## Sample curl Commands (minimum 5)
## Report: Answers to Coursework Questions
  - Part 1.1: JAX-RS Resource lifecycle
  - Part 1.2: HATEOAS explanation
  - Part 2.1: Full objects vs IDs
  - Part 2.2: DELETE idempotency
  - Part 3.1: @Consumes mismatch consequences
  - Part 3.2: @QueryParam vs path segment
  - Part 4.1: Sub-Resource Locator benefits
  - Part 5.2: 422 vs 404 semantics
  - Part 5.4: Stack trace security risks
  - Part 5.5: Filters vs manual logging
```

### ✅ Commit
```
docs: complete README with report answers and curl examples
```

---

## 🎥 Block 8 — Video Demo (18:30 – 20:00)

### Video Checklist (max 10 minutes)
- [ ] Introduce yourself and the project (30 sec)
- [ ] Show the project structure in IDE briefly (30 sec)
- [ ] Postman: Part 1 — Discovery endpoint
- [ ] Postman: Part 2 — Room CRUD + 409 delete error
- [ ] Postman: Part 3 — Sensor POST + type filter
- [ ] Postman: Part 4 — Readings POST (show currentValue update) + GET history
- [ ] Postman: Part 5 — Show 403, 422, 500 responses + Tomcat logs
- [ ] Wrap up (30 sec)

> **Requirements:** Camera ON, speak clearly, no need to show code.

### ✅ Commit
```
docs: add video demo link to README
```

---

## 🚀 Block 9 — Final Submission (20:00 – 20:30)

- [ ] Review all commits are pushed to `main`
- [ ] Verify GitHub repo is **public**
- [ ] Submit GitHub link on Blackboard
- [ ] Upload video file to Blackboard submission link
- [ ] Double-check README contains all 10 question answers

### ✅ Final Commit
```
chore: final submission cleanup
```

---

## 📁 Final Project Structure

```
w2153565SmartCampus/
├── pom.xml
├── README.md
├── PLAN.md
└── src/
    └── main/
        ├── java/com/smartcampus/
        │   ├── SmartCampusApplication.java
        │   ├── model/
        │   │   ├── Room.java
        │   │   ├── Sensor.java
        │   │   └── SensorReading.java
        │   ├── store/
        │   │   └── DataStore.java
        │   ├── resource/
        │   │   ├── DiscoveryResource.java
        │   │   ├── RoomResource.java
        │   │   ├── SensorResource.java
        │   │   └── SensorReadingResource.java
        │   ├── exception/
        │   │   ├── RoomNotEmptyException.java
        │   │   ├── LinkedResourceNotFoundException.java
        │   │   └── SensorUnavailableException.java
        │   ├── mapper/
        │   │   ├── RoomNotEmptyExceptionMapper.java
        │   │   ├── LinkedResourceNotFoundExceptionMapper.java
        │   │   ├── SensorUnavailableExceptionMapper.java
        │   │   └── GlobalExceptionMapper.java
        │   └── filter/
        │       └── LoggingFilter.java
        └── webapp/
            └── WEB-INF/
                └── web.xml
```

---

## ⚠️ Key Rules (Do NOT break these)

| Rule | Consequence |
|------|------------|
| ❌ No Spring Boot | Immediate ZERO |
| ❌ No database (SQL, MongoDB etc.) | ZERO mark |
| ❌ No ZIP submission | ZERO mark |
| ✅ Must use JAX-RS + Jersey only | Required |
| ✅ Must use HashMap/ArrayList | Required |
| ✅ GitHub repo must be PUBLIC | Required |
| ✅ Video is MANDATORY | 30% per task |

---

## 📊 Mark Allocation Reminder

For **each** task (e.g., Part 3.1 worth 10 marks):
- **5 marks** → Coding implementation
- **3 marks** → Video demonstration
- **2 marks** → Written question answer in README

> Focus on getting ALL parts working, then polish. A working Part 5 (30 marks) is more valuable than a perfect Part 2.
