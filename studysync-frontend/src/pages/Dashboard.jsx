import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Sparkles, CalendarX, CheckCircle2 } from 'lucide-react';
import * as dashboardApi from '../api/dashboard';
import * as taskApi from '../api/tasks';
import TaskCard from '../components/TaskCard';
import GoalCard from '../components/GoalCard';
import LoadMeter from '../components/LoadMeter';
import NotificationsPanel from '../components/NotificationsPanel';
import { useGoalContext } from '../context/GoalContext';

export default function Dashboard() {
  const { selectedGoalId, loading: goalsLoading } = useGoalContext();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [confirmMissDay, setConfirmMissDay] = useState(false);
  const [missingDay, setMissingDay] = useState(false);

  const load = async () => {
    try {
      const d = await dashboardApi.getDashboard(selectedGoalId);
      setData(d);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load dashboard. Check that the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (goalsLoading) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedGoalId, goalsLoading]);

  const handleComplete = async (taskId, effortRating) => {
    await taskApi.completeTask(taskId, effortRating);
    await load();
  };

  const handleMiss = async (taskId) => {
    await taskApi.markMissed(taskId);
    await load();
  };

  const handleMissWholeDay = async () => {
    setMissingDay(true);
    try {
      const today = new Date().toISOString().slice(0, 10);
      await taskApi.markDayMissed(today);
      setConfirmMissDay(false);
      await load();
    } finally {
      setMissingDay(false);
    }
  };

  const missedTasks = data?.todayTasks?.filter((t) => t.status === 'MISSED') || [];
  const completedTasks = data?.todayTasks?.filter((t) => t.status === 'DONE') || [];
  const remainingTasks = data?.todayTasks?.filter((t) => t.status === 'PENDING' || t.isRestBlock) || [];
  const pendingRealTasks = data?.todayTasks?.filter((t) => t.status === 'PENDING' && !t.isRestBlock) || [];

  if (loading || goalsLoading) {
    return (
      <div className="flex items-center justify-center h-[60vh]">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ repeat: Infinity, duration: 1, ease: 'linear' }}
          className="w-8 h-8 border-4 border-accent-500 border-t-transparent rounded-full"
        />
      </div>
    );
  }

  return (
    <div className="px-8 pb-8 pt-2 max-w-6xl mx-auto">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="flex items-center justify-between mb-8 mt-4">
        <div>
          <h1 className="font-display text-3xl font-bold text-navy-800">Today</h1>
          <p className="text-navy-400 text-sm mt-1">Here's your plan for today.</p>
        </div>
        <NotificationsPanel unreadCount={data?.unreadNotifications || 0} onRead={load} />
      </motion.div>

      {error && (
        <div className="mb-6 text-sm text-red-600 bg-red-50 px-4 py-3 rounded-xl border border-red-100">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <motion.div
          initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}
          className="lg:col-span-2 bg-white rounded-2xl shadow-card border border-navy-100 p-6"
        >
          <h2 className="font-semibold text-navy-800 mb-4 flex items-center gap-2">
            <Sparkles size={18} className="text-accent-500" /> Mental Load Balancer
          </h2>
          {data?.mentalLoad && <LoadMeter {...{
            score: data.mentalLoad.loadScore,
            threshold: data.mentalLoad.threshold,
            status: data.mentalLoad.status,
          }} />}
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }}
          className="bg-gradient-to-br from-navy-800 to-accent-600 rounded-2xl shadow-card p-6 text-white flex flex-col justify-center"
        >
          <p className="text-white/70 text-sm">Overall Completion</p>
          <p className="text-4xl font-bold mt-1">{data?.overallCompletionRate ?? 0}%</p>
          <p className="text-white/60 text-xs mt-2">across all active goals</p>
        </motion.div>
      </div>

      {missedTasks.length > 0 && (
        <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="mb-6">
          <h2 className="font-semibold text-red-600 mb-3 flex items-center gap-2 text-sm">
            <CalendarX size={16} /> Missed ({missedTasks.length})
          </h2>
          <div className="space-y-2">
            {missedTasks.map((task) => (
              <div key={task.id} className="rounded-xl border border-red-100 bg-red-50/60 px-4 py-3 flex items-center justify-between">
                <div>
                  <p className="text-xs text-red-400">{task.startTime} - {task.endTime}</p>
                  <p className="font-medium text-red-700 text-sm">{task.title}</p>
                </div>
                <span className="text-xs font-semibold text-red-500 uppercase">Missed</span>
              </div>
            ))}
          </div>
        </motion.div>
      )}

      {completedTasks.length > 0 && (
        <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="mb-6">
          <h2 className="font-semibold text-green-600 mb-3 flex items-center gap-2 text-sm">
            <CheckCircle2 size={16} /> Completed ({completedTasks.length})
          </h2>
          <div className="space-y-2">
            {completedTasks.map((task) => (
              <div key={task.id} className="rounded-xl border border-green-100 bg-green-50/60 px-4 py-3 flex items-center justify-between">
                <div>
                  <p className="text-xs text-green-500">{task.startTime} - {task.endTime}</p>
                  <p className="font-medium text-green-700 text-sm">{task.title}</p>
                </div>
                <span className="text-xs font-semibold text-green-600 uppercase">Done</span>
              </div>
            ))}
          </div>
        </motion.div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-navy-800">Today's Tasks</h2>
            {pendingRealTasks.length > 0 && (
              <button
                onClick={() => setConfirmMissDay(true)}
                className="flex items-center gap-1.5 text-xs font-semibold text-red-500 hover:text-red-600 transition-smooth"
              >
                <CalendarX size={14} /> Mark whole day missed
              </button>
            )}
          </div>

          <div className="space-y-3">
            <AnimatePresence>
              {remainingTasks.length ? (
                remainingTasks.map((task) => (
                  <TaskCard key={task.id} task={task} onComplete={handleComplete} onMiss={handleMiss} />
                ))
              ) : (
                <p className="text-navy-400 text-sm bg-white rounded-2xl p-6 text-center border border-navy-100">
                  No tasks scheduled today for this goal.
                </p>
              )}
            </AnimatePresence>
          </div>
        </div>

        <div>
          <h2 className="font-semibold text-navy-800 mb-4">Active Goals</h2>
          <div className="space-y-3">
            {data?.activeGoals?.length ? (
              data.activeGoals.map((goal) => <GoalCard key={goal.id} goal={goal} />)
            ) : (
              <p className="text-navy-400 text-sm bg-white rounded-2xl p-6 text-center border border-navy-100">
                No active goals yet.
              </p>
            )}
          </div>
        </div>
      </div>

      <AnimatePresence>
        {confirmMissDay && (
          <motion.div
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 bg-navy-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
            onClick={() => setConfirmMissDay(false)}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6"
            >
              <h3 className="font-semibold text-navy-800 text-lg mb-2">Mark whole day as missed?</h3>
              <p className="text-sm text-navy-500 mb-6">
                This marks all {pendingRealTasks.length} remaining task(s) today as missed and regenerates your
                schedule with a lighter pace for the next few days. This can't be undone from the app.
              </p>
              <div className="flex gap-3">
                <button onClick={() => setConfirmMissDay(false)}
                  className="flex-1 py-2.5 rounded-xl border border-navy-100 text-navy-600 font-medium hover:bg-navy-50 transition-smooth">
                  Cancel
                </button>
                <button onClick={handleMissWholeDay} disabled={missingDay}
                        className="flex-1 py-2.5 rounded-xl bg-red-500 text-white font-semibold hover:bg-red-600 transition-smooth disabled:opacity-60">
                  {missingDay ? 'Marking...' : 'Yes, mark missed'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
