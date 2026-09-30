import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { Calendar, TrendingUp } from 'lucide-react';

export default function GoalCard({ goal }) {
  return (
    <Link to={`/goals/${goal.id}`}>
      <motion.div
        whileHover={{ y: -3 }}
        transition={{ duration: 0.2 }}
        className="rounded-2xl bg-white border border-navy-100 p-5 shadow-card hover:shadow-soft transition-smooth cursor-pointer"
      >
        <div className="flex items-start justify-between">
          <h3 className="font-semibold text-navy-800 text-lg">{goal.title}</h3>
          <span className="text-xs font-semibold px-2 py-1 rounded-full bg-accent-500/10 text-accent-600">
            {goal.status}
          </span>
        </div>

        <div className="flex items-center gap-4 mt-3 text-xs text-navy-400">
          <span className="flex items-center gap-1"><Calendar size={13} /> {goal.targetDate}</span>
          <span className="flex items-center gap-1"><TrendingUp size={13} /> {goal.daysRemaining} days left</span>
        </div>

        <div className="mt-4">
          <div className="flex justify-between text-xs text-navy-500 mb-1">
            <span>Progress</span>
            <span className="font-semibold">{goal.percentComplete}%</span>
          </div>
          <div className="h-2 rounded-full bg-navy-50 overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${goal.percentComplete}%` }}
              transition={{ duration: 0.8, ease: 'easeOut' }}
              className="h-full bg-gradient-to-r from-accent-500 to-accent-400 rounded-full"
            />
          </div>
        </div>
      </motion.div>
    </Link>
  );
}
