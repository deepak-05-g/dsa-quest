export function RootRoute() {
  return (
    <section className="mx-auto flex min-h-[calc(100vh-89px)] max-w-7xl items-center px-6 py-20 lg:px-10">
      <div className="max-w-3xl">
        <p className="mb-6 text-sm font-bold uppercase tracking-[0.3em] text-accent">
          Learn. Solve. Level Up.
        </p>
        <h1 className="font-display text-5xl font-bold leading-tight tracking-tight sm:text-7xl">
          Your DSA journey starts with a clear path.
        </h1>
        <p className="mt-8 max-w-2xl text-lg leading-8 text-muted">
          DSA QUEST is being built as a focused learning platform for mastering
          data structures and algorithms through understanding, practice, and
          deliberate progress.
        </p>
        <div className="mt-10 flex flex-wrap gap-3 text-sm font-semibold text-muted">
          <span className="rounded-full border border-line bg-panel px-4 py-2">
            React foundation
          </span>
          <span className="rounded-full border border-line bg-panel px-4 py-2">
            Java-first curriculum
          </span>
          <span className="rounded-full border border-line bg-panel px-4 py-2">
            Built for depth
          </span>
        </div>
      </div>
    </section>
  )
}
