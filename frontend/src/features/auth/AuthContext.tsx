import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import { authApi, type AuthResponse, type AuthUser, type LoginInput, type RegisterInput } from '../../services/api'

interface AuthContextValue {
  user: AuthUser | null
  accessToken: string | null
  isLoading: boolean
  login: (input: LoginInput) => Promise<void>
  register: (input: RegisterInput) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function applySession(session: AuthResponse, setUser: (user: AuthUser) => void, setAccessToken: (token: string) => void) {
  setUser(session.user)
  setAccessToken(session.accessToken)
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [accessToken, setAccessToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const restoreSession = useCallback(async () => {
    try {
      const session = await authApi.refresh()
      applySession(session, setUser, setAccessToken)
    } catch {
      setUser(null)
      setAccessToken(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void restoreSession()
  }, [restoreSession])

  const login = useCallback(async (input: LoginInput) => {
    applySession(await authApi.login(input), setUser, setAccessToken)
  }, [])

  const register = useCallback(async (input: RegisterInput) => {
    applySession(await authApi.register(input), setUser, setAccessToken)
  }, [])

  const logout = useCallback(async () => {
    try {
      await authApi.logout(accessToken ?? undefined)
    } finally {
      setUser(null)
      setAccessToken(null)
    }
  }, [accessToken])

  const value = useMemo(
    () => ({ user, accessToken, isLoading, login, register, logout }),
    [accessToken, isLoading, login, logout, register, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export { AuthContext }
