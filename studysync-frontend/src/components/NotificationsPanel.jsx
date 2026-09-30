import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, X, AlertTriangle, RefreshCw, Clock, Zap } from 'lucide-react';
import * as notificationsApi from '../api/notifications';

const typeIcon = {
  TASK_MISSED: <AlertTriangle size={16} className="text-red-500" />,
  PLAN_REGENERATED: <RefreshCw size={16} className="text-accent-500" />,
  GOAL_DEADLINE_REMINDER: <Clock size={16} className="text-amber-500" />,
  OVERLOAD_WARNING: <Zap size={16} className="text-amber-500" />,
};

export default function NotificationsPanel({ unreadCount, onRead }) {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleOpen = async () => {
    const next = !open;
    setOpen(next);
    if (next) {
      setLoading(true);
      try {
        const data = await notificationsApi.getNotifications();
        setNotifications(data);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleMarkRead = async (id) => {
    await notificationsApi.markNotificationRead(id);
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
    onRead?.();
  };

  return (
    <div className="relative" ref={ref}>
      <button onClick={toggleOpen} className="relative">
        <Bell className="text-navy-500 hover:text-navy-700 transition-smooth" size={22} />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: -8, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.97 }}
            transition={{ duration: 0.15 }}
            className="absolute right-0 mt-3 w-96 max-h-[28rem] overflow-y-auto bg-white rounded-2xl shadow-soft border border-navy-100 z-50"
          >
            <div className="flex items-center justify-between px-4 py-3 border-b border-navy-50">
              <h3 className="font-semibold text-navy-800 text-sm">Notifications</h3>
              <button onClick={() => setOpen(false)} className="text-navy-300 hover:text-navy-600">
                <X size={16} />
              </button>
            </div>

            {loading ? (
              <p className="text-navy-400 text-sm text-center py-8">Loading...</p>
            ) : notifications.length === 0 ? (
              <p className="text-navy-400 text-sm text-center py-8">No notifications yet.</p>
            ) : (
              <div className="divide-y divide-navy-50">
                {notifications.map((n) => (
                  <div
                    key={n.id}
                    onClick={() => !n.isRead && handleMarkRead(n.id)}
                    className={`px-4 py-3 flex gap-3 cursor-pointer transition-smooth ${
                      n.isRead ? 'bg-white' : 'bg-accent-500/5 hover:bg-accent-500/10'
                    }`}
                  >
                    <span className="mt-0.5 shrink-0">{typeIcon[n.type] || <Bell size={16} className="text-navy-400" />}</span>
                    <div className="min-w-0">
                      <p className={`text-sm ${n.isRead ? 'text-navy-500' : 'text-navy-800 font-medium'}`}>{n.message}</p>
                      <p className="text-xs text-navy-300 mt-1">{n.createdAt}</p>
                    </div>
                    {!n.isRead && <span className="w-2 h-2 rounded-full bg-accent-500 shrink-0 mt-1.5" />}
                  </div>
                ))}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
