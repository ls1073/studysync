import { useEffect, useRef } from 'react';
import * as taskApi from '../api/tasks';

/**
 * Foreground task-start notifications using the browser's Notification API.
 *
 * Important honesty note: this only fires while the StudySync tab is open in
 * the browser (it polls every 30s and checks if any task's start time has
 * just arrived). It is NOT a true background/push notification system --
 * that would require a service worker, a push server, and VAPID keys, which
 * is a separate, larger feature. This gives real, working reminders for
 * anyone who keeps the app open, which covers the common case of studying
 * with StudySync open in a tab.
 */
export default function useTaskNotifications(enabled) {
  const notifiedTaskIds = useRef(new Set());

  useEffect(() => {
    if (!enabled) return;
    if (!('Notification' in window)) return;

    if (Notification.permission === 'default') {
      Notification.requestPermission();
    }

    const checkTasks = async () => {
      if (Notification.permission !== 'granted') return;
      try {
        const tasks = await taskApi.getTodayTasks();
        const now = new Date();
        const nowMinutes = now.getHours() * 60 + now.getMinutes();

        tasks.forEach((task) => {
          if (task.status !== 'PENDING') return;
          if (notifiedTaskIds.current.has(task.id)) return;

          const [h, m] = task.startTime.split(':').map(Number);
          const startMinutes = h * 60 + m;

          // fire within a 1-minute window of the task's actual start time
          if (nowMinutes >= startMinutes && nowMinutes <= startMinutes + 1) {
            new Notification(task.isRestBlock ? 'Time for a break' : 'Task starting now', {
              body: task.isRestBlock
                ? `Rest & Recharge (${task.allocatedHours}h)`
                : `${task.title} -- ${task.allocatedHours}h scheduled now`,
              icon: '/vite.svg',
            });
            notifiedTaskIds.current.add(task.id);
          }
        });
      } catch {
        // silently skip if not authenticated yet or request fails
      }
    };

    const interval = setInterval(checkTasks, 30000);
    checkTasks();

    return () => clearInterval(interval);
  }, [enabled]);
}
