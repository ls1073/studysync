# StudySync Backend

Spring Boot 3.3 + PostgreSQL backend for StudySync — a Day-Order and
Academic-Calendar aware adaptive study scheduler with a self-calibrating
Mental Load Balancer.

## Tech Stack
- Java 17
- Spring Boot 3.3.4 (Web, Data JPA, Security, Validation)
- PostgreSQL
- JWT authentication (jjwt)
- Maven

## 1. Prerequisites
- JDK 17+ installed
- PostgreSQL installed and running (v13+)
- IntelliJ IDEA (Community or Ultimate)

## 2. Database Setup
Open psql or pgAdmin and run:

```sql
CREATE DATABASE studysyncdeploy_db;
```

(See `setup-database.sql` in this folder.)

The application uses `ddl-auto: update`, so **all tables are created
automatically** the first time you run the app — you do not need to write
any CREATE TABLE statements yourself.

## 3. Configure Database Credentials
Open `src/main/resources/application.yml` and update if your PostgreSQL
username/password differ from the defaults:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/studysyncdeploy_db
    username: postgres
    password: postgres
```

## 4. Open in IntelliJ IDEA
1. Open IntelliJ IDEA → **File → Open** → select the `studysync-backend` folder.
2. IntelliJ will detect it as a Maven project and prompt to load it — click
   **Load Maven Project** (or it may happen automatically).
3. Wait for Maven to download all dependencies (needs internet access; this
   is a one-time download into your local `~/.m2` repository).
4. Once indexing finishes, open `src/main/java/com/studysync/StudySyncApplication.java`.
5. Click the green **Run** arrow next to the `main` method (or right-click →
   Run 'StudySyncApplication').

If the run succeeds, you'll see Spring Boot's startup banner in the console
and a line like:
```
Tomcat started on port(s): 8080 (http)
```

The API is now live at `http://localhost:8080`.

## 5. Verify It's Working
Test the registration endpoint with curl (or Postman):

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","fullName":"Test User"}'
```

You should get back a JSON response with a `token`.

## 6. Seeded Data
On first startup, `DataSeeder` automatically inserts 10 curated goal
templates (curriculum) into the database so the "Auto" goal-creation mode
works immediately:

- UPSC CSE
- GATE CS
- Campus Placement - DSA
- Bank PO
- UGC NET Computer Science
- SSC CGL
- CAT MBA Entrance
- NEET UG
- JEE Main
- IELTS

You can view them via `GET /api/goal-templates` (no auth required).

## 7. Project Structure
```
src/main/java/com/studysync/
  config/       - Security config, CORS, current-user helper, data seeder
  security/     - JWT utilities and filter
  entity/       - JPA entities
  repository/   - Spring Data repositories
  dto/          - Request/response DTOs (grouped by feature)
  service/      - Business logic (the core scheduling/mental-load engines live here)
  controller/   - REST controllers
  exception/    - Global exception handling
```

## 8. Core Logic — Where to Look
- **Free-hour computation**: `service/FreeHourService.java`
- **Goal-backward scheduling + rest-block insertion**: `service/SchedulerService.java`
- **Mental Load Balancer (scoring + self-calibration)**: `service/MentalLoadService.java`
- **Consequence & regeneration on missed tasks**: `service/RegenerationService.java`
- **Weekly report + rule-based tips**: `service/WeeklyReportService.java`
- **Automatic daily missed-task detection**: `service/TaskService.java` (`@Scheduled` job, runs at 00:05 daily)

## 9. API Overview
All endpoints are under `/api`. Auth endpoints and `/api/goal-templates` are
public; everything else requires a `Authorization: Bearer <token>` header
obtained from `/api/auth/login` or `/api/auth/register`.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Timetable | `GET/POST /timetable/day-order`, `DELETE /timetable/day-order/{id}` |
| Calendar | `POST /calendar/entry`, `POST /calendar/bulk-range`, `GET /calendar` |
| Goal Templates | `GET /goal-templates`, `GET /goal-templates/{id}` |
| Goals | `POST /goals`, `GET /goals`, `GET /goals/{id}` |
| Tasks | `GET /tasks/today`, `/tasks/date/{date}`, `/tasks/upcoming`, `/tasks/history`, `POST /tasks/{id}/complete`, `POST /tasks/{id}/miss` |
| Mental Load | `GET /mental-load/today`, `GET /mental-load/trend` |
| Dashboard | `GET /dashboard` |
| Reports | `GET /reports/weekly?weekStart=...&weekEnd=...` (defaults to last 7 days) |
| Notifications | `GET /notifications`, `POST /notifications/{id}/read` |

## 10. Common Issues
- **"Could not resolve parent POM" / dependency download errors**: your
  machine needs internet access to Maven Central the first time you open
  the project. Corporate proxies/firewalls can block this — configure
  IntelliJ's Maven proxy settings if needed.
- **Connection refused to PostgreSQL**: make sure the PostgreSQL service is
  actually running (`pg_ctl status` or check Services on Windows) and that
  the port in `application.yml` (default 5432) matches your installation.
- **Port 8080 already in use**: change `server.port` in `application.yml`.
