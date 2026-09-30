import client from './client';

export const searchTemplates = (query = '') => client.get('/goal-templates', { params: { query } }).then(r => r.data);
export const getTemplateDetail = (id) => client.get(`/goal-templates/${id}`).then(r => r.data);
export const createGoal = (data) => client.post('/goals', data).then(r => r.data);
export const listGoals = () => client.get('/goals').then(r => r.data);
export const getGoal = (id) => client.get(`/goals/${id}`).then(r => r.data);
export const deleteGoal = (id) => client.delete(`/goals/${id}`);
