import client from './client';

export const getTodayTasks = (goalId) => client.get('/tasks/today', { params: { goalId } }).then(r => r.data);
export const getTasksForDate = (date, goalId) => client.get(`/tasks/date/${date}`, { params: { goalId } }).then(r => r.data);
export const getUpcomingTasks = (from, to, goalId) => client.get('/tasks/upcoming', { params: { from, to, goalId } }).then(r => r.data);
export const getHistory = (goalId) => client.get('/tasks/history', { params: { goalId } }).then(r => r.data);
export const completeTask = (id, effortRating) => client.post(`/tasks/${id}/complete`, { effortRating }).then(r => r.data);
export const markMissed = (id) => client.post(`/tasks/${id}/miss`).then(r => r.data);
export const markDayMissed = (date) => client.post('/tasks/miss-day', null, { params: { date } }).then(r => r.data);
