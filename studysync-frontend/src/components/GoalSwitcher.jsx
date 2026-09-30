import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, Target, CheckCircle2 } from 'lucide-react';
import { useGoalContext } from '../context/GoalContext';

export default function GoalSwitcher() {
  const { goals, selectedGoalId, setSelectedGoalId, primaryGoalId, selectedGoal, loading } = useGoalContext();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (loading) return null;
  if (goals.length === 0) return null;

  const activeGoals = goals.filter((g) => g.status === 'ACTIVE');
  const otherGoals = goals.filter((g) => g.status !== 'ACTIVE');

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex items-center gap-2 bg-white border border-navy-100 rounded-xl px-4 py-2 text-sm font-semibold text-navy-800 hover:border-accent-300 transition-smooth shadow-card"
      >
        <Target size={15} className="text-accent-500" />
        <span className="max-w-[180px] truncate">{selectedGoal?.title || 'Select a goal'}</span>
        {selectedGoalId === primaryGoalId && (
          <span className="text-[10px] font-semibold uppercase tracking-wide text-accent-600 bg-accent-500/10 px-1.5 py-0.5 rounded-full">
            Primary
          </span>
        )}
        <ChevronDown size={14} className={`text-navy-400 transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: -6, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -6, scale: 0.97 }}
            transition={{ duration: 0.15 }}
            className="absolute left-0 mt-2 w-72 bg-white rounded-2xl shadow-soft border border-navy-100 z-50 overflow-hidden"
          >
            {activeGoals.length > 0 && (
              <div>
                <p className="px-4 pt-3 pb-1 text-[10px] font-semibold uppercase tracking-wide text-navy-300">Active</p>
                {activeGoals.map((g) => (
                  <button
                    key={g.id}
                    onClick={() => { setSelectedGoalId(g.id); setOpen(false); }}
                    className={`w-full text-left px-4 py-2.5 flex items-center justify-between hover:bg-navy-50 transition-smooth ${
                      g.id === selectedGoalId ? 'bg-accent-500/5' : ''
                    }`}
                  >
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-navy-800 truncate">{g.title}</p>
                      <p className="text-xs text-navy-400">{g.startDate} &rarr; {g.targetDate}</p>
                    </div>
                    {g.id === selectedGoalId && <CheckCircle2 size={16} className="text-accent-500 shrink-0" />}
                  </button>
                ))}
              </div>
            )}

            {otherGoals.length > 0 && (
              <div className="border-t border-navy-50">
                <p className="px-4 pt-3 pb-1 text-[10px] font-semibold uppercase tracking-wide text-navy-300">Completed / Other</p>
                {otherGoals.map((g) => (
                  <button
                    key={g.id}
                    onClick={() => { setSelectedGoalId(g.id); setOpen(false); }}
                    className={`w-full text-left px-4 py-2.5 flex items-center justify-between hover:bg-navy-50 transition-smooth ${
                      g.id === selectedGoalId ? 'bg-accent-500/5' : ''
                    }`}
                  >
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-navy-500 truncate">{g.title}</p>
                      <p className="text-xs text-navy-300">{g.status}</p>
                    </div>
                    {g.id === selectedGoalId && <CheckCircle2 size={16} className="text-accent-500 shrink-0" />}
                  </button>
                ))}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
