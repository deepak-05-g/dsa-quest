import { Link } from 'react-router-dom'

export function BrandMark() {
  return (
    <Link to="/" className="flex items-center gap-3" aria-label="DSA QUEST home">
      <span className="grid size-10 place-items-center rounded-xl bg-accent text-lg font-bold text-canvas shadow-glow">
        DQ
      </span>
      <span className="font-display text-lg font-bold tracking-tight">
        DSA <span className="text-accent">QUEST</span>
      </span>
    </Link>
  )
}
