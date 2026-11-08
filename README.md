# DSA QUEST

> Learn. Solve. Level Up.

DSA QUEST is a gamified Data Structures and Algorithms learning platform for students preparing for coding interviews. It will combine structured learning, visual explanations, Java practice, and measurable progression without sacrificing technical depth.

## Current foundation

The repository now includes authentication and the Stage 5 student dashboard/progress foundation. The dashboard reports only persisted data; with no published lessons or verified activity, it intentionally displays zero values and useful empty states.

## Technology stack

- **Frontend:** React, TypeScript, Vite, Tailwind CSS, React Router
- **UI architecture:** shadcn/ui-compatible components using Tailwind design tokens
- **Planned backend:** Java, Spring Boot, Spring Security, JWT, Spring Data JPA, PostgreSQL, Flyway, Maven
- **Planned code execution:** Judge0 API through a secured backend integration

## Project structure

```text
frontend/
  public/
  src/
    app/          Application providers and root composition
    components/   Shared non-domain components
    features/     Domain-oriented feature modules
    hooks/        Reusable React hooks
    layout/       Page and navigation layout components
    lib/          Framework and utility helpers
    routes/       Route definitions
    services/     Backend integration boundaries
    styles/       Global styles and design tokens
    types/        Shared frontend types
    App.tsx
    main.tsx
backend/
  src/main/java/com/dsaquest/
    auth/          Authentication, JWT, refresh sessions
    dashboard/     Authenticated dashboard API and aggregation
    ...
```

The backend exposes versioned REST endpoints under `/api/v1`. `GET /api/v1/dashboard` derives the authenticated user's XP, level, streak, learning progress, achievements, and recent activity from PostgreSQL. The frontend accesses it through a typed service module and never stores refresh tokens in browser storage.

## Dashboard rules

- XP is summed from backend-owned `xp_transactions` records.
- Level 1 starts at 0 XP; level `n` begins at `100 * (n - 1)^2` XP.
- Streaks use verified `daily_activity` records in UTC; dashboard visits do not create activity.
- Learning progress counts only published lessons and persisted completion records.
- No curriculum PDF or lesson records are currently present in the repository, so learning, achievement, and activity sections remain honest empty states.

## Prerequisites

- Node.js 22+
- npm 11+
- Java 21+ and Maven for future backend stages
- PostgreSQL for future backend stages

## Frontend setup

```powershell
cd frontend
npm install
npm run dev
```

Other checks:

```powershell
npm run typecheck
npm run lint
npm run build
```

Copy `frontend/.env.example` to `frontend/.env.local` for local configuration. Never commit environment files containing secrets.

## Environment conventions

- `VITE_` variables are public build-time frontend configuration.
- Backend secrets must never be placed in frontend environment files.
- Future backend variables will use names such as `DATABASE_URL`, `JWT_SECRET`, and `JUDGE0_API_KEY`.

## Roadmap

1. **Stage 1:** Foundation and architecture setup
2. **Stage 2:** Spring Boot backend foundation
3. **Stage 3:** Curriculum and learning engine
4. **Stage 4:** Visualizers and Java coding interface
5. **Stage 5:** Isolated code execution
6. **Stage 6:** Administration, testing, and quality
7. **Stage 7:** Deployment and release
