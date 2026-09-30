import client from './client';

export const getDayOrders = () => client.get('/timetable/day-order').then(r => r.data);
export const saveDayOrder = (data) => client.post('/timetable/day-order', data).then(r => r.data);
export const deleteDayOrder = (id) => client.delete(`/timetable/day-order/${id}`);
