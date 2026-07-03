import React from 'react';
import { X, MapPin, Clock, Radio, AlertTriangle, ChevronRight } from 'lucide-react';
import StatusBadge from './StatusBadge';

export default function AlertDetailsPanel({ alert, isOpen, onClose }) {
  if (!isOpen || !alert) return null;

  // Zone badges render කරන function එක
  const renderZoneBadges = (zoneNumbers) => {
    if (!zoneNumbers || zoneNumbers === '00' || zoneNumbers === '0') {
      return <span className="text-slate-500 text-sm">No Zone</span>;
    }

    const zones = zoneNumbers.split(',').map(z => z.trim()).filter(z => z !== '');
    
    if (zones.length === 0) {
      return <span className="text-slate-500 text-sm">No Zone</span>;
    }

    return (
      <div className="flex flex-wrap gap-2">
        {zones.map((zone, index) => (
          <span 
            key={index}
            className="px-3 py-1 bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-lg text-sm font-bold"
          >
            Zone {String(zone).padStart(2, '0')}
          </span>
        ))}
      </div>
    );
  };

  return (
    <>
      {/* Mobile: Full screen overlay */}
      <div className="lg:hidden fixed inset-0 z-50 bg-black/70 backdrop-blur-sm" onClick={onClose}>
        <div 
          className="absolute bottom-0 left-0 right-0 bg-slate-900 rounded-t-2xl max-h-[80vh] overflow-y-auto p-6 border-t border-slate-700"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-bold text-white">Alert Details</h3>
            <button onClick={onClose} className="p-2 hover:bg-slate-800 rounded-lg">
              <X className="w-5 h-5 text-slate-400" />
            </button>
          </div>
          <PanelContent alert={alert} renderZoneBadges={renderZoneBadges} />
        </div>
      </div>

      {/* Desktop: Right side panel */}
      <div className={`hidden lg:block fixed top-0 right-0 h-full w-[420px] bg-slate-900 border-l border-slate-800 shadow-2xl transform transition-transform duration-300 z-40 ${
        isOpen ? 'translate-x-0' : 'translate-x-full'
      }`}>
        <div className="p-6 h-full overflow-y-auto">
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <ChevronRight className="w-5 h-5 text-red-500" />
              Alert Details
            </h3>
            <button onClick={onClose} className="p-2 hover:bg-slate-800 rounded-lg">
              <X className="w-5 h-5 text-slate-400" />
            </button>
          </div>
          <PanelContent alert={alert} renderZoneBadges={renderZoneBadges} />
        </div>
      </div>
    </>
  );
}

function PanelContent({ alert, renderZoneBadges }) {
  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-3">
        <StatusBadge status={alert.status} />
        <span className="text-sm font-mono text-white bg-slate-800 px-3 py-1 rounded-lg">
          {alert.alarmSystem?.systemCode || 'UNKNOWN'}
        </span>
      </div>

      <div className="flex items-center gap-2 text-slate-300">
        <MapPin className="w-4 h-4 text-slate-500 flex-shrink-0" />
        <span className="text-sm">{alert.alarmSystem?.location || 'Unknown Location'}</span>
      </div>

      <div className="flex items-start gap-2">
        <Radio className="w-4 h-4 text-slate-500 flex-shrink-0 mt-1" />
        <div className="flex flex-wrap gap-2">
          {renderZoneBadges(alert.zoneNumbers)}
        </div>
      </div>

      <div className="bg-slate-950 border border-slate-800 rounded-xl p-4">
        <p className="text-xs text-slate-400 mb-2 uppercase tracking-wider">📨 Full Message</p>
        <p className="text-white font-mono text-sm break-words leading-relaxed">
          {alert.rawMessage || alert.alertType || 'No message'}
        </p>
      </div>

      <div className="flex items-center gap-2 text-slate-400 text-sm border-t border-slate-800 pt-4">
        <Clock className="w-4 h-4 flex-shrink-0" />
        <span>{new Date(alert.receivedAt).toLocaleString()}</span>
      </div>
    </div>
  );
}