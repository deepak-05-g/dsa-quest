import { Outlet, NavLink } from 'react-router-dom'
import { BrandMark } from '../components/BrandMark'

const navigation = [
  { label: 'Journey', href: '/' },
]

export function AppShell() {
  return (
    <div className="min-h-screen bg-canvas text-ink">
      <header className="border-b border-line/80 bg-canvas/90 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5 lg:px-10">
          <BrandMark />
          <nav aria-label="Primary navigation" className="flex items-center gap-6">
            {navigation.map((item) => (
              <NavLink
                key={item.href}
                to={item.href}
                className={({ isActive }) =>
                  `text-sm font-semibold transition-colors ${
                    isActive ? 'text-accent' : 'text-muted hover:text-ink'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
            <span className="hidden rounded-full border border-line px-4 py-2 text-xs font-semibold text-muted sm:inline-flex">
              Foundation mode
            </span>
          </nav>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
