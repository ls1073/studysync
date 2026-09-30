import client from './client';

export const getNotifications = () => client.get('/notifications').then(r => r.data);
export const markNotificationRead = (id) => client.post(`/notifications/${id}/read`);
