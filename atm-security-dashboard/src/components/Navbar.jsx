import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Shield, RefreshCw, LogOut, Settings, FileText, Power, PowerOff, Loader2, Menu, X } from 'lucide-react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export default function Navbar({ user, onLogout, onOpenAdminPanel, onRefresh, onOpenReport }) {
  const [loading, setLoading] = useState(false);
  const [commandStatus, setCommandStatus] = useState('');
  const [systems, setSystems] = useState([]);
  const [selectedSystem, setSelectedSystem] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

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
    <nav className="border-b border-slate-800 bg-slate-950/50 backdrop-blur px-3 sm:px-4 md:px-6 py-3 md:py-4 sticky top-0 z-50">
      <div className="flex flex-col lg:flex-row justify-between items-center gap-3">
        
        {/* Brand - Left */}
        <div className="flex items-center justify-between w-full lg:w-auto">
          <div className="flex items-center gap-2 sm:gap-3">
            <div className="bg-red-500/10 p-1.5 sm:p-2 rounded-lg border border-red-500/20">
              <Shield className="w-5 h-5 sm:w-6 sm:h-6 text-red-500 animate-pulse" />
            </div>
            <div>
              <h1 className="text-sm sm:text-base md:text-lg font-bold tracking-wider uppercase text-white">
                CENTRALIZED ALARM
              </h1>
              <p className="text-[10px] sm:text-xs text-slate-400 font-mono flex items-center gap-1">
                <span className="w-1.5 h-1.5 sm:w-2 sm:h-2 bg-emerald-500 rounded-full animate-ping"></span>
                LIVE MONITORING
              </p>
            </div>
          </div>
          
          {/* Mobile Menu Toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="lg:hidden p-1.5 rounded-lg hover:bg-slate-800 transition-colors"
          >
            {mobileMenuOpen ? <X className="w-5 h-5 text-slate-400" /> : <Menu className="w-5 h-5 text-slate-400" />}
          </button>
        </div>

        {/* Right Section - Desktop */}
        <div className={`${mobileMenuOpen ? 'flex' : 'hidden'} lg:flex flex-col lg:flex-row items-center gap-2 sm:gap-3 w-full lg:w-auto`}>
          
          {/* ARM/DISARM Controls */}
          <div className="flex flex-wrap items-center justify-center gap-1.5 sm:gap-2 w-full lg:w-auto">
            {systems.length > 0 && (
              <select
                value={selectedSystem}
                onChange={(e) => setSelectedSystem(e.target.value)}
                className="bg-slate-900 border border-slate-700 hover:border-slate-600 rounded-lg px-2 py-1.5 sm:px-3 sm:py-2 text-[10px] sm:text-xs font-mono text-white focus:outline-none focus:border-red-500/50 transition-colors w-full sm:w-auto max-w-[140px] sm:max-w-[160px]"
              >
                <option value="">Select...</option>
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
              className="flex items-center justify-center gap-1 px-2 py-1.5 sm:px-3 sm:py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-[10px] sm:text-xs font-mono transition-all disabled:opacity-50 disabled:cursor-not-allowed flex-1 sm:flex-none"
            >
              {loading ? <Loader2 className="w-3 h-3 sm:w-3.5 sm:h-3.5 animate-spin" /> : <Power className="w-3 h-3 sm:w-3.5 sm:h-3.5" />}
              <span className="hidden xs:inline">ARM</span>
            </button>
            <button
              onClick={() => sendCommand('DISARM')}
              disabled={loading || !selectedSystem}
              className="flex items-center justify-center gap-1 px-2 py-1.5 sm:px-3 sm:py-2 bg-red-600 hover:bg-red-500 text-white rounded-lg text-[10px] sm:text-xs font-mono transition-all disabled:opacity-50 disabled:cursor-not-allowed flex-1 sm:flex-none"
            >
              {loading ? <Loader2 className="w-3 h-3 sm:w-3.5 sm:h-3.5 animate-spin" /> : <PowerOff className="w-3 h-3 sm:w-3.5 sm:h-3.5" />}
              <span className="hidden xs:inline">DISARM</span>
            </button>
          </div>

          {/* Status Message */}
          {commandStatus && (
            <span className={`text-[10px] sm:text-xs font-mono ${commandStatus.includes('✅') ? 'text-emerald-400' : 'text-red-400'} text-center`}>
              {commandStatus}
            </span>
          )}

          {/* User Badge */}
          <div className="bg-slate-900 border border-slate-700 px-2.5 py-1 sm:px-3.5 sm:py-1.5 rounded-lg flex items-center gap-1.5 sm:gap-2 w-full sm:w-auto justify-center">
            <div className="w-2 h-2 sm:w-2.5 sm:h-2.5 bg-red-500 rounded-full animate-pulse" />
            <span className="text-[10px] sm:text-xs font-mono text-slate-300 truncate max-w-[100px] sm:max-w-none">
              {user.username} <span className="text-slate-500 hidden xs:inline">({user.role})</span>
            </span>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-wrap items-center justify-center gap-1.5 sm:gap-2 w-full lg:w-auto">
            <button
              onClick={onOpenReport}
              className="flex items-center justify-center gap-1 px-2 py-1.5 sm:px-3 sm:py-2 bg-blue-500/10 hover:bg-blue-500/20 text-blue-400 hover:text-blue-300 border border-blue-500/30 hover:border-blue-500/50 rounded-lg text-[10px] sm:text-xs font-mono transition-all flex-1 sm:flex-none"
            >
              <FileText className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
              <span className="hidden xs:inline">Reports</span>
            </button>

            {user.role === 'ADMIN' && (
              <button
                onClick={onOpenAdminPanel}
                className="flex items-center justify-center gap-1 px-2 py-1.5 sm:px-3 sm:py-2 bg-slate-800 hover:bg-red-650 hover:text-white border border-slate-700 hover:border-red-500 rounded-lg text-[10px] sm:text-xs font-mono transition-all flex-1 sm:flex-none"
              >
                <Settings className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
                <span className="hidden xs:inline">Access</span>
              </button>
            )}

            <button 
              onClick={onRefresh}
              className="flex items-center justify-center gap-1 px-2 py-1.5 sm:px-3 sm:py-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-[10px] sm:text-xs font-mono transition-all border border-slate-700 flex-1 sm:flex-none"
            >
              <RefreshCw className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
              <span className="hidden xs:inline">Refresh</span>
            </button>

            <button
              onClick={onLogout}
              className="flex items-center justify-center gap-1 px-2 py-1.5 sm:px-3 sm:py-2 bg-red-500/10 hover:bg-red-600 text-red-400 hover:text-white border border-red-500/20 hover:border-red-500 rounded-lg text-[10px] sm:text-xs font-mono transition-all flex-1 sm:flex-none"
            >
              <LogOut className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
              <span className="hidden xs:inline">Logout</span>
            </button>
          </div>
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