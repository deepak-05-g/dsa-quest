import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        canvas: 'hsl(var(--canvas))',
        ink: 'hsl(var(--ink))',
        muted: 'hsl(var(--muted))',
        panel: 'hsl(var(--panel))',
        line: 'hsl(var(--line))',
        accent: 'hsl(var(--accent))',
        'accent-strong': 'hsl(var(--accent-strong))',
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        sans: ['"Inter"', 'sans-serif'],
      },
    },
  },
  plugins: [],
} satisfies Config
