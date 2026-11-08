import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { DashboardRoute } from './DashboardRoute'
import { getDashboard } from './dashboardApi'
import { useAuth } from '../auth/useAuth'
import type { DashboardData } from './dashboardApi'

vi.mock('../auth/useAuth', () => ({ useAuth: vi.fn() }))
vi.mock('./dashboardApi', () => ({ getDashboard: vi.fn() }))

const mockedUseAuth = vi.mocked(useAuth)
const mockedGetDashboard = vi.mocked(getDashboard)

const user = {
  id: 'student-1',
  fullName: 'Ada Lovelace',
  email: 'ada@example.com',
  role: 'STUDENT' as const,
}

const dashboard: DashboardData = {
  user,
  xp: { totalXp: 100, level: 2, xpForNextLevel: 400, xpProgress: 0 },
  streak: { currentDays: 3, longestDays: 5, activeToday: true },
  learning: { completedLessons: 0, publishedLessons: 0, completionPercent: 0 },
  continueLearning: null,
  achievements: [],
  recentActivity: [],
}

function setAuth(overrides: Partial<ReturnType<typeof useAuth>> = {}) {
  mockedUseAuth.mockReturnValue({
    user: null,
    accessToken: null,
    isLoading: false,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    ...overrides,
  })
}

describe('DashboardRoute', () => {
  afterEach(() => {
    cleanup()
  })

  beforeEach(() => {
    vi.clearAllMocks()
    mockedGetDashboard.mockResolvedValue(dashboard)
  })

  it('renders the loading state while authentication is restoring', () => {
    setAuth({ isLoading: true })

    render(<DashboardRoute />)

    expect(document.querySelector('.animate-pulse')).toBeInTheDocument()
  })

  it('renders the protected empty state without an access token', () => {
    setAuth()

    render(<DashboardRoute />)

    expect(screen.getByText('Sign in to enter your dashboard.')).toBeInTheDocument()
    expect(mockedGetDashboard).not.toHaveBeenCalled()
  })

  it('renders dashboard metrics and empty sections after a successful response', async () => {
    setAuth({ user, accessToken: 'access-token' })

    render(<DashboardRoute />)

    expect(await screen.findByText('Welcome back, Ada.')).toBeInTheDocument()
    expect(screen.getByText('100 XP earned')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(screen.getByText('Achievements will appear after you complete verified milestones.')).toBeInTheDocument()
    expect(screen.getByText('No learning activity yet. Complete a verified lesson or challenge to start your timeline.')).toBeInTheDocument()
  })

  it('renders API failures explicitly', async () => {
    setAuth({ user, accessToken: 'access-token' })
    mockedGetDashboard.mockRejectedValue(new Error('network unavailable'))

    render(<DashboardRoute />)

    expect(await screen.findByText('Dashboard data could not be loaded.')).toBeInTheDocument()
  })

  it('does not expose dashboard data while authentication is absent', async () => {
    setAuth({ user: null, accessToken: null })

    render(<DashboardRoute />)

    await waitFor(() => expect(screen.queryByText('Welcome back, Ada.')).not.toBeInTheDocument())
    expect(screen.getByText('Your progress, streaks, and learning path will appear here once you are authenticated.')).toBeInTheDocument()
  })
})
