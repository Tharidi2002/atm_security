const API_BASE_URL = 'http://localhost:8080/api';

export const fetchAlerts = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/alerts`);
    if (!response.ok) throw new Error('Failed to fetch alerts');
    return await response.json();
  } catch (error) {
    console.error('Error fetching alerts:', error);
    return [];
  }
};

export const resolveAlert = async (alertId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/alerts/${alertId}/resolve`, {
      method: 'PUT',
    });
    return await response.json();
  } catch (error) {
    console.error('Error resolving alert:', error);
    return null;
  }
};