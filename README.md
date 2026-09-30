# StudySync — Full Project

A day-order and academic-calendar aware adaptive study scheduler with a
self-calibrating Mental Load Balancer, multi-goal separation, rest-aware
scheduling, and weekly progress reporting.

This zip contains two independent projects:

```
studysync-backend/    Spring Boot 3.3 + PostgreSQL REST API
studysync-frontend/   React + Vite + Tailwind + Framer Motion UI
```

## Quick Start Order

1. **Set up the database** — install PostgreSQL, then run the SQL in
   `studysync-backend/setup-database.sql` to create the `studysyncdeploy_db`
   database (all tables are auto-created by Hibernate on first run).

2. **Run the backend** — open `studysync-backend/` in IntelliJ IDEA as a
   Maven project, update your DB password in `application.yml`, let it
   download dependencies, then run `StudySyncApplication.java`. Full
   details in `studysync-backend/README.md`. Starts on `http://localhost:8080`.

3. **Run the frontend**:
   ```bash
   cd studysync-frontend
   npm install
   npm run dev
   ```
   Opens on `http://localhost:5173`. Full details in
   `studysync-frontend/PROJECT_README.md`.

4. **Use the app** — register, set up your Day-Order timetable and commute
   distance, mark your Academic Calendar through your goal's target date,
   then go to Goals and create one (Auto or Manual).

## What's Implemented

- **Two-form input** (Day-Order Timetable + visual month-calendar mapping)
  → real free-hour computation for any date, any college, including a
  realistic daily routine (commute time from actual distance, morning prep,
  meals) skipped entirely on holidays
- **Multiple goals, fully separated**: a goal switcher (Dashboard, Full
  Timetable, Tasks, Weekly Report) lets you view any active or past goal's
  own schedule/tasks/history independently. The scheduler hard-blocks
  double-booking — two goals can never claim the same real hour. The
  "primary" goal (shown by default) is always whichever active goal has the
  soonest start date, with no manual switching needed as dates arrive.
  Mental Load stays one shared score (one brain, one capacity).
- **Goal creation**: Auto mode (10 seeded curricula: UPSC CSE, GATE CS,
  Campus Placement DSA, Bank PO, UGC NET, SSC CGL, CAT, NEET UG, JEE Main,
  IELTS) or Manual mode (custom subjects/topics with per-topic
  Low/Medium/High importance, add/remove rows freely, blank rows never get
  submitted) — both with a start date and target date. Deleting a goal
  removes everything generated for it (tasks, schedule) and nothing else.
- **Goal-backward, importance-ordered scheduling** with automatic
  **rest-block insertion** (15/20/25 min breaks after every 2 hours of
  continuous study, length tuned to current mental-load capacity)
- **Mental Load Balancer**: 0–100 daily score, self-calibrating personal
  threshold via exponential moving average
- **Two-track regeneration**: immediate consequence-driven replanning when
  a task/day is missed, PLUS a weekly threshold-drift check (Sunday nights)
  that gently replans only future/not-yet-started tasks if the student's
  real capacity has meaningfully shifted — even without a miss
- **Dashboard**: today's tasks split into Missed / Completed / Remaining,
  mark done/missed, active goals, mental load meter
- **Full Timetable page**: tabular, hour-by-hour schedule view across
  multiple days, per selected goal
- **Tasks page**: Today / Upcoming / History (History now correctly shows
  only past tasks, grouped by date)
- **Weekly Report + per-goal Report**: completion stats, subject-wise time
  breakdown, rule-based improvement tips, print-to-PDF download
- **Foreground browser notifications** at task start time (while the app
  tab is open — see honesty note below)
- JWT authentication, smooth page-transition animations throughout

## Two Honesty Notes

**Notifications**: the "Enable Notifications" behavior fires real browser
notifications when a task's start time arrives, but only while the
StudySync tab is open. True background/push notifications (working even
when the browser is closed) require a service worker + push server + VAPID
keys — a separate, larger feature not included here.

**Backend verification**: the backend was written and manually reviewed
line-by-line for correctness (including a full brace-balance check and a
pass verifying every DTO constructor call site matches its field order
exactly), but could not be compiled in the sandbox this was built in (no
access to Maven Central). It follows standard, well-established Spring Boot
patterns throughout. The frontend, by contrast, was compiled and
lint-checked successfully in this environment (`npm run build` and lint
both passed clean with 0 errors).
