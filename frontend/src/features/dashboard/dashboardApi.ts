import { request } from '../../services/api'

export interface DashboardData {
  user: {
    id: string
    fullName: string
    email: string
    role: 'STUDENT' | 'ADMIN'
  }
  xp: {
    totalXp: number
    level: number
    xpForNextLevel: number
    xpProgress: number
  }
  streak: {
    currentDays: number
    longestDays: number
    activeToday: boolean
  }
  learning: {
    completedLessons: number
    publishedLessons: number
    completionPercent: number
  }
  continueLearning: {
    lessonTitle: string
    topic: string
    lastAccessedAt: string
  } | null
  achievements: Array<{
    title: string
    description: string
    earnedAt: string
  }>
  recentActivity: Array<{
    type: string
    description: string
    xpEarned: number
    occurredAt: string
  }>
}

export function getDashboard(accessToken: string) {
  return request<DashboardData>('/api/v1/dashboard', { method: 'GET' }, accessToken)
}
