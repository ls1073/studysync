# StudySync Frontend

React + Vite frontend for StudySync, styled with Tailwind CSS and animated
with Framer Motion.

## Tech Stack
- React 18 + Vite
- React Router v6
- Tailwind CSS
- Framer Motion (animations/transitions)
- Recharts (mental load trend chart)
- Axios (API client)
- lucide-react (icons)

## 1. Prerequisites
- Node.js 18+ and npm
- The StudySync backend running at `http://localhost:8080` (see
  `studysync-backend/README.md`)

## 2. Install Dependencies
```bash
cd studysync-frontend
npm install
```

## 3. Run in Development
```bash
npm run dev
```
This starts the dev server, by default at `http://localhost:5173`. The
backend's CORS config already allows this origin.

## 4. Build for Production
```bash
npm run build
```
Output goes to `dist/`. Serve it with any static file server, or point
Spring Boot to serve it directly if you want a single deployable unit later.

## 5. Project Structure
```
src/
  api/          - Axios wrapper + one file per backend resource (auth, goals, tasks, etc.)
  context/      - AuthContext (JWT token + user state, persisted to localStorage)
  components/   - Reusable UI: Sidebar, TaskCard, GoalCard, LoadMeter, ProtectedRoute
  pages/        - One file per route: Login, Register, Dashboard, Goals, GoalDetail,
                  Timetable, Tasks, MentalLoad
```

## 6. Changing the Backend URL
If your backend runs somewhere other than `http://localhost:8080`, edit:
```
src/api/client.js
```
and change the `API_BASE_URL` constant.

## 7. First-Time Walkthrough
1. Register a new account on `/register`.
2. Go to **Timetable Setup** in the sidebar:
   - Add your Day-Order templates (Day 1, Day 2, ...) with each class's
     subject + start/end time.
   - Use the visual month calendar below it to mark every date (through
     your intended goal's target date) with a Day-Order or "Holiday" —
     click any day's dropdown to set or change it, including mid-term
     schedule shifts.
   - A banner will prompt you to head to Goals once you're set up.
3. Go to **Goals** → **New Goal**:
   - Set a Start Date and Target Date.
   - Pick **Auto** and search a template (UPSC, GATE, Bank PO, Placement,
     SSC CGL, CAT, NEET, JEE, IELTS...) to auto-populate subjects/topics, or
   - Pick **Manual** to define your own subjects (with a Low/Medium/High
     importance level) and topics.
4. StudySync immediately generates a day-by-day schedule — including
   automatic rest blocks — visible on the **Dashboard** and in full tabular
   form on **Full Timetable**.
5. Mark tasks done (with an effort rating) or missed on the Dashboard or
   Tasks page — missed tasks trigger consequence + regeneration
   automatically. Rest blocks are shown but don't need marking.
6. Check **Mental Load** to see your personal capacity threshold evolve,
   and **Weekly Report** for a full progress summary with improvement tips
   and a "Download Report" (print-to-PDF) button.
7. If you keep the app open in a browser tab, allow notifications when
   prompted — you'll get a real browser notification when a task's start
   time arrives.

## 8. Common Issues
- **Network errors / CORS errors in the browser console**: make sure the
  backend is actually running on port 8080, and that you're accessing the
  frontend via `http://localhost:5173` (matches the backend's allowed
  origins in `application.yml`).
- **401 errors after some time**: JWT tokens expire after 24 hours by
  default (`app.jwt.expiration-ms` in the backend's `application.yml`) —
  just log in again.
