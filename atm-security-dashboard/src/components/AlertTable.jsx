import React, { useState } from 'react';
import { MapPin, Clock, MessageSquare, Phone, Bell, AlertTriangle } from 'lucide-react';
import StatusBadge from './StatusBadge';
import AlertModal from './AlertModal';
import AlertDetailsPanel from './AlertDetailsPanel';
import LoadingSkeleton from './LoadingSkeleton';

export default function AlertTable({ alerts, loading, tableContainerRef }) {
  const [selectedAlert, setSelectedAlert] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isPanelOpen, setIsPanelOpen] = useState(false);

  const getMessageIcon = (alertType) => {
    if (!alertType) return <MessageSquare className="w-4 h-4 text-slate-400" />;
    
    const lower = alertType.toLowerCase();
    if (lower.includes('call') || lower.includes('voice') || lower.includes('incoming')) {
      return <Phone className="w-4 h-4 text-blue-400" />;
    }
    if (lower.includes('alarm') || lower.includes('alert')) {
      return <Bell className="w-4 h-4 text-red-400" />;
    }
    if (lower.includes('zone')) {
      return <AlertTriangle className="w-4 h-4 text-amber-400" />;
    }
    return <MessageSquare className="w-4 h-4 text-slate-400" />;
  };

  const getMessagePreview = (alertType) => {
    if (!alertType) return 'No message';
    if (alertType.length > 50) {
      return alertType.substring(0, 50) + '...';
    }
    return alertType;
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

  // Zone badges render කරන function එක - ALL zones පෙන්වන්න
  const renderZoneBadges = (zoneNumbers) => {
    if (!zoneNumbers || zoneNumbers === '00' || zoneNumbers === '0') {
      return <span className="text-slate-500 text-xs">No Zone</span>;
    }

    const zones = zoneNumbers.split(',').map(z => z.trim()).filter(z => z !== '');
    
    if (zones.length === 0) {
      return <span className="text-slate-500 text-xs">No Zone</span>;
    }

    return (
      <div className="flex flex-wrap gap-1">
        {zones.map((zone, index) => (
          <span 
            key={index}
            className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30 whitespace-nowrap"
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

  if (alerts.length === 0) {
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
        {/* Desktop Table */}
        <div className="hidden lg:block">
          <table className="w-full text-left">
            <thead className="sticky top-0 bg-slate-900/95 text-slate-400 uppercase text-xs tracking-wider border-b border-slate-800 font-mono z-10 backdrop-blur">
              <tr>
                <th className="py-4 px-6">Status</th>
                <th className="py-4 px-6">Alarm System</th>
                <th className="py-4 px-6">Location</th>
                <th className="py-4 px-6">Zones</th>
                <th className="py-4 px-6">Message</th>
                <th className="py-4 px-6">Time</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/50">
              {alerts.map((alert) => (
                <tr 
                  key={alert.id} 
                  onClick={() => handleRowClick(alert)}
                  className="hover:bg-slate-900/40 transition-colors cursor-pointer"
                >
                  <td className="py-4 px-6">
                    <StatusBadge status={alert.status} />
                  </td>
                  <td className="py-4 px-6 font-mono font-bold text-white text-sm">
                    {alert.alarmSystem?.systemCode || 'UNKNOWN'}
                  </td>
                  <td className="py-4 px-6 text-slate-300 text-sm">
                    <div className="flex items-center gap-1">
                       <MapPin className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                      <span className="truncate max-w-[150px]">
                        {alert.alarmSystem?.location || 'Unknown'}
                      </span>
                    </div>
                  </td>
                  <td className="py-4 px-6">
                    {renderZoneBadges(alert.zoneNumbers)}
                  </td>
                  <td className="py-4 px-6">
                    <button
                      onClick={(e) => handleMessageClick(e, alert)}
                      className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors group max-w-xs"
                    >
                      {getMessageIcon(alert.alertType)}
                      <span className="text-sm font-mono group-hover:text-blue-400 transition-colors truncate">
                        {getMessagePreview(alert.alertType)}
                      </span>
                    </button>
                  </td>
                  <td className="py-4 px-6 text-slate-400 text-xs font-mono whitespace-nowrap">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                      {new Date(alert.receivedAt).toLocaleString()}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Mobile Cards - ALL zones පෙන්වන්න */}
        <div className="lg:hidden divide-y divide-slate-800">
          {alerts.map((alert) => (
            <div 
              key={alert.id}
              onClick={() => handleRowClick(alert)}
              className="p-4 hover:bg-slate-900/40 transition-colors cursor-pointer"
            >
              <div className="flex justify-between items-start mb-2">
                <div className="flex items-center gap-2 flex-wrap">
                  <StatusBadge status={alert.status} />
                  <span className="font-mono font-bold text-white text-sm">
                    {alert.alarmSystem?.systemCode || 'UNKNOWN'}
                  </span>
                </div>
                <button
                  onClick={(e) => handleMessageClick(e, alert)}
                  className="p-1.5 bg-slate-800 rounded-lg hover:bg-slate-700 transition-colors flex-shrink-0"
                >
                  {getMessageIcon(alert.alertType)}
                </button>
              </div>
              
              <div className="flex items-center gap-1 text-slate-400 text-xs mb-1">
                <MapPin className="w-3 h-3 flex-shrink-0" />
                <span className="truncate">{alert.alarmSystem?.location || 'Unknown'}</span>
              </div>

              {/* ALL zones පෙන්වන්න */}
              <div className="flex items-center gap-1 mb-1 flex-wrap">
                {renderZoneBadges(alert.zoneNumbers)}
              </div>

              <div className="flex items-center gap-1 text-slate-400 text-xs">
                <Clock className="w-3 h-3 flex-shrink-0" />
                {new Date(alert.receivedAt).toLocaleString()}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Modals */}
      <AlertModal 
        alert={selectedAlert} 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
      />
      
      <AlertDetailsPanel 
        alert={selectedAlert} 
        isOpen={isPanelOpen} 
        onClose={() => setIsPanelOpen(false)} 
      />
    </>
  );
}