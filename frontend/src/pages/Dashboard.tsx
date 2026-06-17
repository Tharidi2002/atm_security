import {
  AlertTriangle,
  Bell,
  Eye,
  LogOut,
  Shield,
  X,
  Plus,
  Edit2,
  Trash2,
  Users,
  FileText,
  CheckCircle,
  Clock,
  MapPin,
  Phone,
  Server
} from 'lucide-react';
import { useEffect, useState, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { alertApi, stationApi, bankApi } from '../services/api';
import type { SecurityAlert, AtmStation, Bank } from '../types';

export default function Dashboard() {
  const { user, logout } = useAuth();
  
  // State variables
  const [alerts, setAlerts] = useState<SecurityAlert[]>([]);
  const [stations, setStations] = useState<AtmStation[]>([]);
  const [banks, setBanks] = useState<Bank[]>([]);
  
  const [selectedAlert, setSelectedAlert] = useState<SecurityAlert | null>(null);
  const [showAdminPopup, setShowAdminPopup] = useState(false);
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [bankFilter, setBankFilter] = useState<string>('ALL');
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [ackFilter, setAckFilter] = useState<string>('ALL');
  
  // Tab control: 'alerts' or 'stations'
  const [activeTab, setActiveTab] = useState<'alerts' | 'stations'>('alerts');

  // Station Form States
  const [showStationModal, setShowStationModal] = useState(false);
  const [editingStation, setEditingStation] = useState<AtmStation | null>(null);
  const [stationCode, setStationCode] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [stationBankId, setStationBankId] = useState<number>(1);
  const [locationName, setLocationName] = useState('');
  const [locationAddress, setLocationAddress] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');

  // Acknowledge Notes state
  const [ackNotes, setAckNotes] = useState('');

  // Mock User Creation Modal state
  const [showUserModal, setShowUserModal] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [newFullName, setNewFullName] = useState('');
  const [newUserRole, setNewUserRole] = useState('SECURITY_PERSONNEL');
  const [newUserBankId, setNewUserBankId] = useState<number>(1);
  
  // Reports Modal state
  const [showReportModal, setShowReportModal] = useState(false);

  // WebSocket Ref
  const wsRef = useRef<WebSocket | null>(null);

  // Fetch initial data
  useEffect(() => {
    fetchAlerts();
    fetchStations();
    fetchBanks();
  }, []);

  // Set up WebSocket connection
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    // Connect via Gateway (port 8080)
    const wsUrl = `ws://localhost:8080/ws/alerts?token=${token}`;
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onmessage = (event) => {
      try {
        const newAlert: SecurityAlert = JSON.parse(event.data);
        logWebSocketAlert(newAlert);
      } catch (e) {
        console.error('Error parsing WebSocket message', e);
      }
    };

    ws.onclose = () => {
      console.log('WebSocket connection closed. Reconnecting in 5s...');
      setTimeout(() => {
        // Simple reconnect logic
      }, 5000);
    };

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, []);

  const logWebSocketAlert = (newAlert: SecurityAlert) => {
    setAlerts((prev) => [newAlert, ...prev]);

    // If critical, trigger administrative popup alert
    if (newAlert.severity === 'CRITICAL' && !newAlert.acknowledged) {
      // Admin users get instant popup notification
      if (user?.role === 'ADMIN') {
        setShowAdminPopup(true);
      }
    }
  };

  const fetchAlerts = async () => {
    try {
      const res = await alertApi.list();
      if (res.data.success) {
        // Sort alerts by received time descending
        const sorted = res.data.data.sort(
          (a, b) => new Date(b.receivedAt).getTime() - new Date(a.receivedAt).getTime()
        );
        setAlerts(sorted);
        
        // Show popup for admin login if there are critical alerts
        const hasUnackCritical = sorted.some(a => a.severity === 'CRITICAL' && !a.acknowledged);
        if (hasUnackCritical && user?.role === 'ADMIN' && sessionStorage.getItem('showAdminPopup') === 'true') {
          setShowAdminPopup(true);
          sessionStorage.removeItem('showAdminPopup');
        }
      }
    } catch (e) {
      console.error('Error fetching alerts', e);
    }
  };

  const fetchStations = async () => {
    try {
      const res = await stationApi.list();
      if (res.data.success) {
        setStations(res.data.data);
      }
    } catch (e) {
      console.error('Error fetching stations', e);
    }
  };

  const fetchBanks = async () => {
    try {
      const res = await bankApi.list();
      if (res.data.success) {
        setBanks(res.data.data);
      }
    } catch (e) {
      console.error('Error fetching banks', e);
    }
  };

  // Station CRUD Handlers
  const handleOpenCreateStation = () => {
    setEditingStation(null);
    setStationCode('');
    setPhoneNumber('');
    setStationBankId(user?.bankId || 1);
    setLocationName('');
    setLocationAddress('');
    setLatitude('');
    setLongitude('');
    setShowStationModal(true);
  };

  const handleOpenEditStation = (st: AtmStation) => {
    setEditingStation(st);
    setStationCode(st.stationCode);
    setPhoneNumber(st.phoneNumberEnc || '');
    setStationBankId(st.bankId);
    setLocationName(st.locationName);
    setLocationAddress(st.locationAddress || '');
    setLatitude(st.latitude ? st.latitude.toString() : '');
    setLongitude(st.longitude ? st.longitude.toString() : '');
    setShowStationModal(true);
  };

  const handleSaveStation = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload: AtmStation = {
      stationCode,
      phoneNumberEnc: phoneNumber,
      bankId: stationBankId,
      locationName,
      locationAddress,
      latitude: latitude ? parseFloat(latitude) : undefined,
      longitude: longitude ? parseFloat(longitude) : undefined,
      active: true
    };

    try {
      if (editingStation && editingStation.id) {
        await stationApi.update(editingStation.id, payload);
      } else {
        await stationApi.create(payload);
      }
      setShowStationModal(false);
      fetchStations();
    } catch (err) {
      console.error('Error saving station', err);
    }
  };

  const handleDeleteStation = async (id: number) => {
    if (confirm('Are you sure you want to delete this ATM station?')) {
      try {
        await stationApi.delete(id);
        fetchStations();
      } catch (err) {
        console.error('Error deleting station', err);
      }
    }
  };

  // Acknowledge Alert Handler
  const handleAcknowledgeAlert = async () => {
    if (!selectedAlert) return;
    try {
      const res = await alertApi.acknowledge(selectedAlert.id, ackNotes);
      if (res.data.success) {
        setSelectedAlert(null);
        setAckNotes('');
        fetchAlerts();
      }
    } catch (err) {
      console.error('Error acknowledging alert', err);
    }
  };

  // Mock Report Download
  const handleDownloadReport = (format: string) => {
    const csvContent = "data:text/csv;charset=utf-8,ID,Bank,Type,Severity,Location,ReceivedAt,Acknowledged\n" + 
      alerts.map(a => `${a.id},"${a.bankName || a.bankId}",${a.alertType},${a.severity},"${a.locationName || 'N/A'}",${a.receivedAt},${a.acknowledged}`).join("\n");
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `ATM_Security_Report_${new Date().toISOString().split('T')[0]}.${format}`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setShowReportModal(false);
  };

  // Mock User Creation
  const handleCreateUser = (e: React.FormEvent) => {
    e.preventDefault();
    alert(`Mock User ${newFullName} (${newUsername}) registered successfully with role ${newUserRole}!`);
    setShowUserModal(false);
    setNewUsername('');
    setNewFullName('');
  };

  // Alert Filters Logic
  const filteredAlerts = alerts.filter((alert) => {
    if (severityFilter !== 'ALL' && alert.severity !== severityFilter) return false;
    if (bankFilter !== 'ALL' && alert.bankId.toString() !== bankFilter) return false;
    if (typeFilter !== 'ALL' && alert.alertType !== typeFilter) return false;
    if (ackFilter !== 'ALL') {
      const isAck = ackFilter === 'ACK';
      if (alert.acknowledged !== isAck) return false;
    }
    // Scope check: non-admins can only see their own bank's alerts
    if (user?.role !== 'ADMIN' && user?.bankId && alert.bankId !== user.bankId) return false;
    return true;
  });

  const criticalAlerts = alerts.filter((a) => a.severity === 'CRITICAL' && !a.acknowledged);

  const severityBadge = (severity: string) => {
    const styles: Record<string, string> = {
      CRITICAL: 'bg-danger-500/20 text-danger-500 border-danger-500/30',
      WARNING: 'bg-warning-500/20 text-warning-500 border-warning-500/30',
      INFO: 'bg-primary-500/20 text-primary-500 border-primary-500/30',
    };
    return styles[severity] || styles.INFO;
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans">
      {/* Premium Header */}
      <header className="border-b border-slate-800/80 bg-slate-900/40 backdrop-blur sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-gradient-to-tr from-primary-600 to-indigo-500 rounded-xl shadow-lg shadow-primary-500/10">
              <Shield className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="font-bold text-xl tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-200 to-slate-400">
                ATM Security System
              </h1>
              <p className="text-xs text-slate-400 font-medium">
                {user?.fullName} · <span className="text-primary-400">{user?.role.replace('_', ' ')}</span>
                {user?.bankName && ` · ${user.bankName}`}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {user?.role === 'ADMIN' && (
              <>
                <button
                  onClick={() => setShowUserModal(true)}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-800 bg-slate-900 hover:bg-slate-800 text-xs font-semibold text-slate-300 transition-all"
                >
                  <Users className="w-3.5 h-3.5" />
                  Add User
                </button>
                <button
                  onClick={() => setShowReportModal(true)}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-800 bg-slate-900 hover:bg-slate-800 text-xs font-semibold text-slate-300 transition-all"
                >
                  <FileText className="w-3.5 h-3.5" />
                  Generate Report
                </button>
              </>
            )}

            <button
              onClick={() => logout()}
              className="flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-800 hover:bg-slate-900 hover:border-slate-700 text-sm font-semibold transition-all duration-200"
            >
              <LogOut className="w-4 h-4 text-slate-400" />
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-6 py-8">
        {/* Navigation Tabs */}
        <div className="flex border-b border-slate-800 mb-6">
          <button
            onClick={() => setActiveTab('alerts')}
            className={`px-5 py-3 font-semibold text-sm transition-all border-b-2 ${
              activeTab === 'alerts'
                ? 'border-primary-500 text-primary-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            Security Alerts
          </button>
          {((user?.role === 'ADMIN') || (user?.role === 'BANK_MANAGER')) && (
            <button
              onClick={() => setActiveTab('stations')}
              className={`px-5 py-3 font-semibold text-sm transition-all border-b-2 ${
                activeTab === 'stations'
                  ? 'border-primary-500 text-primary-400'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              ATM Station Registry
            </button>
          )}
        </div>

        {activeTab === 'alerts' && (
          <>
            {/* Stat Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
              <StatCard
                label="Unacknowledged Critical"
                value={criticalAlerts.length}
                color="text-danger-500"
                icon={<AlertTriangle className="w-5 h-5 text-danger-500" />}
              />
              <StatCard
                label="Total Alerts"
                value={filteredAlerts.length}
                color="text-primary-500"
                icon={<Bell className="w-5 h-5 text-primary-500" />}
              />
              <StatCard
                label="Acknowledged"
                value={filteredAlerts.filter((a) => a.acknowledged).length}
                color="text-emerald-500"
                icon={<CheckCircle className="w-5 h-5 text-emerald-500" />}
              />
              <StatCard
                label="Unacknowledged"
                value={filteredAlerts.filter((a) => !a.acknowledged).length}
                color="text-warning-500"
                icon={<Clock className="w-5 h-5 text-warning-500" />}
              />
            </div>

            {/* Filter Bar */}
            <div className="bg-slate-900/50 border border-slate-800/80 rounded-xl p-4 mb-6 flex flex-wrap gap-4 items-center">
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Severity:</span>
                <div className="flex gap-1.5">
                  {['ALL', 'CRITICAL', 'WARNING', 'INFO'].map((s) => (
                    <button
                      key={s}
                      onClick={() => setSeverityFilter(s)}
                      className={`px-3 py-1 rounded-lg text-xs font-medium border transition-colors ${
                        severityFilter === s
                          ? 'bg-primary-600 border-primary-500 text-white'
                          : 'border-slate-800 bg-slate-900/40 text-slate-400 hover:border-slate-700'
                      }`}
                    >
                      {s}
                    </button>
                  ))}
                </div>
              </div>

              {user?.role === 'ADMIN' && (
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Bank:</span>
                  <select
                    value={bankFilter}
                    onChange={(e) => setBankFilter(e.target.value)}
                    className="bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-2.5 py-1 text-xs outline-none focus:border-primary-500"
                  >
                    <option value="ALL">All Banks</option>
                    {banks.map(b => (
                      <option key={b.id} value={b.id.toString()}>{b.name}</option>
                    ))}
                  </select>
                </div>
              )}

              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Status:</span>
                <select
                  value={ackFilter}
                  onChange={(e) => setAckFilter(e.target.value)}
                  className="bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-2.5 py-1 text-xs outline-none focus:border-primary-500"
                >
                  <option value="ALL">All Statuses</option>
                  <option value="UNACK">Unacknowledged Only</option>
                  <option value="ACK">Acknowledged Only</option>
                </select>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Event Type:</span>
                <select
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value)}
                  className="bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-2.5 py-1 text-xs outline-none focus:border-primary-500"
                >
                  <option value="ALL">All Types</option>
                  <option value="DOOR_OPEN">Door Open</option>
                  <option value="FIRE_ALARM">Fire Alarm</option>
                  <option value="POWER_FAILURE">Power Failure</option>
                  <option value="PHYSICAL_TAMPERING">Physical Tampering</option>
                  <option value="GENERAL">General</option>
                </select>
              </div>
            </div>

            {/* Alert Feed Table */}
            <div className="bg-slate-900 border border-slate-800/80 rounded-xl overflow-hidden shadow-xl">
              <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Bell className="w-5 h-5 text-primary-500" />
                  <h2 className="font-semibold text-slate-200">Security Alert Feed</h2>
                </div>
                <span className="text-xs bg-primary-950/80 text-primary-400 border border-primary-900/50 px-2.5 py-1 rounded-full font-medium flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 bg-primary-500 rounded-full animate-ping" />
                  Live Stream Active
                </span>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-slate-400 bg-slate-900/80 border-b border-slate-800">
                      <th className="text-left px-6 py-3.5 font-semibold">Severity</th>
                      <th className="text-left px-6 py-3.5 font-semibold">Type</th>
                      <th className="text-left px-6 py-3.5 font-semibold">Title</th>
                      <th className="text-left px-6 py-3.5 font-semibold">Location</th>
                      <th className="text-left px-6 py-3.5 font-semibold">Bank</th>
                      <th className="text-left px-6 py-3.5 font-semibold">AI Risk</th>
                      <th className="text-left px-6 py-3.5 font-semibold">Time</th>
                      <th className="text-left px-6 py-3.5 font-semibold">Status</th>
                      <th className="text-center px-6 py-3.5 font-semibold">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/55">
                    {filteredAlerts.map((alert) => (
                      <tr
                        key={alert.id}
                        className={`hover:bg-slate-800/20 transition-all ${
                          !alert.acknowledged && alert.severity === 'CRITICAL' ? 'bg-danger-500/5' : ''
                        }`}
                      >
                        <td className="px-6 py-4">
                          <span
                            className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-semibold border ${severityBadge(
                              alert.severity
                            )}`}
                          >
                            {alert.severity}
                          </span>
                        </td>
                        <td className="px-6 py-4 font-mono text-xs text-slate-300">{alert.alertType}</td>
                        <td className="px-6 py-4 font-semibold">{alert.title}</td>
                        <td className="px-6 py-4 text-slate-300 flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5 text-slate-500" />
                          {alert.locationName || 'Unknown Location'}
                        </td>
                        <td className="px-6 py-4 text-slate-400">{alert.bankName || `Bank Ref #${alert.bankId}`}</td>
                        <td className="px-6 py-4">
                          {alert.anomalyScore !== undefined ? (
                            <span
                              className={`inline-flex px-2 py-0.5 rounded text-xs font-semibold border ${
                                alert.isAnomaly
                                  ? 'bg-amber-950/80 text-amber-400 border-amber-800/50'
                                  : 'bg-slate-800/50 text-slate-400 border-slate-700/50'
                              }`}
                            >
                              {(alert.anomalyScore * 100).toFixed(0)}% {alert.isAnomaly ? 'Anomaly' : 'Normal'}
                            </span>
                          ) : (
                            <span className="text-slate-500">-</span>
                          )}
                        </td>
                        <td className="px-6 py-4 text-slate-500 font-medium">
                          {new Date(alert.receivedAt).toLocaleString()}
                        </td>
                        <td className="px-6 py-4">
                          {alert.acknowledged ? (
                            <span className="inline-flex items-center gap-1 text-emerald-400 text-xs font-semibold">
                              <CheckCircle className="w-3.5 h-3.5" />
                              Resolved
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 text-warning-400 text-xs font-semibold">
                              <Clock className="w-3.5 h-3.5" />
                              Pending
                            </span>
                          )}
                        </td>
                        <td className="px-6 py-4 text-center">
                          <button
                            onClick={() => setSelectedAlert(alert)}
                            className="p-1.5 rounded-lg hover:bg-slate-800 text-primary-400 hover:text-primary-300 transition-colors"
                            title="View details & acknowledge"
                          >
                            <Eye className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                    {filteredAlerts.length === 0 && (
                      <tr>
                        <td colSpan={9} className="px-6 py-12 text-center text-slate-500">
                          No alerts match your filter selections.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}

        {activeTab === 'stations' && (
          <div className="bg-slate-900 border border-slate-800/80 rounded-xl overflow-hidden shadow-xl">
            <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Server className="w-5 h-5 text-primary-500" />
                <h2 className="font-semibold text-slate-200">Registered ATM Stations</h2>
              </div>
              <button
                onClick={handleOpenCreateStation}
                className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-primary-600 hover:bg-primary-700 text-sm font-semibold transition-all"
              >
                <Plus className="w-4 h-4" />
                Register ATM Station
              </button>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-slate-400 bg-slate-900/80 border-b border-slate-800">
                    <th className="text-left px-6 py-3.5 font-semibold">Station Code</th>
                    <th className="text-left px-6 py-3.5 font-semibold">Location Name</th>
                    <th className="text-left px-6 py-3.5 font-semibold">Phone Number</th>
                    <th className="text-left px-6 py-3.5 font-semibold">Bank Name</th>
                    <th className="text-left px-6 py-3.5 font-semibold">Coordinates</th>
                    <th className="text-left px-6 py-3.5 font-semibold">Status</th>
                    <th className="text-center px-6 py-3.5 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/55">
                  {stations.map((st) => (
                    <tr key={st.id} className="hover:bg-slate-800/20 transition-all">
                      <td className="px-6 py-4 font-mono font-semibold text-slate-300">{st.stationCode}</td>
                      <td className="px-6 py-4 font-semibold">{st.locationName}</td>
                      <td className="px-6 py-4 text-slate-300 flex items-center gap-1.5">
                        <Phone className="w-3.5 h-3.5 text-slate-500" />
                        {st.phoneNumberEnc}
                      </td>
                      <td className="px-6 py-4 text-slate-400">
                        {st.bank?.name || `Bank Ref #${st.bankId}`}
                      </td>
                      <td className="px-6 py-4 text-slate-500 font-mono text-xs">
                        {st.latitude && st.longitude ? `${st.latitude}, ${st.longitude}` : 'N/A'}
                      </td>
                      <td className="px-6 py-4">
                        <span
                          className={`inline-flex px-2 py-0.5 rounded-full text-xs font-semibold border ${
                            st.active
                              ? 'bg-emerald-950/80 text-emerald-400 border-emerald-800/50'
                              : 'bg-slate-800/50 text-slate-500 border-slate-700/50'
                          }`}
                        >
                          {st.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <div className="flex gap-2 justify-center">
                          <button
                            onClick={() => handleOpenEditStation(st)}
                            className="p-1.5 rounded-lg hover:bg-slate-800 text-amber-500 hover:text-amber-400 transition-colors"
                            title="Edit station"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => st.id && handleDeleteStation(st.id)}
                            className="p-1.5 rounded-lg hover:bg-slate-800 text-danger-500 hover:text-danger-400 transition-colors"
                            title="Delete station"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  {stations.length === 0 && (
                    <tr>
                      <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                        No registered ATM stations found.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* Admin Popup Alert */}
      {showAdminPopup && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-slate-900 border border-danger-500/40 rounded-2xl max-w-lg w-full shadow-2xl overflow-hidden transform animate-in fade-in zoom-in duration-200">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-danger-500/10">
              <div className="flex items-center gap-2 text-danger-500">
                <AlertTriangle className="w-5.5 h-5.5 animate-bounce" />
                <h3 className="font-bold text-lg">CRITICAL SECURITY BREACH ALERT</h3>
              </div>
              <button onClick={() => setShowAdminPopup(false)}>
                <X className="w-5 h-5 text-slate-400 hover:text-white" />
              </button>
            </div>
            <div className="p-6 space-y-4 max-h-80 overflow-y-auto">
              <p className="text-sm text-slate-400">
                The following critical security incident was reported by a standalone ATM and requires immediate response:
              </p>
              {criticalAlerts.map((alert) => (
                <div
                  key={alert.id}
                  className="p-4 rounded-xl bg-danger-500/10 border border-danger-500/20 space-y-1.5"
                >
                  <p className="font-bold text-slate-200">{alert.title}</p>
                  <p className="text-xs text-slate-400 font-medium">{alert.message}</p>
                  <div className="flex justify-between items-center pt-1.5 text-xs text-slate-500 border-t border-slate-800">
                    <span>ATM: {alert.locationName}</span>
                    <span>{new Date(alert.receivedAt).toLocaleTimeString()}</span>
                  </div>
                </div>
              ))}
            </div>
            <div className="px-6 py-4 border-t border-slate-800 bg-slate-950/30 flex gap-3">
              <button
                onClick={() => setShowAdminPopup(false)}
                className="flex-1 py-2.5 bg-slate-800 hover:bg-slate-700 rounded-xl font-semibold transition-all text-sm"
              >
                Close Warning
              </button>
              <button
                onClick={() => {
                  setShowAdminPopup(false);
                  if (criticalAlerts.length > 0) {
                    setSelectedAlert(criticalAlerts[0]);
                  }
                }}
                className="flex-1 py-2.5 bg-danger-600 hover:bg-danger-700 rounded-xl font-semibold transition-all text-sm text-white"
              >
                Respond Immediately
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Alert Details Modal */}
      {selectedAlert && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full overflow-hidden shadow-2xl">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
              <h3 className="font-bold text-lg text-slate-200">Alert Analysis & Incident Log</h3>
              <button onClick={() => setSelectedAlert(null)}>
                <X className="w-5 h-5 text-slate-400 hover:text-white" />
              </button>
            </div>
            <div className="p-6 space-y-4 max-h-[70vh] overflow-y-auto">
              <div className="grid grid-cols-2 gap-4">
                <DetailRow label="Severity" value={selectedAlert.severity} />
                <DetailRow label="Alert Type" value={selectedAlert.alertType} />
                <DetailRow label="Title" value={selectedAlert.title} />
                <DetailRow label="Location" value={selectedAlert.locationName || 'N/A'} />
                <DetailRow label="Bank Scope" value={selectedAlert.bankName || `Bank Ref ID #${selectedAlert.bankId}`} />
                <DetailRow label="Alert Zone" value={selectedAlert.zone || 'general'} />
                <DetailRow label="AI Threat Level" value={selectedAlert.anomalyScore ? `${(selectedAlert.anomalyScore * 100).toFixed(0)}% (${selectedAlert.isAnomaly ? 'ANOMALOUS' : 'NORMAL'})` : 'N/A'} />
                <DetailRow label="Received At" value={new Date(selectedAlert.receivedAt).toLocaleString()} />
              </div>

              <div>
                <p className="text-xs text-slate-500 uppercase tracking-wide font-semibold">ATM Event Description</p>
                <div className="mt-1 p-3 rounded-lg bg-slate-950 border border-slate-800 text-sm text-slate-300 font-mono">
                  {selectedAlert.message}
                </div>
              </div>

              {selectedAlert.acknowledged ? (
                <div className="p-4 rounded-xl bg-emerald-950/20 border border-emerald-800/30">
                  <p className="text-xs text-emerald-400 uppercase tracking-wide font-bold">Response Note</p>
                  <p className="text-sm mt-1 text-slate-300">{selectedAlert.responseNotes || 'No notes logged.'}</p>
                </div>
              ) : (
                <div className="space-y-2.5 pt-2 border-t border-slate-800">
                  <label className="text-xs text-slate-400 font-semibold">Incident Response & Action Notes</label>
                  <textarea
                    value={ackNotes}
                    onChange={(e) => setAckNotes(e.target.value)}
                    placeholder="Enter details about investigation or dispatching security guards..."
                    className="w-full h-20 bg-slate-950 border border-slate-800 rounded-xl p-3 text-sm outline-none focus:border-primary-500 text-slate-200"
                  />
                  <button
                    onClick={handleAcknowledgeAlert}
                    className="w-full py-2.5 bg-primary-600 hover:bg-primary-700 rounded-xl font-semibold transition-all text-sm text-white"
                  >
                    Acknowledge & Resolve Incident
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Station Create/Edit Modal */}
      {showStationModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <form onSubmit={handleSaveStation} className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full overflow-hidden shadow-2xl">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
              <h3 className="font-bold text-lg text-slate-200">
                {editingStation ? 'Edit ATM Station' : 'Register ATM Station'}
              </h3>
              <button type="button" onClick={() => setShowStationModal(false)}>
                <X className="w-5 h-5 text-slate-400 hover:text-white" />
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Station Code</label>
                  <input
                    type="text"
                    required
                    value={stationCode}
                    onChange={(e) => setStationCode(e.target.value)}
                    placeholder="ST-COLOMBO-01"
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">ATM Phone Number</label>
                  <input
                    type="text"
                    required
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    placeholder="+94771234567"
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Location Name</label>
                <input
                  type="text"
                  required
                  value={locationName}
                  onChange={(e) => setLocationName(e.target.value)}
                  placeholder="Colombo Fort Branch ATM"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Detailed Address / Description</label>
                <input
                  type="text"
                  value={locationAddress}
                  onChange={(e) => setLocationAddress(e.target.value)}
                  placeholder="No. 45, York Street, Colombo 01"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                {user?.role === 'ADMIN' ? (
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">Assigned Bank</label>
                    <select
                      value={stationBankId}
                      onChange={(e) => setStationBankId(parseInt(e.target.value))}
                      className="w-full bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500"
                    >
                      {banks.map(b => (
                        <option key={b.id} value={b.id}>{b.name}</option>
                      ))}
                    </select>
                  </div>
                ) : (
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">Bank Scope</label>
                    <input
                      type="text"
                      disabled
                      value={user?.bankName || ''}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-500 cursor-not-allowed"
                    />
                  </div>
                )}
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">Latitude</label>
                    <input
                      type="text"
                      value={latitude}
                      onChange={(e) => setLatitude(e.target.value)}
                      placeholder="6.93"
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">Longitude</label>
                    <input
                      type="text"
                      value={longitude}
                      onChange={(e) => setLongitude(e.target.value)}
                      placeholder="79.84"
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div className="px-6 py-4 border-t border-slate-800 bg-slate-950/30 flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowStationModal(false)}
                className="px-4 py-2 border border-slate-800 hover:bg-slate-800 rounded-lg text-sm transition-all"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 bg-primary-600 hover:bg-primary-700 rounded-lg text-sm font-semibold transition-all text-white"
              >
                Save Station
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Mock User Modal */}
      {showUserModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <form onSubmit={handleCreateUser} className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full overflow-hidden shadow-2xl">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
              <h3 className="font-bold text-lg text-slate-200">Register System User</h3>
              <button type="button" onClick={() => setShowUserModal(false)}>
                <X className="w-5 h-5 text-slate-400 hover:text-white" />
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Username</label>
                <input
                  type="text"
                  required
                  value={newUsername}
                  onChange={(e) => setNewUsername(e.target.value)}
                  placeholder="johndoe"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Full Name</label>
                <input
                  type="text"
                  required
                  value={newFullName}
                  onChange={(e) => setNewFullName(e.target.value)}
                  placeholder="John Doe"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500 text-slate-200"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Role</label>
                  <select
                    value={newUserRole}
                    onChange={(e) => setNewUserRole(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500"
                  >
                    <option value="ADMIN">Admin</option>
                    <option value="BANK_MANAGER">Bank Manager</option>
                    <option value="SECURITY_PERSONNEL">Security Personnel</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Assigned Bank</label>
                  <select
                    value={newUserBankId}
                    onChange={(e) => setNewUserBankId(parseInt(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 text-slate-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-primary-500"
                  >
                    {banks.map(b => (
                      <option key={b.id} value={b.id}>{b.name}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
            <div className="px-6 py-4 border-t border-slate-800 bg-slate-950/30 flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowUserModal(false)}
                className="px-4 py-2 border border-slate-800 hover:bg-slate-800 rounded-lg text-sm transition-all"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 bg-primary-600 hover:bg-primary-700 rounded-lg text-sm font-semibold transition-all text-white"
              >
                Create User
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Report Modal */}
      {showReportModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full overflow-hidden shadow-2xl">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
              <h3 className="font-bold text-lg text-slate-200">Generate Audit Security Reports</h3>
              <button onClick={() => setShowReportModal(false)}>
                <X className="w-5 h-5 text-slate-400 hover:text-white" />
              </button>
            </div>
            <div className="p-6 space-y-4">
              <p className="text-sm text-slate-400">
                Choose the export format to compile the historical security logs and response statistics:
              </p>
              <div className="grid grid-cols-2 gap-4">
                <button
                  onClick={() => handleDownloadReport('csv')}
                  className="p-4 rounded-xl border border-slate-800 bg-slate-950 hover:bg-slate-800 hover:border-slate-700 text-center space-y-2 transition-all"
                >
                  <FileText className="w-8 h-8 text-primary-500 mx-auto" />
                  <p className="font-bold text-sm">Download CSV</p>
                  <p className="text-xs text-slate-500">Spreadsheet ready format</p>
                </button>
                <button
                  onClick={() => handleDownloadReport('json')}
                  className="p-4 rounded-xl border border-slate-800 bg-slate-950 hover:bg-slate-800 hover:border-slate-700 text-center space-y-2 transition-all"
                >
                  <FileText className="w-8 h-8 text-emerald-500 mx-auto" />
                  <p className="font-bold text-sm">Download JSON</p>
                  <p className="text-xs text-slate-500">Raw database integration format</p>
                </button>
              </div>
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
  icon,
}: {
  label: string;
  value: number;
  color: string;
  icon: React.ReactNode;
}) {
  return (
    <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-5 flex items-center justify-between shadow-md hover:border-slate-700/60 transition-all">
      <div>
        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide">{label}</p>
        <p className={`text-3xl font-extrabold mt-1.5 ${color}`}>{value}</p>
      </div>
      <div className="p-3 bg-slate-950 rounded-xl border border-slate-800/50 shadow-inner">
        {icon}
      </div>
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="space-y-0.5">
      <p className="text-xs text-slate-500 uppercase tracking-wide font-semibold">{label}</p>
      <p className="text-sm text-slate-200 font-semibold bg-slate-950/40 border border-slate-800/30 px-3 py-1.5 rounded-lg">{value}</p>
    </div>
  );
}
