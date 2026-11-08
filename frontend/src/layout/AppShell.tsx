import { NavLink, Outlet } from 'react-router-dom'
import { BrandMark } from '../components/BrandMark'
import { useAuth } from '../features/auth/useAuth'

const navigation = [
  { label: 'Dashboard', href: '/' },
  { label: 'Learning Paths', href: '/foundation', comingSoon: true },
  { label: 'Practice', href: '/foundation', comingSoon: true },
  { label: 'Visualizer', href: '/foundation', comingSoon: true },
  { label: 'Achievements', href: '/foundation', comingSoon: true },
  { label: 'Profile', href: '/foundation', comingSoon: true },
]

export function AppShell() {
  const { user, logout } = useAuth()

  return (
    <div className="min-h-screen bg-canvas text-ink lg:flex">
      <aside className="hidden w-64 shrink-0 border-r border-line/80 bg-canvas/80 p-6 lg:flex lg:flex-col">
        <BrandMark />
        <nav aria-label="Primary navigation" className="mt-12 space-y-2">
          {navigation.map((item, index) => (
            <NavLink
              key={item.label}
              to={item.href}
              className={({ isActive }) =>
                `flex items-center justify-between rounded-2xl px-4 py-3 text-sm font-semibold transition-colors ${
                  isActive && index === 0 ? 'bg-accent/10 text-accent' : 'text-muted hover:bg-panel hover:text-ink'
                }`
              }
            >
              <span>{item.label}</span>
              {item.comingSoon && <span className="text-[10px] uppercase tracking-wider text-muted/70">Soon</span>}
            </NavLink>
          ))}
        </nav>
        <div className="mt-auto rounded-2xl border border-line bg-panel p-4">
          <p className="text-xs font-bold uppercase tracking-wider text-accent">DSA QUEST</p>
          <p className="mt-2 text-xs leading-5 text-muted">Your verified progress lives here. No dashboard visit awards XP.</p>
        </div>
      </aside>
      <div className="min-w-0 flex-1">
        <header className="border-b border-line/80 bg-canvas/90 backdrop-blur">
          <div className="flex items-center justify-between px-5 py-4 sm:px-8 lg:px-10">
            <div className="lg:hidden"><BrandMark /></div>
            <nav aria-label="Mobile navigation" className="flex gap-2 overflow-x-auto lg:hidden">
              <NavLink to="/" className="whitespace-nowrap rounded-full bg-accent/10 px-3 py-2 text-xs font-semibold text-accent">Dashboard</NavLink>
              <span className="whitespace-nowrap rounded-full border border-line px-3 py-2 text-xs font-semibold text-muted">More coming soon</span>
            </nav>
            {user && (
              <button type="button" onClick={() => void logout()} className="rounded-full border border-line px-3 py-2 text-xs font-semibold text-muted transition hover:border-accent hover:text-accent">
                Log out
              </button>
            )}
          </div>
        </header>
        <main><Outlet /></main>
      </div>
    </div>
  )
}
