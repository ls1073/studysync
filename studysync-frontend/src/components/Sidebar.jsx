import { NavLink } from 'react-router-dom';
import { motion } from 'framer-motion';
import { LayoutDashboard, Target, CalendarClock, ListChecks, BrainCircuit, LogOut, Table2, FileBarChart } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/goals', label: 'Goals', icon: Target },
  { to: '/timetable', label: 'Timetable Setup', icon: CalendarClock },
  { to: '/schedule', label: 'Full Timetable', icon: Table2 },
  { to: '/tasks', label: 'Tasks', icon: ListChecks },
  { to: '/mental-load', label: 'Mental Load', icon: BrainCircuit },
  { to: '/reports', label: 'Weekly Report', icon: FileBarChart },
];

export default function Sidebar() {
  const { logout, user } = useAuth();

  return (
    <aside className="w-64 shrink-0 h-screen sticky top-0 bg-navy-800 text-white flex flex-col">
      <div className="px-6 py-7">
        <h1 className="font-display text-2xl font-bold tracking-tight">StudySync</h1>
        <p className="text-ice/70 text-xs mt-1">Adaptive study scheduler</p>
      </div>

      <nav className="flex-1 px-3 space-y-1">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2.5 rounded-xl transition-smooth text-sm font-medium ${
                isActive
                  ? 'bg-accent-500 text-white shadow-glow'
                  : 'text-ice/80 hover:bg-navy-700 hover:text-white'
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="px-4 py-5 border-t border-navy-700">
        <div className="flex items-center justify-between px-2">
          <div>
            <p className="text-sm font-semibold">{user?.fullName || user?.username}</p>
            <p className="text-xs text-ice/60">@{user?.username}</p>
          </div>
          <motion.button
            whileTap={{ scale: 0.9 }}
            onClick={logout}
            className="p-2 rounded-lg hover:bg-navy-700 transition-smooth text-ice/80 hover:text-white"
            title="Log out"
          >
            <LogOut size={18} />
          </motion.button>
        </div>
      </div>
    </aside>
  );
}
