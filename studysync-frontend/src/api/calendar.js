import client from './client';

export const setSingleEntry = (data) => client.post('/calendar/entry', data).then(r => r.data);
export const setBulkRange = (data) => client.post('/calendar/bulk-range', data).then(r => r.data);
export const getRange = (start, end) => client.get('/calendar', { params: { start, end } }).then(r => r.data);
