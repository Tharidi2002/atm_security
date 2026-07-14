import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Shield, RefreshCw, LogOut, Settings, FileText, Power, PowerOff, Loader2 } from 'lucide-react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export default function Navbar({ user, onLogout, onOpenAdminPanel, onRefresh, onOpenReport }) {
  const [loading, setLoading] = useState(false);
  const [commandStatus, setCommandStatus] = useState('');
  const [systems, setSystems] = useState([]);
  const [selectedSystem, setSelectedSystem] = useState('');

  // ===== FETCH SYSTEMS ON MOUNT =====
  useEffect(() => {
    const loadSystems = async () => {
      try {
        if (user.role === 'USER' && user.assignedSystems) {
          const mappedSystems = user.assignedSystems.map(code => ({ systemCode: code }));
          setSystems(mappedSystems);
          if (mappedSystems.length > 0) {
            setSelectedSystem(mappedSystems[0].systemCode);
          }
        } else {
          const res = await fetch(`${API_BASE_URL}/admin/systems`);
          if (res.ok) {
            const data = await res.json();
            setSystems(data);
            if (data.length > 0) {
              setSelectedSystem(data[0].systemCode);
            }
          }
        }
      } catch (e) {
        console.error('Failed to load systems in Navbar', e);
      }
    };
    loadSystems();
  }, [user]);

  // ===== ARM / DISARM COMMAND =====
  const sendCommand = async (command) => {
    if (!selectedSystem) {
      setCommandStatus('❌ Select a system');
      return;
    }
    
    setLoading(true);
    setCommandStatus('');
    
    try {
      const params = new URLSearchParams();
      params.append('atmCode', selectedSystem);
      params.append('command', command);
      
      const response = await fetch(`${API_BASE_URL}/alerts/set-command?${params}`, {
        method: 'POST',
      });
      
      const data = await response.json();
      
      if (response.ok) {
        setCommandStatus(`✅ ${command} successful!`);
        // Refresh alerts after command
        setTimeout(() => onRefresh(), 1000);
      } else {
        setCommandStatus(`❌ ${command} failed: ${data.message || data || 'Unknown error'}`);
      }
    } catch (error) {
      setCommandStatus(`❌ ${command} failed: ${error.message}`);
    } finally {
      setLoading(false);
      setTimeout(() => setCommandStatus(''), 5000);
    }
  };

  return (
    <nav className="border-b border-slate-800 bg-slate-950/50 backdrop-blur px-6 py-4 flex flex-col md:flex-row justify-between items-center gap-4 sticky top-0 z-50">
      {/* Brand */}
      <div className="flex items-center gap-3">
        <div className="bg-red-500/10 p-2 rounded-lg border border-red-500/20">
          <Shield className="w-6 h-6 text-red-500 animate-pulse" />
        </div>
        <div>
          <h1 className="text-lg font-bold tracking-wider uppercase text-white">
            CENTRALIZED ALARM SECURITY
          </h1>
          <p className="text-xs text-slate-400 font-mono flex items-center gap-1">
            <span className="w-2 h-2 bg-emerald-500 rounded-full animate-ping"></span>
            LIVE MONITORING SYSTEM
          </p>
        </div>
      </div>

      {/* Actions */}
      <div className="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
        {/* ARM/DISARM Buttons */}
        <div className="flex items-center gap-2 w-full sm:w-auto">
          {systems.length > 0 && (
            <select
              value={selectedSystem}
              onChange={(e) => setSelectedSystem(e.target.value)}
              className="bg-slate-900 border border-slate-700 hover:border-slate-600 rounded-lg px-3 py-2 text-xs font-mono text-white focus:outline-none focus:border-red-500/50 transition-colors w-full sm:w-auto"
            >
              <option value="">Select ATM...</option>
              {systems.map((sys) => (
                <option key={sys.systemCode || sys.id} value={sys.systemCode}>
                  {sys.systemCode}
                </option>
              ))}
            </select>
          )}
          <button
            onClick={() => sendCommand('ARM')}
            disabled={loading || !selectedSystem}
            className="flex items-center justify-center gap-1.5 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2 rounded-lg text-xs font-mono transition-all disabled:opacity-50 disabled:cursor-not-allowed w-full sm:w-auto"
          >
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Power className="w-4 h-4" />}
            ARM
          </button>
          <button
            onClick={() => sendCommand('DISARM')}
            disabled={loading || !selectedSystem}
            className="flex items-center justify-center gap-1.5 bg-red-600 hover:bg-red-500 text-white px-4 py-2 rounded-lg text-xs font-mono transition-all disabled:opacity-50 disabled:cursor-not-allowed w-full sm:w-auto"
          >
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <PowerOff className="w-4 h-4" />}
            DISARM
          </button>
        </div>

        {/* Status Message */}
        {commandStatus && (
          <span className={`text-xs font-mono ${commandStatus.includes('✅') ? 'text-emerald-400' : 'text-red-400'}`}>
            {commandStatus}
          </span>
        )}

        {/* User Badge */}
        <div className="bg-slate-900 border border-slate-850 px-3.5 py-1.5 rounded-lg flex items-center gap-2 w-full sm:w-auto justify-center sm:justify-start">
          <div className="w-2.5 h-2.5 bg-red-500 rounded-full animate-pulse" />
          <span className="text-xs font-mono text-slate-300">
            {user.username} <span className="text-slate-500">({user.role})</span>
          </span>
        </div>

        <div className="flex gap-2 w-full sm:w-auto">
          <button
            onClick={onOpenReport}
            className="flex items-center justify-center gap-1.5 bg-blue-500/10 hover:bg-blue-500/20 text-blue-400 hover:text-blue-300 border border-blue-500/30 hover:border-blue-500/50 px-4 py-2 rounded-lg text-xs font-mono transition-all w-full sm:w-auto"
          >
            <FileText className="w-4 h-4" /> Reports
          </button>

          {user.role === 'ADMIN' && (
            <button
              onClick={onOpenAdminPanel}
              className="flex items-center justify-center gap-1.5 bg-slate-800 hover:bg-red-650 hover:text-white border border-slate-700 hover:border-red-500 px-4 py-2 rounded-lg text-xs font-mono transition-all w-full sm:w-auto"
            >
              <Settings className="w-4 h-4" /> System Access
            </button>
          )}

          <button 
            onClick={onRefresh}
            className="flex items-center justify-center gap-1.5 bg-slate-800 hover:bg-slate-700 px-4 py-2 rounded-lg text-xs font-mono transition-all border border-slate-700 w-full sm:w-auto"
          >
            <RefreshCw className="w-3.5 h-3.5" /> Refresh
          </button>

          <button
            onClick={onLogout}
            className="flex items-center justify-center gap-1.5 bg-red-500/10 hover:bg-red-600 text-red-400 hover:text-white border border-red-500/20 hover:border-red-500 px-4 py-2 rounded-lg text-xs font-mono transition-all w-full sm:w-auto"
          >
            <LogOut className="w-3.5 h-3.5" /> Logout
          </button>
        </div>
      </div>
    </nav>
  );
}

Navbar.propTypes = {
  user: PropTypes.shape({
    username: PropTypes.string.isRequired,
    role: PropTypes.string.isRequired,
    assignedSystems: PropTypes.array,
  }).isRequired,
  onLogout: PropTypes.func.isRequired,
  onOpenAdminPanel: PropTypes.func.isRequired,
  onRefresh: PropTypes.func.isRequired,
  onOpenReport: PropTypes.func.isRequired,
};