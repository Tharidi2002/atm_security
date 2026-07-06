const API_BASE_URL = 'http://localhost:8080/api';

export const fetchAlerts = async (username) => {
  try {
    const url = username 
      ? `${API_BASE_URL}/alerts?username=${encodeURIComponent(username)}` 
      : `${API_BASE_URL}/alerts`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Failed to fetch alerts');
    return await response.json();
  } catch (error) {
    console.error('Error fetching alerts:', error);
    return [];
  }
};

export const fetchUsers = async () => {
  const response = await fetch(`${API_BASE_URL}/admin/users`);
  if (!response.ok) throw new Error('Failed to fetch users');
  return await response.json();
};

export const createUser = async (userData) => {
  const response = await fetch(`${API_BASE_URL}/admin/users`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData),
  });
  if (!response.ok) {
    const errorMsg = await response.text();
    throw new Error(errorMsg || 'Failed to create user');
  }
  return await response.json();
};

export const fetchSystems = async () => {
  const response = await fetch(`${API_BASE_URL}/admin/systems`);
  if (!response.ok) throw new Error('Failed to fetch systems');
  return await response.json();
};

export const assignSystems = async (userId, systemIds) => {
  const response = await fetch(`${API_BASE_URL}/admin/users/${userId}/assign`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ systemIds }),
  });
  if (!response.ok) throw new Error('Failed to assign systems');
  return true;
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

export const createSystem = async (systemData) => {
  const response = await fetch(`${API_BASE_URL}/admin/systems`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(systemData),
  });
  if (!response.ok) {
    const errorMsg = await response.text();
    throw new Error(errorMsg || 'Failed to create system');
  }
  return await response.json();
};

export const updateSystem = async (systemId, systemData) => {
  const response = await fetch(`${API_BASE_URL}/admin/systems/${systemId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(systemData),
  });
  if (!response.ok) throw new Error('Failed to update system');
  return await response.json();
};

export const toggleSystemStatus = async (systemId, status) => {
  const response = await fetch(`${API_BASE_URL}/admin/systems/${systemId}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  if (!response.ok) throw new Error('Failed to change status');
  return await response.json();
};

export const deleteSystem = async (systemId) => {
  const response = await fetch(`${API_BASE_URL}/admin/systems/${systemId}`, {
    method: 'DELETE',
  });
  if (!response.ok) throw new Error('Failed to delete system');
  return true;
};