import { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import * as taskApi from '../api/tasks';
import TaskCard from '../components/TaskCard';
import { useGoalContext } from '../context/GoalContext';

const TABS = [
  { key: 'today', label: 'Today' },
  { key: 'upcoming', label: 'Upcoming' },
  { key: 'history', label: 'History' },
];

function formatDateHeading(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('default', { weekday: 'long', month: 'short', day: 'numeric' });
}

export default function Tasks() {
  const { selectedGoalId, loading: goalsLoading } = useGoalContext();
  const [tab, setTab] = useState('today');
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      let data;
      if (tab === 'today') data = await taskApi.getTodayTasks(selectedGoalId);
      else if (tab === 'upcoming') {
        const from = new Date().toISOString().slice(0, 10);
        const toDate = new Date();
        toDate.setDate(toDate.getDate() + 14);
        data = await taskApi.getUpcomingTasks(from, toDate.toISOString().slice(0, 10), selectedGoalId);
      } else data = await taskApi.getHistory(selectedGoalId);
      setTasks(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (goalsLoading) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab, selectedGoalId, goalsLoading]);

  const handleComplete = async (id, effortRating) => { await taskApi.completeTask(id, effortRating); await load(); };
  const handleMiss = async (id) => { await taskApi.markMissed(id); await load(); };

  const showDateHeaders = tab !== 'today';
  const grouped = showDateHeaders
    ? tasks.reduce((acc, t) => { (acc[t.date] = acc[t.date] || []).push(t); return acc; }, {})
    : null;
  // History comes back newest-first from the backend; upcoming comes oldest-first.
  const dates = grouped ? Object.keys(grouped).sort(tab === 'history' ? (a, b) => b.localeCompare(a) : undefined) : [];

  return (
    <div className="px-8 pb-8 pt-2 max-w-3xl mx-auto">
      <h1 className="font-display text-3xl font-bold text-navy-800 mb-6 mt-4">Tasks</h1>

      <div className="flex gap-2 mb-6 bg-navy-50 p-1 rounded-xl w-fit">
        {TABS.map((t) => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`px-4 py-2 rounded-lg text-sm font-semibold transition-smooth ${
              tab === t.key ? 'bg-white text-navy-800 shadow-sm' : 'text-navy-400 hover:text-navy-600'
            }`}>
            {t.label}
          </button>
        ))}
      </div>

      {loading || goalsLoading ? (
        <p className="text-navy-400 text-sm">Loading...</p>
      ) : tasks.length === 0 ? (
        <p className="text-navy-400 text-sm bg-white rounded-2xl p-8 text-center border border-navy-100">
          Nothing here yet.
        </p>
      ) : showDateHeaders ? (
        <div className="space-y-6">
          {dates.map((date, idx) => (
            <motion.div key={date} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: idx * 0.03 }}>
              <h3 className="text-xs font-semibold uppercase tracking-wide text-navy-400 mb-2 px-1">
                {formatDateHeading(date)}
              </h3>
              <div className="space-y-3">
                <AnimatePresence>
                  {grouped[date].map((task) => (
                    <TaskCard key={task.id} task={task} onComplete={handleComplete} onMiss={handleMiss} />
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          ))}
        </div>
      ) : (
        <div className="space-y-3">
          <AnimatePresence>
            {tasks.map((task) => (
              <TaskCard key={task.id} task={task} onComplete={handleComplete} onMiss={handleMiss} />
            ))}
          </AnimatePresence>
        </div>
      )}
    </div>
  );
}
