import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, ReferenceLine } from 'recharts';
import * as mentalLoadApi from '../api/mentalLoad';
import LoadMeter from '../components/LoadMeter';

export default function MentalLoad() {
  const [today, setToday] = useState(null);
  const [trend, setTrend] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const to = new Date().toISOString().slice(0, 10);
    const fromDate = new Date();
    fromDate.setDate(fromDate.getDate() - 14);
    const from = fromDate.toISOString().slice(0, 10);

    Promise.all([mentalLoadApi.getTodayLoad(), mentalLoadApi.getTrend(from, to)])
      .then(([t, tr]) => { setToday(t); setTrend(tr); })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="p-8 text-navy-400">Loading...</div>;

  const chartData = trend?.points.map(p => ({ date: p.date.slice(5), load: p.loadScore, threshold: p.threshold })) || [];

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <h1 className="font-display text-3xl font-bold text-navy-800 mb-2">Mental Load Balancer</h1>
      <p className="text-navy-400 text-sm mb-8">Tracks your capacity and self-calibrates over time.</p>

      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
        className="bg-white rounded-2xl border border-navy-100 shadow-card p-6 mb-8">
        <h2 className="font-semibold text-navy-800 mb-4">Today</h2>
        {today && <LoadMeter score={today.loadScore} threshold={today.threshold} status={today.status} />}
      </motion.div>

      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}
        className="bg-white rounded-2xl border border-navy-100 shadow-card p-6">
        <h2 className="font-semibold text-navy-800 mb-4">14-Day Trend</h2>
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#F5F7FC" />
            <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#AEB8D0' }} />
            <YAxis domain={[0, 100]} tick={{ fontSize: 12, fill: '#AEB8D0' }} />
            <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #E8ECF6' }} />
            <ReferenceLine y={trend?.currentThreshold} stroke="#f59e0b" strokeDasharray="4 4" label={{ value: 'Threshold', fontSize: 11, fill: '#f59e0b' }} />
            <Line type="monotone" dataKey="load" stroke="#6250F0" strokeWidth={2.5} dot={{ r: 3 }} activeDot={{ r: 5 }} />
          </LineChart>
        </ResponsiveContainer>
        <p className="text-xs text-navy-400 mt-2">
          The dashed line is your current self-calibrated threshold — it moves as StudySync learns your capacity.
        </p>
      </motion.div>
    </div>
  );
}
