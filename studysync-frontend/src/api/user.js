import client from './client';

export const getProfile = () => client.get('/users/me').then(r => r.data);
export const updateCommuteDistance = (commuteDistanceKm) =>
  client.put('/users/me/commute-distance', { commuteDistanceKm }).then(r => r.data);
