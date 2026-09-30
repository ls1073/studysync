import { motion } from 'framer-motion';

const statusConfig = {
  LIGHT: { color: '#22c55e', label: 'Light', bg: 'bg-green-50', text: 'text-green-700' },
  MODERATE: { color: '#6250F0', label: 'Moderate', bg: 'bg-accent-500/10', text: 'text-accent-600' },
  HIGH: { color: '#f59e0b', label: 'High', bg: 'bg-amber-50', text: 'text-amber-700' },
  OVERLOADED: { color: '#ef4444', label: 'Overloaded', bg: 'bg-red-50', text: 'text-red-700' },
};

export default function LoadMeter({ score = 0, threshold = 65, status = 'LIGHT' }) {
  const cfg = statusConfig[status] || statusConfig.LIGHT;
  const circumference = 2 * Math.PI * 54;
  const offset = circumference - (Math.min(100, score) / 100) * circumference;

  return (
    <div className="flex items-center gap-6">
      <div className="relative w-32 h-32">
        <svg className="w-32 h-32 -rotate-90" viewBox="0 0 120 120">
          <circle cx="60" cy="60" r="54" fill="none" stroke="#F5F7FC" strokeWidth="10" />
          <motion.circle
            cx="60" cy="60" r="54" fill="none"
            stroke={cfg.color}
            strokeWidth="10"
            strokeLinecap="round"
            strokeDasharray={circumference}
            initial={{ strokeDashoffset: circumference }}
            animate={{ strokeDashoffset: offset }}
            transition={{ duration: 1, ease: 'easeOut' }}
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <motion.span
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.3 }}
            className="text-2xl font-bold text-navy-800"
          >
            {Math.round(score)}
          </motion.span>
          <span className="text-xs text-navy-500">/ 100</span>
        </div>
      </div>

      <div>
        <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${cfg.bg} ${cfg.text}`}>
          {cfg.label}
        </span>
        <p className="text-sm text-navy-500 mt-2">Your threshold: <span className="font-semibold text-navy-800">{Math.round(threshold)}</span></p>
        <p className="text-xs text-navy-400 mt-1 max-w-[180px]">
          Adjusts automatically as StudySync learns your capacity.
        </p>
      </div>
    </div>
  );
}
