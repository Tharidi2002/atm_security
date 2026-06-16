import {
  AlertTriangle,
  Bell,
  Eye,
  LogOut,
  Shield,
  X,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import type { SecurityAlert } from '../types';

const MOCK_ALERTS: SecurityAlert[] = [
  {
    id: 1,
    bankId: 1,
    bankName: 'Commercial Bank of Ceylon',
    alertType: 'DOOR_OPEN',
    severity: 'CRITICAL',
    title: 'Unauthorized Door Access',
    message: 'ATM enclosure door opened outside scheduled maintenance window.',
    locationName: 'Colombo Fort Branch ATM',
    acknowledged: false,
    receivedAt: new Date().toISOString(),
  },
  {
    id: 2,
    bankId: 2,
    bankName: "People's Bank",
    alertType: 'POWER_FAILURE',
    severity: 'WARNING',
    title: 'Power Supply Interrupted',
    message: 'Main power lost. UPS backup activated.',
    locationName: 'Kandy City Center ATM',
    acknowledged: false,
    receivedAt: new Date(Date.now() - 300000).toISOString(),
  },
  {
    id: 3,
    bankId: 3,
    bankName: 'Bank of Ceylon',
    alertType: 'FIRE_ALARM',
    severity: 'CRITICAL',
    title: 'Fire Alarm Triggered',
    message: 'Smoke detector activated in ATM vestibule.',
    locationName: 'Galle Fort ATM',
    acknowledged: false,
    receivedAt: new Date(Date.now() - 600000).toISOString(),
  },
];

export default function Dashboard() {
  const { user, logout } = useAuth();
  const [alerts, setAlerts] = useState<SecurityAlert[]>(MOCK_ALERTS);
  const [selectedAlert, setSelectedAlert] = useState<SecurityAlert | null>(null);
  const [showAdminPopup, setShowAdminPopup] = useState(false);
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');

  useEffect(() => {
    if (sessionStorage.getItem('showAdminPopup') === 'true' && user?.role === 'ADMIN') {
      setShowAdminPopup(true);
      sessionStorage.removeItem('showAdminPopup');
    }
  }, [user]);

  const criticalAlerts = alerts.filter((a) => a.severity === 'CRITICAL' && !a.acknowledged);

  const filteredAlerts = alerts.filter((alert) => {
    if (severityFilter !== 'ALL' && alert.severity !== severityFilter) return false;
    if (user?.role !== 'ADMIN' && user?.bankId && alert.bankId !== user.bankId) return false;
    return true;
  });

  const severityBadge = (severity: string) => {
    const styles: Record<string, string> = {
      CRITICAL: 'bg-danger-500/20 text-danger-500 border-danger-500/30',
      WARNING: 'bg-warning-500/20 text-warning-500 border-warning-500/30',
      INFO: 'bg-primary-500/20 text-primary-500 border-primary-500/30',
    };
    return styles[severity] || styles.INFO;
  };

  return (
    <div className="min-h-screen bg-slate-950">
      <header className="border-b border-slate-800 bg-slate-900/50 backdrop-blur sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Shield className="w-7 h-7 text-primary-500" />
            <div>
              <h1 className="font-bold text-lg">ATM Security Dashboard</h1>
              <p className="text-xs text-slate-400">
                {user?.fullName} · {user?.role.replace('_', ' ')}
                {user?.bankName && ` · ${user.bankName}`}
              </p>
            </div>
          </div>
          <button
            onClick={() => logout()}
            className="flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-700 hover:bg-slate-800 text-sm"
          >
            <LogOut className="w-4 h-4" />
            Logout
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <StatCard label="Critical Alerts" value={criticalAlerts.length} color="text-danger-500" />
          <StatCard
            label="Total Alerts"
            value={filteredAlerts.length}
            color="text-primary-500"
          />
          <StatCard
            label="Unacknowledged"
            value={filteredAlerts.filter((a) => !a.acknowledged).length}
            color="text-warning-500"
          />
        </div>

        <div className="flex flex-wrap gap-2 mb-4">
          {['ALL', 'CRITICAL', 'WARNING', 'INFO'].map((s) => (
            <button
              key={s}
              onClick={() => setSeverityFilter(s)}
              className={`px-4 py-1.5 rounded-full text-sm border transition-colors ${
                severityFilter === s
                  ? 'bg-primary-600 border-primary-500 text-white'
                  : 'border-slate-700 text-slate-400 hover:border-slate-500'
              }`}
            >
              {s}
            </button>
          ))}
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
          <div className="px-6 py-4 border-b border-slate-800 flex items-center gap-2">
            <Bell className="w-5 h-5 text-primary-500" />
            <h2 className="font-semibold">Security Alert Feed</h2>
            <span className="text-xs text-slate-500 ml-2">
              (Live WebSocket feed connects in Alert Service — Step 3)
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-slate-400 border-b border-slate-800">
                  <th className="text-left px-6 py-3 font-medium">Severity</th>
                  <th className="text-left px-6 py-3 font-medium">Type</th>
                  <th className="text-left px-6 py-3 font-medium">Title</th>
                  <th className="text-left px-6 py-3 font-medium">Location</th>
                  <th className="text-left px-6 py-3 font-medium">Bank</th>
                  <th className="text-left px-6 py-3 font-medium">Time</th>
                  <th className="text-left px-6 py-3 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredAlerts.map((alert) => (
                  <tr
                    key={alert.id}
                    className="border-b border-slate-800/50 hover:bg-slate-800/30"
                  >
                    <td className="px-6 py-3">
                      <span
                        className={`inline-flex px-2 py-0.5 rounded-full text-xs border ${severityBadge(alert.severity)}`}
                      >
                        {alert.severity}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-slate-300">{alert.alertType}</td>
                    <td className="px-6 py-3 font-medium">{alert.title}</td>
                    <td className="px-6 py-3 text-slate-400">{alert.locationName}</td>
                    <td className="px-6 py-3 text-slate-400">{alert.bankName}</td>
                    <td className="px-6 py-3 text-slate-500">
                      {new Date(alert.receivedAt).toLocaleString()}
                    </td>
                    <td className="px-6 py-3">
                      <button
                        onClick={() => setSelectedAlert(alert)}
                        className="p-1.5 rounded-lg hover:bg-slate-700 text-primary-500"
                        title="View details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
                {filteredAlerts.length === 0 && (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                      No alerts match your filters
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>

      {showAdminPopup && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
          <div className="bg-slate-900 border border-danger-500/30 rounded-2xl max-w-lg w-full shadow-2xl">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
              <div className="flex items-center gap-2 text-danger-500">
                <AlertTriangle className="w-5 h-5" />
                <h3 className="font-bold">Urgent Security Alerts</h3>
              </div>
              <button onClick={() => setShowAdminPopup(false)}>
                <X className="w-5 h-5 text-slate-400 hover:text-white" />
              </button>
            </div>
            <div className="p-6 space-y-3 max-h-80 overflow-y-auto">
              {criticalAlerts.length === 0 ? (
                <p className="text-slate-400">No critical alerts at this time.</p>
              ) : (
                criticalAlerts.map((alert) => (
                  <div
                    key={alert.id}
                    className="p-4 rounded-lg bg-danger-500/10 border border-danger-500/20"
                  >
                    <p className="font-medium">{alert.title}</p>
                    <p className="text-sm text-slate-400 mt-1">{alert.locationName}</p>
                    <p className="text-xs text-slate-500 mt-1">
                      {alert.bankName} · {new Date(alert.receivedAt).toLocaleString()}
                    </p>
                  </div>
                ))
              )}
            </div>
            <div className="px-6 py-4 border-t border-slate-800">
              <button
                onClick={() => setShowAdminPopup(false)}
                className="w-full py-2 bg-primary-600 hover:bg-primary-700 rounded-lg font-medium"
              >
                Acknowledge & Continue
              </button>
            </div>
          </div>
        </div>
      )}

      {selectedAlert && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-lg w-full">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
              <h3 className="font-bold">Alert Details</h3>
              <button onClick={() => setSelectedAlert(null)}>
                <X className="w-5 h-5 text-slate-400" />
              </button>
            </div>
            <div className="p-6 space-y-3">
              <DetailRow label="Severity" value={selectedAlert.severity} />
              <DetailRow label="Type" value={selectedAlert.alertType} />
              <DetailRow label="Title" value={selectedAlert.title} />
              <DetailRow label="Message" value={selectedAlert.message} />
              <DetailRow label="Location" value={selectedAlert.locationName || 'N/A'} />
              <DetailRow label="Bank" value={selectedAlert.bankName || 'N/A'} />
              <DetailRow
                label="Received"
                value={new Date(selectedAlert.receivedAt).toLocaleString()}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatCard({
  label,
  value,
  color,
}: {
  label: string;
  value: number;
  color: string;
}) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
      <p className="text-sm text-slate-400">{label}</p>
      <p className={`text-3xl font-bold mt-1 ${color}`}>{value}</p>
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs text-slate-500 uppercase tracking-wide">{label}</p>
      <p className="text-sm mt-0.5">{value}</p>
    </div>
  );
}
