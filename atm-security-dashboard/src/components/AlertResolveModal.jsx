import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { X, CheckCircle, AlertCircle, User, FileText, Calendar, Timer } from 'lucide-react';
import { resolveAlert } from '../services/api';

export default function AlertResolveModal({ alert, isOpen, onClose, onResolved, username }) {
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [pendingDuration, setPendingDuration] = useState('');

  useEffect(() => {
    if (isOpen && alert) {
      updatePendingDuration();
      const interval = setInterval(updatePendingDuration, 1000);
      return () => clearInterval(interval);
    }
  }, [isOpen, alert]);

  const updatePendingDuration = () => {
    if (!alert || !alert.receivedAt) {
      setPendingDuration('N/A');
      return;
    }
    
    const now = new Date();
    const received = new Date(alert.receivedAt);
    const diffMs = now - received;
    
    if (diffMs < 0) {
      setPendingDuration('N/A');
      return;
    }
    
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
    
    setPendingDuration(durationStr.trim() || '0s');
  };

  if (!isOpen || !alert) return null;

  const handleResolve = async () => {
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const result = await resolveAlert(alert.id, username, description.trim() || undefined);
      
      if (result.success) {
        setSuccess('✅ Alert resolved successfully!');
        setTimeout(() => {
          if (onResolved) {
            onResolved(result.alert);
          }
          // Close modal after success
          if (onClose) {
            onClose();
          }
        }, 1500);
      } else {
        setError(result.message || 'Failed to resolve alert');
      }
    } catch (err) {
      setError(err.message || 'An error occurred while resolving');
    } finally {
      setLoading(false);
    }
  };

  // ===== HANDLE CANCEL/CLOSE =====
  const handleClose = () => {
    if (onClose) {
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto shadow-2xl shadow-red-500/10">
        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <CheckCircle className="w-6 h-6 text-emerald-500" />
            <h2 className="text-xl font-bold text-white">Resolve Alert</h2>
          </div>
          <button 
            onClick={handleClose}
            className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
            aria-label="Close modal"
          >
            <X className="w-5 h-5 text-slate-400 hover:text-white" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-4">
          {/* Alert Info */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-sm text-slate-400">Alert ID</span>
              <span className="font-mono text-white">#{alert.id}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-slate-400">System</span>
              <span className="font-mono text-white">{alert.alarmSystem?.systemCode || 'UNKNOWN'}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-slate-400">Zones</span>
              <span className="text-white">{alert.zoneNumbers || '00'}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-slate-400">Received</span>
              <span className="text-white font-mono text-sm">
                <Calendar className="w-3.5 h-3.5 inline mr-1 text-slate-500" />
                {new Date(alert.receivedAt).toLocaleString()}
              </span>
            </div>
            
            <div className="flex justify-between items-center border-t border-slate-800 pt-3 mt-1">
              <span className="text-sm text-slate-400 flex items-center gap-1.5">
                <Timer className="w-4 h-4 text-yellow-500" />
                Pending Duration
              </span>
              <span className="text-yellow-400 font-bold font-mono text-lg">
                {pendingDuration}
              </span>
            </div>
          </div>

          {/* Message Preview */}
          <div className="bg-slate-950/50 border border-slate-800 rounded-xl p-3">
            <p className="text-[10px] text-slate-500 font-mono">📨 Alert Message</p>
            <p className="text-xs text-slate-300 font-mono truncate">
              {alert.alertType || 'No message'}
            </p>
          </div>

          {/* Description Input */}
          <div className="space-y-2">
            <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono flex items-center gap-2">
              <FileText className="w-4 h-4" />
              Resolution Description <span className="text-slate-500 font-normal">(Optional)</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe how this alert was resolved..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/50 transition-all font-mono min-h-[80px] resize-none"
              rows={3}
            />
          </div>

          {/* Resolve Info */}
          <div className="flex items-center gap-2 text-xs text-slate-500 font-mono">
            <User className="w-3.5 h-3.5" />
            <span>Resolving as: <span className="text-white font-bold">{username}</span></span>
          </div>

          {/* Error/Success Messages */}
          {error && (
            <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-3 flex items-start gap-2.5 text-sm text-red-400">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-3 flex items-start gap-2.5 text-sm text-emerald-400">
              <CheckCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{success}</span>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <button
              onClick={handleClose}
              className="flex-1 py-2.5 border border-slate-700 text-slate-400 hover:text-white hover:border-slate-600 rounded-xl text-sm font-mono transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleResolve}
              disabled={loading}
              className="flex-1 py-2.5 bg-gradient-to-r from-emerald-600 to-emerald-700 hover:from-emerald-500 hover:to-emerald-600 text-white font-bold rounded-xl text-sm font-mono transition-all uppercase tracking-wide flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>
                  <CheckCircle className="w-4 h-4" />
                  Resolve Alert
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

AlertResolveModal.propTypes = {
  alert: PropTypes.object,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onResolved: PropTypes.func,
  username: PropTypes.string.isRequired,
};