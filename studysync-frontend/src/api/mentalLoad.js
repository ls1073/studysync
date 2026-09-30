import client from './client';

export const getTodayLoad = () => client.get('/mental-load/today').then(r => r.data);
export const getTrend = (start, end) => client.get('/mental-load/trend', { params: { start, end } }).then(r => r.data);
