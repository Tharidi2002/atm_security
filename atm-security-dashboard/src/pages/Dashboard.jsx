import React from 'react';
import Navbar from '../components/Navbar';
import StatsCards from '../components/StatsCards';
import AlertTable from '../components/AlertTable';
import NotificationToast from '../components/NotificationToast';
import { useAlerts } from '../hooks/useAlerts';

export default function Dashboard() {
  const { 
    alerts, 
    loading, 
    stats, 
    newAlert,
    clearNewAlert,
    refreshAlerts,
    tableContainerRef 
  } = useAlerts();

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 font-sans">
      <Navbar onRefresh={refreshAlerts} />
      
      <main className="p-4 sm:p-6 max-w-7xl mx-auto space-y-6">
        <StatsCards stats={stats} />
        
        {/* Alerts count badge */}
        <div className="flex justify-between items-center">
          <div className="text-sm text-slate-400">
            Showing <span className="text-white font-bold">{alerts.length}</span> alerts
            {stats.pending > 0 && (
              <span className="ml-2 text-red-400">
                • <span className="font-bold">{stats.pending}</span> pending
              </span>
            )}
          </div>
          <div className="text-xs text-slate-500 font-mono">
            Auto-refresh every 5s
          </div>
        </div>

        <AlertTable 
          alerts={alerts} 
          loading={loading} 
          tableContainerRef={tableContainerRef}
        />
      </main>

      {/* Notification Toast - New alert එකක් ආවොත් */}
      {newAlert && (
        <NotificationToast 
          alert={newAlert} 
          onClose={clearNewAlert} 
        />
      )}
    </div>
  );
}