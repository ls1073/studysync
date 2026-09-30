/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#F5F7FC',
          100: '#E8ECF6',
          200: '#D2D9EA',
          300: '#AEB8D0',
          400: '#7C8AA8',
          500: '#4A5875',
          600: '#2D3A52',
          700: '#1E2738',
          800: '#131A28',
          900: '#0B0F17',
        },
        accent: {
          400: '#7B6EF6',
          500: '#6250F0',
          600: '#4E3AD9',
        },
        ice: '#E3DFFC',
        surface: '#F7F7FC',
      },
      fontFamily: {
        display: ['"Plus Jakarta Sans"', '"Segoe UI"', 'sans-serif'],
        body: ['"Inter"', '"Segoe UI"', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 8px 30px -8px rgba(11, 15, 23, 0.18)',
        card: '0 2px 14px -4px rgba(11, 15, 23, 0.10)',
        glow: '0 0 0 4px rgba(98, 80, 240, 0.15)',
      },
      borderRadius: {
        xl2: '1.25rem',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: 0, transform: 'translateY(6px)' },
          '100%': { opacity: 1, transform: 'translateY(0)' },
        },
        pulseSoft: {
          '0%, 100%': { opacity: 1 },
          '50%': { opacity: 0.6 },
        },
      },
      animation: {
        fadeIn: 'fadeIn 0.35s ease-out',
        pulseSoft: 'pulseSoft 2s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
