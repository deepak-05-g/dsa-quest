# DSA QUEST

> Learn. Solve. Level Up.

DSA QUEST is a gamified Data Structures and Algorithms learning platform for students preparing for coding interviews. It will combine structured learning, visual explanations, Java practice, and measurable progression without sacrificing technical depth.

## Stage 1 foundation

Stage 1 establishes the frontend application shell and repository architecture. It intentionally does not include authentication, lessons, dashboards, visualizers, code execution, or progression data.

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
backend/          Reserved for the future Spring Boot service
```

The backend will expose versioned REST endpoints under `/api/v1`. The frontend will access them through typed service modules, with the API base URL supplied by environment configuration. Authentication, persistence, and business logic remain future-stage work.

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

