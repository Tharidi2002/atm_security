import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { MapPin, Clock, MessageSquare, Phone, Bell, AlertTriangle, CheckCircle, Timer } from 'lucide-react';
import StatusBadge from './StatusBadge';
import AlertModal from './AlertModal';
import AlertDetailsPanel from './AlertDetailsPanel';
import AlertResolveModal from './AlertResolveModal';
import LoadingSkeleton from './LoadingSkeleton';

export default function AlertTable({ alerts, loading, tableContainerRef, username, onAlertResolved }) {
  const [selectedAlert, setSelectedAlert] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isPanelOpen, setIsPanelOpen] = useState(false);
  const [resolveAlertData, setResolveAlertData] = useState(null);
  const [isResolveModalOpen, setIsResolveModalOpen] = useState(false);
  const [, setForceUpdate] = useState({});

  // Update every second for live pending duration
  useEffect(() => {
    const interval = setInterval(() => {
      setForceUpdate({});
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const getMessageIcon = (alertType) => {
    if (!alertType) return <MessageSquare className="w-4 h-4 text-slate-400 flex-shrink-0" />;
    
    const lower = alertType.toLowerCase();
    
    if (lower.includes('call incoming') || lower.includes('call from')) {
      return <Phone className="w-4 h-4 text-blue-400 flex-shrink-0" />;
    }
    if (lower.includes('call') || lower.includes('voice') || lower.includes('incoming')) {
      return <Phone className="w-4 h-4 text-blue-400 flex-shrink-0" />;
    }
    if (lower.includes('alarm') || lower.includes('alert')) {
      return <Bell className="w-4 h-4 text-red-400 flex-shrink-0" />;
    }
    if (lower.includes('zone')) {
      return <AlertTriangle className="w-4 h-4 text-amber-400 flex-shrink-0" />;
    }
    return <MessageSquare className="w-4 h-4 text-slate-400 flex-shrink-0" />;
  };

  const getMessagePreview = (alertType) => {
    if (!alertType) return 'No message';
    if (alertType.length > 35) {
      return alertType.substring(0, 35) + '...';
    }
    return alertType;
  };

  const getPendingDuration = (receivedAt) => {
    if (!receivedAt) return 'N/A';
    const now = new Date();
    const received = new Date(receivedAt);
    const diffMs = now - received;
    
    if (diffMs < 0) return 'N/A';
    
    const diffSecs = Math.floor(diffMs / 1000);
    const diffMins = Math.floor(diffSecs / 60);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);
    
    let durationStr = '';
    
    if (diffDays > 0) {
      durationStr += `${diffDays}d `;
    }
    if (diffHours % 24 > 0) {
      durationStr += `${diffHours % 24}h `;
    }
    if (diffMins % 60 > 0) {
      durationStr += `${diffMins % 60}m `;
    }
    if (diffSecs % 60 > 0) {
      durationStr += `${diffSecs % 60}s`;
    }
    
    return durationStr.trim() || '0s';
  };

  const handleRowClick = (alert) => {
    setSelectedAlert(alert);
    setIsPanelOpen(true);
  };

  const handleMessageClick = (e, alert) => {
    e.stopPropagation();
    setSelectedAlert(alert);
    setIsModalOpen(true);
  };

  const handleResolveClick = (e, alert) => {
    e.stopPropagation();
    setResolveAlertData(alert);
    setIsResolveModalOpen(true);
  };

  const handleResolved = (resolvedAlert) => {
    setSelectedAlert(resolvedAlert);
    if (onAlertResolved) {
      onAlertResolved();
    }
    setIsResolveModalOpen(false);
  };

  const renderZoneBadges = (zoneNumbers) => {
    if (!zoneNumbers || zoneNumbers === '00' || zoneNumbers === '0') {
      return <span className="text-slate-500 text-[10px]">No Zone</span>;
    }

    const zones = zoneNumbers.split(',').map(z => z.trim()).filter(z => z !== '');
    
    if (zones.length === 0) {
      return <span className="text-slate-500 text-[10px]">No Zone</span>;
    }

    return (
      <div className="flex flex-wrap gap-1">
        {zones.map((zone, index) => (
          <span 
            key={index}
            className="inline-flex items-center px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30 whitespace-nowrap"
          >
            Z{String(zone).padStart(2, '0')}
          </span>
        ))}
      </div>
    );
  };

  if (loading) {
    return <LoadingSkeleton />;
  }

  if (!alerts || alerts.length === 0) {
    return (
      <div className="bg-slate-950 border border-slate-800 rounded-xl p-12 text-center">
        <div className="text-emerald-400 font-mono text-lg">🎉 System Secure. No alerts.</div>
      </div>
    );
  }

  return (
    <>
      <div 
        ref={tableContainerRef}
        className="bg-slate-950 border border-slate-800 rounded-xl overflow-hidden overflow-y-auto max-h-[600px] scroll-smooth"
      >
        {/* ===== DESKTOP TABLE ===== */}
        <div className="hidden lg:block overflow-x-auto">
          <table className="w-full text-left min-w-[900px]">
            <thead className="sticky top-0 bg-slate-900/95 text-slate-400 uppercase text-xs tracking-wider border-b border-slate-800 font-mono z-10 backdrop-blur">
              <tr>
                <th className="py-3 px-4 w-[100px]">Status</th>
                <th className="py-3 px-4 w-[130px]">System</th>
                <th className="py-3 px-4 w-[130px]">Location</th>
                <th className="py-3 px-4 w-[100px]">Zones</th>
                <th className="py-3 px-4 w-[180px]">Message</th>
                <th className="py-3 px-4 w-[120px]">Time</th>
                <th className="py-3 px-4 w-[100px] text-center">Pending</th>
                <th className="py-3 px-4 w-[90px] text-center">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/50">
              {alerts.map((alert) => {
                const isPending = alert.status === 'PENDING';
                return (
                  <tr 
                    key={alert.id} 
                    onClick={() => handleRowClick(alert)}
                    className="hover:bg-slate-900/40 transition-colors cursor-pointer"
                  >
                    <td className="py-3 px-4">
                      <StatusBadge status={alert.status} />
                    </td>
                    <td className="py-3 px-4 font-mono font-bold text-white text-sm truncate max-w-[130px]">
                      {alert.alarmSystem?.systemCode || 'UNKNOWN'}
                    </td>
                    <td className="py-3 px-4 text-slate-300 text-sm">
                      <div className="flex items-center gap-1 min-w-0">
                        <MapPin className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                        <span className="truncate">
                          {alert.alarmSystem?.location || 'Unknown'}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      {renderZoneBadges(alert.zoneNumbers)}
                    </td>
                    <td className="py-3 px-4 max-w-[180px]">
                      <button
                        onClick={(e) => handleMessageClick(e, alert)}
                        className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors group w-full min-w-0"
                      >
                        <span className="flex-shrink-0">
                          {getMessageIcon(alert.alertType)}
                        </span>
                        <span className="text-sm font-mono group-hover:text-blue-400 transition-colors truncate block w-full">
                          {getMessagePreview(alert.alertType)}
                        </span>
                      </button>
                    </td>
                    <td className="py-3 px-4 text-slate-400 text-xs font-mono whitespace-nowrap">
                      <div className="flex items-center gap-1">
                        <Clock className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                        {new Date(alert.receivedAt).toLocaleTimeString()}
                      </div>
                    </td>
                    <td className="py-3 px-4 text-center">
                      {isPending ? (
                        <span className="inline-flex items-center gap-1 text-yellow-400 font-mono text-xs font-bold whitespace-nowrap">
                          <Timer className="w-3.5 h-3.5 flex-shrink-0" />
                          {getPendingDuration(alert.receivedAt)}
                        </span>
                      ) : (
                        <span className="text-[10px] text-slate-500 font-mono">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-center">
                      {isPending ? (
                        <button
                          onClick={(e) => handleResolveClick(e, alert)}
                          className="inline-flex items-center gap-1 px-2.5 py-1 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 hover:text-emerald-300 border border-emerald-500/30 hover:border-emerald-500/50 rounded-lg text-[10px] font-mono transition-all whitespace-nowrap"
                          title="Resolve this alert"
                        >
                          <CheckCircle className="w-3.5 h-3.5 flex-shrink-0" />
                          Resolve
                        </button>
                      ) : (
                        <span className="text-[10px] text-slate-500 font-mono">—</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* ===== TABLET VIEW ===== */}
        <div className="hidden sm:block lg:hidden overflow-x-auto">
          <table className="w-full text-left min-w-[650px]">
            <thead className="sticky top-0 bg-slate-900/95 text-slate-400 uppercase text-[10px] tracking-wider border-b border-slate-800 font-mono z-10 backdrop-blur">
              <tr>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">System</th>
                <th className="py-2.5 px-3">Zones</th>
                <th className="py-2.5 px-3">Message</th>
                <th className="py-2.5 px-3">Time</th>
                <th className="py-2.5 px-3 text-center">Pending</th>
                <th className="py-2.5 px-3 text-center">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/50">
              {alerts.map((alert) => {
                const isPending = alert.status === 'PENDING';
                return (
                  <tr 
                    key={alert.id} 
                    onClick={() => handleRowClick(alert)}
                    className="hover:bg-slate-900/40 transition-colors cursor-pointer"
                  >
                    <td className="py-2.5 px-3">
                      <StatusBadge status={alert.status} />
                    </td>
                    <td className="py-2.5 px-3 font-mono font-bold text-white text-xs truncate max-w-[100px]">
                      {alert.alarmSystem?.systemCode || 'UNKNOWN'}
                    </td>
                    <td className="py-2.5 px-3">
                      {renderZoneBadges(alert.zoneNumbers)}
                    </td>
                    <td className="py-2.5 px-3 max-w-[150px]">
                      <button
                        onClick={(e) => handleMessageClick(e, alert)}
                        className="flex items-center gap-1.5 text-slate-400 hover:text-white transition-colors group w-full min-w-0"
                      >
                        <span className="flex-shrink-0">
                          {getMessageIcon(alert.alertType)}
                        </span>
                        <span className="text-xs font-mono group-hover:text-blue-400 transition-colors truncate block w-full">
                          {getMessagePreview(alert.alertType)}
                        </span>
                      </button>
                    </td>
                    <td className="py-2.5 px-3 text-slate-400 text-[10px] font-mono whitespace-nowrap">
                      {new Date(alert.receivedAt).toLocaleTimeString()}
                    </td>
                    <td className="py-2.5 px-3 text-center">
                      {isPending ? (
                        <span className="text-yellow-400 font-mono text-[10px] font-bold whitespace-nowrap">
                          {getPendingDuration(alert.receivedAt)}
                        </span>
                      ) : (
                        <span className="text-[10px] text-slate-500 font-mono">—</span>
                      )}
                    </td>
                    <td className="py-2.5 px-3 text-center">
                      {isPending ? (
                        <button
                          onClick={(e) => handleResolveClick(e, alert)}
                          className="inline-flex items-center gap-1 px-2 py-0.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-lg text-[10px] font-mono transition-all whitespace-nowrap"
                          title="Resolve"
                        >
                          <CheckCircle className="w-3 h-3 flex-shrink-0" />
                          Resolve
                        </button>
                      ) : (
                        <span className="text-[10px] text-slate-500 font-mono">—</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* ===== MOBILE CARDS ===== */}
        <div className="sm:hidden divide-y divide-slate-800">
          {alerts.map((alert) => {
            const isPending = alert.status === 'PENDING';
            return (
              <div 
                key={alert.id}
                onClick={() => handleRowClick(alert)}
                className="p-3 hover:bg-slate-900/40 transition-colors cursor-pointer space-y-2"
              >
                {/* Row 1: Status + System + Action */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 flex-wrap min-w-0">
                    <StatusBadge status={alert.status} />
                    <span className="font-mono font-bold text-white text-xs truncate max-w-[100px]">
                      {alert.alarmSystem?.systemCode || 'UNKNOWN'}
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5 flex-shrink-0">
                    {isPending && (
                      <button
                        onClick={(e) => handleResolveClick(e, alert)}
                        className="p-1.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-lg transition-all"
                        title="Resolve"
                      >
                        <CheckCircle className="w-3.5 h-3.5" />
                      </button>
                    )}
                    <button
                      onClick={(e) => handleMessageClick(e, alert)}
                      className="p-1.5 bg-slate-800 rounded-lg hover:bg-slate-700 transition-colors flex-shrink-0"
                    >
                      {getMessageIcon(alert.alertType)}
                    </button>
                  </div>
                </div>

                {/* Row 2: Location + Zones */}
                <div className="flex flex-wrap items-center gap-1 text-xs text-slate-400 min-w-0">
                  <MapPin className="w-3 h-3 flex-shrink-0" />
                  <span className="truncate max-w-[120px]">
                    {alert.alarmSystem?.location || 'Unknown'}
                  </span>
                  <span className="text-slate-600 mx-1 flex-shrink-0">•</span>
                  {renderZoneBadges(alert.zoneNumbers)}
                </div>

                {/* Row 3: Message Preview + Time */}
                <div className="flex items-center justify-between gap-2 text-xs">
                  <button
                    onClick={(e) => handleMessageClick(e, alert)}
                    className="flex items-center gap-1.5 text-slate-400 hover:text-white transition-colors group min-w-0 flex-1"
                  >
                    <span className="flex-shrink-0">
                      {getMessageIcon(alert.alertType)}
                    </span>
                    <span className="truncate font-mono text-[10px] block w-full">
                      {getMessagePreview(alert.alertType)}
                    </span>
                  </button>
                  <div className="flex items-center gap-1 text-slate-500 text-[10px] font-mono flex-shrink-0">
                    <Clock className="w-3 h-3" />
                    {new Date(alert.receivedAt).toLocaleTimeString()}
                  </div>
                </div>

                {/* Row 4: Pending Duration */}
                {isPending && (
                  <div className="flex items-center gap-1 text-yellow-400 text-[10px] font-mono font-bold">
                    <Timer className="w-3 h-3 flex-shrink-0" />
                    Pending: {getPendingDuration(alert.receivedAt)}
                  </div>
                )}

                {/* Mobile Resolve Button */}
                {isPending && (
                  <button
                    onClick={(e) => handleResolveClick(e, alert)}
                    className="w-full mt-1 py-1.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-lg text-[10px] font-mono transition-all flex items-center justify-center gap-1.5"
                  >
                    <CheckCircle className="w-3.5 h-3.5 flex-shrink-0" />
                    Resolve Alert
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* ===== MODALS ===== */}
      <AlertModal 
        alert={selectedAlert} 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
      />
      
      <AlertDetailsPanel 
        alert={selectedAlert} 
        isOpen={isPanelOpen} 
        onClose={() => setIsPanelOpen(false)}
        onResolved={handleResolved}
        username={username}
      />

      <AlertResolveModal
        alert={resolveAlertData}
        isOpen={isResolveModalOpen}
        onClose={() => {
          setIsResolveModalOpen(false);
          setResolveAlertData(null);
        }}
        onResolved={handleResolved}
        username={username}
      />
    </>
  );
}

AlertTable.propTypes = {
  alerts: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired,
  tableContainerRef: PropTypes.object,
  username: PropTypes.string.isRequired,
  onAlertResolved: PropTypes.func,
};