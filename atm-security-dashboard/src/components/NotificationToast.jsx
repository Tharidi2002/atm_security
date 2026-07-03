import React, { useEffect, useState } from 'react';
import { Bell, X } from 'lucide-react';

export default function NotificationToast({ alert, onClose }) {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(false);
      setTimeout(onClose, 300);
    }, 5000); // තත්පර 5කින් auto close වෙනවා

    return () => clearTimeout(timer);
  }, [onClose]);

  if (!isVisible) return null;

  return (
    <div className="fixed top-4 right-4 z-50 max-w-sm w-full animate-slide-in">
      <div className="bg-slate-900 border border-red-500/30 rounded-xl shadow-2xl p-4">
        <div className="flex items-start gap-3">
          <div className="bg-red-500/10 p-2 rounded-lg border border-red-500/20 flex-shrink-0">
            <Bell className="w-5 h-5 text-red-500 animate-pulse" />
          </div>
          
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-red-400 uppercase tracking-wider">New Alert!</span>
              <span className="text-xs text-slate-500">{alert.alarmSystem?.systemCode || 'UNKNOWN'}</span>
            </div>
            <p className="text-sm text-slate-300 truncate mt-1">
              {alert.alertType?.substring(0, 60) || 'New alert received'}
            </p>
            <p className="text-xs text-slate-500 mt-1">
              {new Date(alert.receivedAt).toLocaleTimeString()}
            </p>
          </div>

          <button 
            onClick={() => {
              setIsVisible(false);
              setTimeout(onClose, 300);
            }}
            className="p-1 hover:bg-slate-800 rounded-lg transition-colors flex-shrink-0"
          >
            <X className="w-4 h-4 text-slate-400" />
          </button>
        </div>
      </div>
    </div>
  );
}