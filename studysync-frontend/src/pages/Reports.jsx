import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Download, Lightbulb, TrendingUp, Coffee, Target } from 'lucide-react';
import * as reportsApi from '../api/reports';
import { useGoalContext } from '../context/GoalContext';

export default function Reports() {
  const { selectedGoalId, loading: goalsLoading } = useGoalContext();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (goalsLoading) return;
    setLoading(true);
    reportsApi.getWeeklyReport(undefined, undefined, selectedGoalId).then(setReport).finally(() => setLoading(false));
  }, [selectedGoalId, goalsLoading]);

  const handleDownload = () => {
    window.print();
  };

  if (loading || goalsLoading) return <div className="px-8 pt-6 text-navy-400">Loading report...</div>;
  if (!report) return <div className="px-8 pt-6 text-navy-400">No report available yet.</div>;

  return (
    <div className="px-8 pb-8 pt-2 max-w-4xl mx-auto" id="report-content">
      <style>{`
        @media print {
          aside { display: none !important; }
          #report-content { padding: 0 !important; max-width: 100% !important; }
          .no-print { display: none !important; }
        }
      `}</style>

      <div className="flex items-center justify-between mb-8 mt-4">
        <div>
          <h1 className="font-display text-3xl font-bold text-navy-800">Weekly Progress Report</h1>
          <p className="text-navy-400 text-sm mt-1">{report.weekStart} to {report.weekEnd}</p>
        </div>
        <motion.button whileTap={{ scale: 0.96 }} onClick={handleDownload}
          className="no-print flex items-center gap-2 bg-accent-500 text-white font-semibold px-5 py-2.5 rounded-xl hover:bg-accent-600 transition-smooth">
          <Download size={16} /> Download Report
        </motion.button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Completion Rate', value: `${report.completionRate}%`, icon: TrendingUp },
          { label: 'Hours Studied', value: `${report.totalHoursStudied}h`, icon: Target },
          { label: 'Avg Mental Load', value: Math.round(report.avgMentalLoad), icon: Lightbulb },
          { label: 'Rest Hours', value: `${report.restHoursTaken}h`, icon: Coffee },
        ].map((stat, i) => (
          <motion.div key={stat.label} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
            className="bg-white rounded-2xl border border-navy-100 shadow-card p-4">
            <stat.icon size={18} className="text-accent-500 mb-2" />
            <p className="text-2xl font-bold text-navy-800">{stat.value}</p>
            <p className="text-xs text-navy-400 mt-1">{stat.label}</p>
          </motion.div>
        ))}
      </div>

      <div className="bg-white rounded-2xl border border-navy-100 shadow-card p-6 mb-6">
        <h2 className="font-semibold text-navy-800 mb-4">Task Summary</h2>
        <div className="flex gap-8 text-sm">
          <div><p className="text-navy-400">Total Tasks</p><p className="font-semibold text-navy-800 text-lg">{report.totalTasks}</p></div>
          <div><p className="text-navy-400">Completed</p><p className="font-semibold text-green-600 text-lg">{report.completedTasks}</p></div>
          <div><p className="text-navy-400">Missed</p><p className="font-semibold text-red-500 text-lg">{report.missedTasks}</p></div>
        </div>
      </div>

      {report.subjectBreakdown?.length > 0 && (
        <div className="bg-white rounded-2xl border border-navy-100 shadow-card p-6 mb-6">
          <h2 className="font-semibold text-navy-800 mb-4">Time by Subject</h2>
          <div className="space-y-3">
            {report.subjectBreakdown.map((s) => (
              <div key={s.subjectName}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-navy-700 font-medium">{s.subjectName}</span>
                  <span className="text-navy-400">{s.hoursCompleted}h &middot; {s.tasksCompleted} tasks</span>
                </div>
                <div className="h-2 rounded-full bg-navy-50 overflow-hidden">
                  <div className="h-full bg-accent-500 rounded-full"
                    style={{ width: `${Math.min(100, (s.hoursCompleted / Math.max(...report.subjectBreakdown.map(x => x.hoursCompleted))) * 100)}%` }} />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="bg-gradient-to-br from-navy-800 to-accent-600 rounded-2xl p-6 text-white">
        <h2 className="font-semibold mb-4 flex items-center gap-2"><Lightbulb size={18} /> Tips for Next Week</h2>
        <ul className="space-y-2">
          {report.tips.map((tip, i) => (
            <li key={i} className="text-sm text-white/90 flex gap-2">
              <span className="text-accent-200">&bull;</span> {tip}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
