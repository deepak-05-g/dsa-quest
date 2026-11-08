import { useEffect, useState } from 'react'
import { useAuth } from '../auth/useAuth'
import { ApiError } from '../../services/api'
import { getDashboard, type DashboardData } from './dashboardApi'

function initials(name: string) {
  return name.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric' }).format(new Date(value))
}

function Card({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <section className={`rounded-3xl border border-line bg-panel p-5 shadow-panel ${className}`}>{children}</section>
}

function ProgressBar({ value }: { value: number }) {
  return (
    <div className="h-2 overflow-hidden rounded-full bg-line" aria-label={`${value}% complete`}>
      <div className="h-full rounded-full bg-accent transition-all" style={{ width: `${Math.min(value, 100)}%` }} />
    </div>
  )
}

export function DashboardRoute() {
  const { user, accessToken, isLoading: authLoading } = useAuth()
  const [dashboard, setDashboard] = useState<DashboardData | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!accessToken) {
      setIsLoading(false)
      return
    }
    let active = true
    setIsLoading(true)
    getDashboard(accessToken)
      .then((data) => {
        if (active) setDashboard(data)
      })
      .catch((cause: unknown) => {
        if (active) {
          setError(cause instanceof ApiError ? cause.message : 'Dashboard data could not be loaded.')
        }
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => {
      active = false
    }
  }, [accessToken])

  if (authLoading || isLoading) {
    return <DashboardLoading />
  }
  if (!user || !accessToken) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-24 text-center">
        <p className="text-sm font-bold uppercase tracking-[0.3em] text-accent">Your quest awaits</p>
        <h1 className="mt-4 font-display text-4xl font-bold">Sign in to enter your dashboard.</h1>
        <p className="mt-4 text-muted">Your progress, streaks, and learning path will appear here once you are authenticated.</p>
      </div>
    )
  }
  if (error || !dashboard) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-24 text-center">
        <h1 className="font-display text-3xl font-bold">We could not load your quest.</h1>
        <p className="mt-3 text-muted">{error ?? 'Please try again shortly.'}</p>
      </div>
    )
  }

  const progress = dashboard.xp.xpForNextLevel === 0
    ? 0
    : dashboard.xp.xpProgress / (dashboard.xp.xpForNextLevel - 100 * (dashboard.xp.level - 1) ** 2) * 100

  return (
    <div className="mx-auto max-w-7xl px-5 py-8 sm:px-8 lg:px-10 lg:py-12">
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-bold uppercase tracking-[0.25em] text-accent">Student dashboard</p>
          <h1 className="mt-3 font-display text-4xl font-bold tracking-tight sm:text-5xl">
            Welcome back, {dashboard.user.fullName.split(' ')[0]}.
          </h1>
          <p className="mt-3 max-w-xl text-muted">Build momentum one concept at a time. Your next breakthrough is closer than it looks.</p>
        </div>
        <div className="flex items-center gap-3 rounded-2xl border border-line bg-panel px-4 py-3">
          <span className="grid h-10 w-10 place-items-center rounded-full bg-accent font-bold text-canvas">{initials(dashboard.user.fullName)}</span>
          <div><p className="text-sm font-semibold">{dashboard.user.fullName}</p><p className="text-xs text-muted">{dashboard.user.role}</p></div>
        </div>
      </div>

      <div className="grid gap-5 lg:grid-cols-[1.35fr_1fr_1fr]">
        <Card className="bg-gradient-to-br from-panel to-[hsl(222_40%_19%)]">
          <div className="flex items-start justify-between"><div><p className="text-sm text-muted">Current level</p><p className="mt-2 font-display text-5xl font-bold">{dashboard.xp.level}</p></div><span className="rounded-full bg-accent/15 px-3 py-1 text-xs font-bold text-accent">LEVEL UP</span></div>
          <div className="mt-8 flex items-center justify-between text-sm"><span>{dashboard.xp.totalXp} XP earned</span><span className="text-muted">{dashboard.xp.xpForNextLevel} XP next</span></div>
          <div className="mt-3"><ProgressBar value={progress} /></div>
        </Card>
        <Card><p className="text-sm text-muted">Daily streak</p><div className="mt-3 flex items-end gap-2"><span className="font-display text-5xl font-bold text-accent">{dashboard.streak.currentDays}</span><span className="pb-2 text-muted">days</span></div><p className="mt-4 text-sm text-muted">{dashboard.streak.activeToday ? 'Activity recorded today.' : 'No verified activity today.'}</p><p className="mt-2 text-xs text-muted">Best: {dashboard.streak.longestDays} days</p></Card>
        <Card><p className="text-sm text-muted">Learning progress</p><p className="mt-3 font-display text-5xl font-bold">{dashboard.learning.completionPercent}%</p><div className="mt-5"><ProgressBar value={dashboard.learning.completionPercent} /></div><p className="mt-3 text-sm text-muted">{dashboard.learning.completedLessons} of {dashboard.learning.publishedLessons} published lessons</p></Card>
      </div>

      <div className="mt-5 grid gap-5 lg:grid-cols-[1.15fr_0.85fr]">
        <Card><div className="flex items-center justify-between"><div><p className="text-sm font-bold uppercase tracking-[0.18em] text-accent">Continue learning</p><h2 className="mt-2 font-display text-2xl font-bold">{dashboard.continueLearning?.lessonTitle ?? 'Your learning journey is being prepared.'}</h2></div><span className="text-3xl text-accent">→</span></div><p className="mt-3 text-muted">{dashboard.continueLearning ? `${dashboard.continueLearning.topic} · Last opened ${formatDate(dashboard.continueLearning.lastAccessedAt)}` : 'Published lessons and personalized paths will appear here when available.'}</p></Card>
        <Card><p className="text-sm font-bold uppercase tracking-[0.18em] text-accent">Achievements</p>{dashboard.achievements.length === 0 ? <p className="mt-6 text-muted">Achievements will appear after you complete verified milestones.</p> : <ul className="mt-5 space-y-4">{dashboard.achievements.map((achievement) => <li key={`${achievement.title}-${achievement.earnedAt}`}><p className="font-semibold">{achievement.title}</p><p className="text-sm text-muted">{achievement.description}</p></li>)}</ul>}</Card>
      </div>

      <Card className="mt-5"><p className="text-sm font-bold uppercase tracking-[0.18em] text-accent">Recent activity</p>{dashboard.recentActivity.length === 0 ? <p className="mt-6 text-muted">No learning activity yet. Complete a verified lesson or challenge to start your timeline.</p> : <ul className="mt-5 grid gap-4 sm:grid-cols-2">{dashboard.recentActivity.map((activity) => <li key={`${activity.occurredAt}-${activity.description}`} className="rounded-2xl border border-line p-4"><div className="flex justify-between gap-3"><p className="font-semibold">{activity.description}</p><span className="text-sm text-accent">+{activity.xpEarned} XP</span></div><p className="mt-2 text-xs text-muted">{formatDate(activity.occurredAt)}</p></li>)}</ul>}</Card>
    </div>
  )
}

function DashboardLoading() {
  return <div className="mx-auto max-w-7xl animate-pulse px-5 py-12 sm:px-8 lg:px-10"><div className="h-12 w-2/3 rounded-xl bg-panel" /><div className="mt-8 grid gap-5 lg:grid-cols-3">{[1, 2, 3].map((item) => <div key={item} className="h-52 rounded-3xl bg-panel" />)}</div></div>
}
