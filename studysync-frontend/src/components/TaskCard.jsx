import { motion } from 'framer-motion';
import { Check, Clock, AlertTriangle, Coffee } from 'lucide-react';
import { useState } from 'react';
import { formatTimeRange12h } from '../utils/format';

const statusStyles = {
  PENDING: 'border-navy-100 bg-white',
  DONE: 'border-green-200 bg-green-50/60',
  MISSED: 'border-red-200 bg-red-50/60',
};

export default function TaskCard({ task, onComplete, onMiss }) {
  const [showRating, setShowRating] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleComplete = async (rating) => {
    setSubmitting(true);
    try {
      await onComplete(task.id, rating);
    } finally {
      setSubmitting(false);
      setShowRating(false);
    }
  };

  const handleMissClick = async () => {
    setSubmitting(true);
    try {
      await onMiss(task.id);
    } finally {
      setSubmitting(false);
    }
  };

  if (task.isRestBlock) {
    return (
      <motion.div
        layout initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, x: -20 }}
        transition={{ duration: 0.25 }}
        className="rounded-2xl border border-dashed border-accent-200 bg-accent-500/5 p-4"
      >
        <div className="flex items-center gap-3">
          <span className="w-9 h-9 rounded-full bg-accent-500/15 text-accent-600 flex items-center justify-center shrink-0">
            <Coffee size={16} />
          </span>
          <div>
            <p className="text-xs text-navy-400 flex items-center gap-1">
              <Clock size={12} /> {formatTimeRange12h(task.startTime, task.endTime)} &middot; {task.allocatedHours}h
            </p>
            <p className="font-semibold text-navy-700 mt-0.5">Rest & Recharge</p>
          </div>
        </div>
      </motion.div>
    );
  }

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, x: -20 }}
      transition={{ duration: 0.25 }}
      className={`rounded-2xl border p-4 shadow-card transition-smooth ${statusStyles[task.status]}`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <p className="text-xs text-navy-400 flex items-center gap-1">
            <Clock size={12} /> {formatTimeRange12h(task.startTime, task.endTime)} &middot; {task.allocatedHours}h
          </p>
          <p className="font-semibold text-navy-800 mt-1 truncate">{task.title}</p>
          <p className="text-xs text-navy-400 mt-1">{task.goalTitle}</p>
          {task.isRegenerated && (
            <span className="inline-block mt-2 text-[10px] font-semibold uppercase tracking-wide text-accent-600 bg-accent-500/10 px-2 py-0.5 rounded-full">
              Regenerated
            </span>
          )}
        </div>

        {task.status === 'PENDING' && !showRating && (
          <div className="flex gap-2 shrink-0">
            <motion.button
                whileTap={{ scale: 0.9 }}
                onClick={() => setShowRating(true)}
                disabled={submitting}
                className="w-9 h-9 rounded-full bg-green-500 text-white flex items-center justify-center hover:bg-green-600 transition-smooth disabled:opacity-60"
                title="Mark done"
            >
              <Check size={16} />
            </motion.button>
            <motion.button
                whileTap={{ scale: 0.9 }}
                onClick={handleMissClick}
                disabled={submitting}
                className="w-9 h-9 rounded-full bg-red-100 text-red-600 flex items-center justify-center hover:bg-red-200 transition-smooth disabled:opacity-60"
                title="Mark missed"
            >
              {submitting ? (
                  <motion.div animate={{ rotate: 360 }} transition={{ repeat: Infinity, duration: 0.8, ease: 'linear' }}
                              className="w-3.5 h-3.5 border-2 border-red-600 border-t-transparent rounded-full" />
              ) : (
                  <AlertTriangle size={16} />
              )}
            </motion.button>
          </div>
        )}

        {task.status === 'DONE' && (
          <span className="w-9 h-9 rounded-full bg-green-500 text-white flex items-center justify-center shrink-0">
            <Check size={16} />
          </span>
        )}
        {task.status === 'MISSED' && (
          <span className="w-9 h-9 rounded-full bg-red-500 text-white flex items-center justify-center shrink-0">
            <AlertTriangle size={16} />
          </span>
        )}
      </div>

      {showRating && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="mt-3 pt-3 border-t border-navy-100"
        >
          <p className="text-xs text-navy-500 mb-2">How hard was this task?</p>
          <div className="flex items-center justify-between gap-1.5">
            {[
              { n: 1, label: 'Very Easy' },
              { n: 2, label: 'Easy' },
              { n: 3, label: 'Moderate' },
              { n: 4, label: 'Hard' },
              { n: 5, label: 'Very Hard' },
            ].map(({ n, label }) => (
                <button
                    key={n}
                    onClick={() => handleComplete(n)}
                    disabled={submitting}
                    title={label}
                    className="flex flex-col items-center gap-1 group disabled:opacity-50"
                >
                <span className="w-8 h-8 rounded-full text-xs font-semibold bg-navy-50 group-hover:bg-accent-500 group-hover:text-white text-navy-600 transition-smooth flex items-center justify-center">
                  {n}
                </span>
                <span className="text-[9px] text-navy-400 group-hover:text-accent-600 transition-smooth whitespace-nowrap">{label}</span>
              </button>
            ))}
          </div>
        </motion.div>
      )}
    </motion.div>
  );
}
