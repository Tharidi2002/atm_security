import { useState, useEffect, useCallback, useRef } from 'react';
import { fetchAlerts } from '../services/api';

export function useAlerts() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newAlert, setNewAlert] = useState(null);
  const [stats, setStats] = useState({ total: 0, pending: 0, resolved: 0 });
  const previousAlertIds = useRef(new Set());
  const scrollPositionRef = useRef(0);
  const tableContainerRef = useRef(null);

  const calculateStats = (data) => {
    const pending = data.filter(a => a.status === 'PENDING').length;
    const resolved = data.filter(a => a.status === 'RESOLVED').length;
    return { total: data.length, pending, resolved };
  };

  const loadAlerts = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) setLoading(true);
      
      // Current scroll position එක save කරන්න
      if (tableContainerRef.current) {
        scrollPositionRef.current = tableContainerRef.current.scrollTop;
      }

      const data = await fetchAlerts();
      
      // New alerts තියෙනවද කියලා check කරන්න
      const currentIds = new Set(data.map(a => a.id));
      const newAlerts = data.filter(a => !previousAlertIds.current.has(a.id));
      
      if (newAlerts.length > 0 && previousAlertIds.current.size > 0) {
        // New alert එකක් තියෙනවා - Notification එක පෙන්වන්න
        setNewAlert(newAlerts[0]); // පළවෙනි new alert එක show කරන්න
      }
      
      previousAlertIds.current = currentIds;
      setAlerts(data);
      setStats(calculateStats(data));
      
      // Scroll position එක restore කරන්න (table එක උඩට පනින්න එපා)
      setTimeout(() => {
        if (tableContainerRef.current) {
          tableContainerRef.current.scrollTop = scrollPositionRef.current;
        }
      }, 0);
      
    } catch (error) {
      console.error('Error loading alerts:', error);
    } finally {
      if (showLoading) setLoading(false);
    }
  }, []);

  const clearNewAlert = useCallback(() => {
    setNewAlert(null);
  }, []);

  useEffect(() => {
    loadAlerts(true);
    
    // Auto-refresh - හැබැයි loading state එක පෙන්නන්න එපා
    const interval = setInterval(() => loadAlerts(false), 5000);
    return () => clearInterval(interval);
  }, [loadAlerts]);

  return { 
    alerts, 
    loading, 
    stats, 
    newAlert,
    clearNewAlert,
    refreshAlerts: () => loadAlerts(true),
    tableContainerRef
  };
}