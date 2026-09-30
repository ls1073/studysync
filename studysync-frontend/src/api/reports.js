import client from './client';

export const getWeeklyReport = (weekStart, weekEnd, goalId) =>
  client.get('/reports/weekly', { params: { weekStart, weekEnd, goalId } }).then(r => r.data);
export const getGoalReport = (goalId) => client.get(`/reports/goal/${goalId}`).then(r => r.data);
