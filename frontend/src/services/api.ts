const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export type UserRole = 'STUDENT' | 'ADMIN'

export interface AuthUser {
  id: string
  fullName: string
  email: string
  role: UserRole
}

export interface AuthResponse {
  accessToken: string
  tokenType: 'Bearer'
  accessTokenExpiresAt: string
  user: AuthUser
}

export interface RegisterInput {
  fullName: string
  email: string
  password: string
  passwordConfirmation: string
}

export interface LoginInput {
  email: string
  password: string
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message)
  }
}

function csrfToken(): string | undefined {
  return document.cookie
    .split('; ')
    .find((cookie) => cookie.startsWith('XSRF-TOKEN='))
    ?.split('=')[1]
}

export async function request<T>(
  path: string,
  options: RequestInit = {},
  accessToken?: string,
): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  const token = csrfToken()
  if (token) {
    headers.set('X-XSRF-TOKEN', decodeURIComponent(token))
  }
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    headers,
    credentials: 'include',
  })
  if (!response.ok) {
    const body = (await response.json().catch(() => undefined)) as
      | { message?: string }
      | undefined
    throw new ApiError(body?.message ?? 'The request could not be completed', response.status)
  }
  if (response.status === 204) {
    return undefined as T
  }
  return response.json() as Promise<T>
}

export const authApi = {
  register: (input: RegisterInput) =>
    request<AuthResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  login: (input: LoginInput) =>
    request<AuthResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(input),
    }),
  refresh: () =>
    request<AuthResponse>('/api/v1/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({}),
    }),
  logout: (accessToken?: string) =>
    request<void>('/api/v1/auth/logout', { method: 'POST', body: JSON.stringify({}) }, accessToken),
  me: (accessToken: string) => request<AuthUser>('/api/v1/auth/me', { method: 'GET' }, accessToken),
}
