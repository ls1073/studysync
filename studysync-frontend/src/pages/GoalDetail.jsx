import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowLeft, CheckCircle2, Circle, FileDown, Lightbulb, Trash2 } from 'lucide-react';
import * as goalsApi from '../api/goals';
import * as reportsApi from '../api/reports';
import { useGoalContext } from '../context/GoalContext';

const statusStyles = {
  ACTIVE: 'bg-accent-500/10 text-accent-600',
  COMPLETED: 'bg-green-100 text-green-700',
  ABANDONED: 'bg-slate-100 text-slate-500',
};

const importanceLabel = { 1: 'Low', 2: 'Medium', 3: 'High' };
const importanceStyle = {
  1: 'bg-slate-100 text-slate-500',
  2: 'bg-accent-500/10 text-accent-600',
  3: 'bg-amber-100 text-amber-700',
};

export default function GoalDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { refreshGoals } = useGoalContext();
  const [goal, setGoal] = useState(null);
  const [loading, setLoading] = useState(true);
  const [report, setReport] = useState(null);
  const [reportLoading, setReportLoading] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    goalsApi.getGoal(id).then(setGoal).finally(() => setLoading(false));
  }, [id]);

  const handleLoadReport = async () => {
    if (report) { setReport(null); return; }
    setReportLoading(true);
    try {
      const r = await reportsApi.getGoalReport(id);
      setReport(r);
    } finally {
      setReportLoading(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await goalsApi.deleteGoal(id);
      await refreshGoals();
      navigate('/goals');
    } finally {
      setDeleting(false);
    }
  };

  if (loading) return <div className="p-8 text-navy-400">Loading...</div>;
  if (!goal) return <div className="p-8 text-navy-400">Goal not found.</div>;

  return (
    <div className="p-8 max-w-4xl mx-auto" id="goal-report-content">
      <style>{`
        @media print {
          aside { display: none !important; }
          #goal-report-content { padding: 0 !important; max-width: 100% !important; }
          .no-print { display: none !important; }
        }
      `}</style>

      <div className="no-print flex items-center justify-between mb-6">
        <Link to="/goals" className="flex items-center gap-2 text-sm text-navy-400 hover:text-navy-700 transition-smooth">
          <ArrowLeft size={16} /> Back to goals
        </Link>
        <button onClick={() => setConfirmDelete(true)}
          className="flex items-center gap-2 text-sm font-semibold text-red-500 hover:text-red-600 transition-smooth">
          <Trash2 size={15} /> Delete Goal
        </button>
      </div>

      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="bg-white rounded-2xl border border-navy-100 shadow-card p-6 mb-6">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="font-display text-2xl font-bold text-navy-800">{goal.title}</h1>
            <p className="text-navy-400 text-sm mt-1">{goal.description}</p>
          </div>
          <span className={`text-xs font-semibold px-3 py-1.5 rounded-full ${statusStyles[goal.status] || statusStyles.ACTIVE}`}>
            {goal.status}
          </span>
        </div>

        <div className="flex gap-8 mt-5 text-sm flex-wrap">
          <div><p className="text-navy-400">Start Date</p><p className="font-semibold text-navy-800">{goal.startDate}</p></div>
          <div><p className="text-navy-400">Target Date</p><p className="font-semibold text-navy-800">{goal.targetDate}</p></div>
          <div><p className="text-navy-400">Days Remaining</p><p className="font-semibold text-navy-800">{goal.daysRemaining}</p></div>
          <div><p className="text-navy-400">Mode</p><p className="font-semibold text-navy-800">{goal.mode}</p></div>
        </div>

        <div className="mt-5">
          <div className="flex justify-between text-sm text-navy-500 mb-1">
            <span>Overall Progress</span><span className="font-semibold">{goal.percentComplete}%</span>
          </div>
          <div className="h-2.5 rounded-full bg-navy-50 overflow-hidden">
            <motion.div initial={{ width: 0 }} animate={{ width: `${goal.percentComplete}%` }} transition={{ duration: 0.8 }}
              className="h-full bg-gradient-to-r from-accent-500 to-accent-400 rounded-full" />
          </div>
        </div>

        <div className="no-print flex gap-3 mt-6">
          <button onClick={handleLoadReport} disabled={reportLoading}
            className="flex items-center gap-2 bg-navy-800 text-white text-sm font-semibold px-4 py-2.5 rounded-xl hover:bg-navy-700 transition-smooth disabled:opacity-60">
            <FileDown size={16} /> {reportLoading ? 'Loading...' : report ? 'Hide Report' : 'View / Download Goal Report'}
          </button>
          {report && (
            <button onClick={() => window.print()}
              className="flex items-center gap-2 bg-accent-500 text-white text-sm font-semibold px-4 py-2.5 rounded-xl hover:bg-accent-600 transition-smooth">
              <FileDown size={16} /> Download as PDF
            </button>
          )}
        </div>
      </motion.div>

      <AnimatePresence>
        {report && (
          <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}
            className="bg-white rounded-2xl border border-navy-100 shadow-card p-6 mb-6 overflow-hidden">
            <h2 className="font-semibold text-navy-800 text-lg mb-4">Goal Report: {report.startDate} to {report.targetDate}</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
              <div><p className="text-2xl font-bold text-navy-800">{report.completionRate}%</p><p className="text-xs text-navy-400">Completion Rate</p></div>
              <div><p className="text-2xl font-bold text-navy-800">{report.totalHoursStudied}h</p><p className="text-xs text-navy-400">Hours Studied</p></div>
              <div><p className="text-2xl font-bold text-navy-800">{report.completedTasks}/{report.totalTasks}</p><p className="text-xs text-navy-400">Tasks Done</p></div>
              <div><p className="text-2xl font-bold text-navy-800">{Math.round(report.avgMentalLoad)}</p><p className="text-xs text-navy-400">Avg Mental Load</p></div>
            </div>

            {report.subjectBreakdown?.length > 0 && (
              <div className="mb-6">
                <h3 className="font-semibold text-navy-700 text-sm mb-3">Subject Breakdown</h3>
                <div className="space-y-2">
                  {report.subjectBreakdown.map((s) => (
                    <div key={s.subjectName} className="flex justify-between text-sm py-1.5 border-b border-navy-50 last:border-0">
                      <span className="text-navy-700">{s.subjectName}</span>
                      <span className="text-navy-400">{s.hoursCompleted}h &middot; {s.topicsCompleted}/{s.topicsTotal} topics</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="bg-gradient-to-br from-navy-800 to-accent-600 rounded-2xl p-5 text-white">
              <h3 className="font-semibold mb-3 flex items-center gap-2 text-sm"><Lightbulb size={16} /> Suggestions</h3>
              <ul className="space-y-1.5">
                {report.suggestions.map((s, i) => (
                  <li key={i} className="text-sm text-white/90 flex gap-2"><span className="text-accent-200">&bull;</span> {s}</li>
                ))}
              </ul>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <h2 className="font-semibold text-navy-800 text-lg mb-4">Subjects & Topics</h2>
      <div className="space-y-4">
        {goal.subjects.map((subject) => (
          <motion.div key={subject.id} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
            className="bg-white rounded-2xl border border-navy-100 shadow-card p-5">
            <h3 className="font-semibold text-navy-800 mb-3">{subject.name}</h3>
            <div className="space-y-2">
              {subject.topics.map((topic) => (
                <div key={topic.id} className="flex items-center justify-between text-sm py-1.5 border-b border-navy-50 last:border-0">
                  <span className="flex items-center gap-2 text-navy-700">
                    {topic.completed ? <CheckCircle2 size={16} className="text-green-500" /> : <Circle size={16} className="text-navy-300" />}
                    {topic.name}
                    {topic.weight != null && (
                      <span className={`text-[10px] font-semibold px-1.5 py-0.5 rounded-full ${importanceStyle[Math.round(topic.weight)] || importanceStyle[2]}`}>
                        {importanceLabel[Math.round(topic.weight)] || 'Medium'}
                      </span>
                    )}
                  </span>
                  <span className="text-navy-400 text-xs">{topic.hoursRemaining}h / {topic.estimatedHours}h left</span>
                </div>
              ))}
            </div>
          </motion.div>
        ))}
      </div>

      <AnimatePresence>
        {confirmDelete && (
          <motion.div
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 bg-navy-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
            onClick={() => setConfirmDelete(false)}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: 0.95 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6"
            >
              <h3 className="font-semibold text-navy-800 text-lg mb-2">Delete "{goal.title}"?</h3>
              <p className="text-sm text-navy-500 mb-6">
                This permanently deletes this goal along with every task and schedule generated for it.
                Your timetable setup and other goals are not affected. This can't be undone.
              </p>
              <div className="flex gap-3">
                <button onClick={() => setConfirmDelete(false)} disabled={deleting}
                  className="flex-1 py-2.5 rounded-xl border border-navy-100 text-navy-600 font-medium hover:bg-navy-50 transition-smooth">
                  Cancel
                </button>
                <button onClick={handleDelete} disabled={deleting}
                  className="flex-1 py-2.5 rounded-xl bg-red-500 text-white font-semibold hover:bg-red-600 transition-smooth disabled:opacity-60">
                  {deleting ? 'Deleting...' : 'Yes, delete'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
