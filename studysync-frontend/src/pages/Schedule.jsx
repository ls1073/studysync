import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Coffee, CheckCircle2, XCircle, Circle } from 'lucide-react';
import * as taskApi from '../api/tasks';
import { formatTimeRange12h } from '../utils/format';
import { useGoalContext } from '../context/GoalContext';

const statusIcon = {
  DONE: <CheckCircle2 size={15} className="text-green-500" />,
  MISSED: <XCircle size={15} className="text-red-500" />,
  PENDING: <Circle size={15} className="text-navy-300" />,
};

function formatDateHeading(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('default', { weekday: 'long', month: 'short', day: 'numeric' });
}

export default function Schedule() {
  const { selectedGoalId, activeGoals, loading: goalsLoading } = useGoalContext();
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [rangeDays, setRangeDays] = useState(14);

  const load = async (days) => {
    setLoading(true);
    try {
      const from = new Date().toISOString().slice(0, 10);
      const toDate = new Date();
      toDate.setDate(toDate.getDate() + days);
      const to = toDate.toISOString().slice(0, 10);
      const data = await taskApi.getUpcomingTasks(from, to, selectedGoalId);
      setTasks(data);
      setError('');
    } catch {
      setError('Some data failed to load. Check that the backend is running and try refreshing.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (goalsLoading) return;
    load(rangeDays);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rangeDays, selectedGoalId, goalsLoading]);

  const grouped = tasks.reduce((acc, t) => {
    acc[t.date] = acc[t.date] || [];
    acc[t.date].push(t);
    return acc;
  }, {});
  const dates = Object.keys(grouped).sort();

  return (
    <div className="px-8 pb-8 pt-2 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-2 mt-4">
        <div>
          <h1 className="font-display text-3xl font-bold text-navy-800">Full Timetable</h1>
          <p className="text-navy-400 text-sm mt-1">Your complete hour-by-hour schedule, table view.</p>
        </div>
        <select value={rangeDays} onChange={(e) => setRangeDays(parseInt(e.target.value, 10))}
          className="px-3 py-2 rounded-xl border border-navy-100 text-sm outline-none focus:border-accent-500 bg-white">
          <option value={7}>Next 7 days</option>
          <option value={14}>Next 14 days</option>
          <option value={30}>Next 30 days</option>
        </select>
      </div>

      {error && (
        <div className="mt-4 text-sm text-red-600 bg-red-50 px-4 py-3 rounded-xl border border-red-100">
          {error}
        </div>
      )}

      {activeGoals.length === 0 && !goalsLoading && (
        <p className="text-navy-400 text-sm bg-white rounded-2xl p-6 text-center border border-navy-100 mt-6">
          No active goals yet -- create one from the Goals page to generate your timetable.
        </p>
      )}

      {loading || goalsLoading ? (
        <p className="text-navy-400 text-sm mt-6">Loading...</p>
      ) : (
        <div className="space-y-6 mt-6">
          {dates.length === 0 && activeGoals.length > 0 && (
            <p className="text-navy-400 text-sm bg-white rounded-2xl p-6 text-center border border-navy-100">
              No tasks scheduled in this range for this goal.
            </p>
          )}
          {dates.map((date, idx) => (
            <motion.div key={date} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: idx * 0.03 }}
              className="bg-white rounded-2xl border border-navy-100 shadow-card overflow-hidden">
              <div className="px-5 py-3 bg-navy-800 text-white font-semibold text-sm">
                {formatDateHeading(date)}
              </div>
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-navy-400 text-xs border-b border-navy-50">
                    <th className="px-5 py-2 font-medium">Time</th>
                    <th className="px-5 py-2 font-medium">Task</th>
                    <th className="px-5 py-2 font-medium">Duration</th>
                    <th className="px-5 py-2 font-medium text-right">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {grouped[date].map((t) => (
                    <tr key={t.id} className={`border-b border-navy-50 last:border-0 ${t.isRestBlock ? 'bg-accent-500/5' : ''}`}>
                      <td className="px-5 py-3 text-navy-500 whitespace-nowrap">{formatTimeRange12h(t.startTime, t.endTime)}</td>
                      <td className="px-5 py-3 text-navy-800 font-medium">
                        <span className="flex items-center gap-2">
                          {t.isRestBlock && <Coffee size={14} className="text-accent-500" />}
                          {t.title}
                        </span>
                      </td>
                      <td className="px-5 py-3 text-navy-500">{t.allocatedHours}h</td>
                      <td className="px-5 py-3 text-right">
                        {t.isRestBlock ? (
                          <span className="text-xs text-navy-400">Rest</span>
                        ) : (
                          <span className="inline-flex items-center gap-1 justify-end">
                            {statusIcon[t.status]}
                            <span className="text-xs text-navy-500">{t.status}</span>
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
