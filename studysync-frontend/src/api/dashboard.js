import client from './client';

export const getDashboard = (goalId) => client.get('/dashboard', { params: { goalId } }).then(r => r.data);
