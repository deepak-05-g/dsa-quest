import { createBrowserRouter } from 'react-router-dom'
import { App } from '../App'
import { RootRoute } from './RootRoute'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [{ index: true, element: <RootRoute /> }],
  },
])
