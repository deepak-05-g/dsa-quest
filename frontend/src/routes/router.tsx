import { createBrowserRouter } from 'react-router-dom'
import { App } from '../App'
import { RootRoute } from './RootRoute'
import { DashboardRoute } from '../features/dashboard/DashboardRoute'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [{ index: true, element: <DashboardRoute /> }, { path: 'foundation', element: <RootRoute /> }],
  },
])
