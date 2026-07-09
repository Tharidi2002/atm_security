import { useState } from 'react';
import PropTypes from 'prop-types';
import { 
  X, BookOpen, Shield, CheckCircle, 
  Users, Cpu, Layers, Key, Lock, Phone, Bell,
  ChevronRight, ChevronDown, Home, Settings
} from 'lucide-react';

export default function UserManual({ isOpen, onClose }) {
  const [expandedSection, setExpandedSection] = useState('overview');

  const sections = [
    {
      id: 'overview',
      icon: <Home className="w-4 h-4" />,
      title: 'System Overview',
      content: (
        <div className="space-y-2">
          <p className="text-sm text-slate-300">
            The <span className="text-emerald-400 font-bold">Alarm Security System</span> is a centralized monitoring platform 
            for Z8B GSM alarm systems. It provides real-time alert monitoring, 
            system management, and user access control.
          </p>
          <div className="bg-slate-950 border border-slate-800 rounded-lg p-3">
            <p className="text-xs text-slate-400 font-mono">🔐 Default Login Credentials:</p>
            <div className="grid grid-cols-2 gap-2 mt-1 text-xs font-mono">
              <div><span className="text-slate-500">Admin:</span> <span className="text-white">admin</span></div>
              <div><span className="text-slate-500">Password:</span> <span className="text-emerald-400">admin123</span></div>
              <div><span className="text-slate-500">User:</span> <span className="text-white">user1</span></div>
              <div><span className="text-slate-500">Password:</span> <span className="text-emerald-400">user123</span></div>
            </div>
          </div>
        </div>
      )
    },
    {
      id: 'dashboard',
      icon: <Shield className="w-4 h-4" />,
      title: 'Dashboard & Alerts',
      content: (
        <div className="space-y-2">
          <p className="text-sm text-slate-300">
            The dashboard displays all alerts in real-time with auto-refresh every 5 seconds.
          </p>
          <div className="grid grid-cols-2 gap-2">
            <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-2">
              <div className="flex items-center gap-1 text-red-400 text-xs">
                <span className="w-2 h-2 bg-red-500 rounded-full animate-ping"></span>
                PENDING
              </div>
              <p className="text-[10px] text-slate-400">Zone alarms waiting to be resolved</p>
            </div>
            <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-2">
              <div className="flex items-center gap-1 text-blue-400 text-xs">
                <Phone className="w-3 h-3" />
                CALL
              </div>
              <p className="text-[10px] text-slate-400">Incoming call notifications</p>
            </div>
            <div className="bg-yellow-500/10 border border-yellow-500/20 rounded-lg p-2">
              <div className="flex items-center gap-1 text-yellow-400 text-xs">
                <Bell className="w-3 h-3" />
                ARMED
              </div>
              <p className="text-[10px] text-slate-400">System armed status</p>
            </div>
            <div className="bg-emerald-500/10 border border-emerald-500/20 rounded-lg p-2">
              <div className="flex items-center gap-1 text-emerald-400 text-xs">
                <CheckCircle className="w-3 h-3" />
                RESOLVED
              </div>
              <p className="text-[10px] text-slate-400">Alerts that have been resolved</p>
            </div>
          </div>
        </div>
      )
    },
    {
      id: 'zones',
      icon: <Layers className="w-4 h-4" />,
      title: 'Zone Management',
      content: (
        <div className="space-y-2">
          <p className="text-sm text-slate-300">
            Each alarm system has <span className="text-white font-bold">24 zones</span> (16 wireless + 8 wired).
          </p>
          <div className="bg-slate-950 border border-slate-800 rounded-lg p-3">
            <p className="text-xs text-slate-400 font-mono">📌 Zone Types:</p>
            <div className="grid grid-cols-2 gap-1 mt-1 text-[10px] font-mono">
              <div><span className="text-slate-500">OFF</span> <span className="text-slate-400">- Disabled</span></div>
              <div><span className="text-blue-400">PERIMETER</span> <span className="text-slate-400">- Perimeter zone</span></div>
              <div><span className="text-yellow-400">DELAY</span> <span className="text-slate-400">- Delay zone</span></div>
              <div><span className="text-purple-400">AWAY</span> <span className="text-slate-400">- Away/Part alarm</span></div>
              <div><span className="text-red-400">24HR</span> <span className="text-slate-400">- 24 hours zone</span></div>
              <div><span className="text-green-400">EXIT</span> <span className="text-slate-400">- Exit button zone</span></div>
              <div><span className="text-cyan-400">BELL</span> <span className="text-slate-400">- Door bell zone</span></div>
              <div><span className="text-orange-400">SOS</span> <span className="text-slate-400">- SOS zone</span></div>
            </div>
          </div>
          <p className="text-xs text-slate-400">
            ✏️ Admin can rename zones and change zone types.
          </p>
        </div>
      )
    },
    {
      id: 'armed',
      icon: <Lock className="w-4 h-4" />,
      title: 'ARMED / DISARMED System',
      content: (
        <div className="space-y-2">
          <div className="bg-yellow-500/5 border border-yellow-500/20 rounded-lg p-3">
            <h4 className="text-sm font-bold text-yellow-400 flex items-center gap-2">
              <Lock className="w-4 h-4" />
              Z8B Default Passwords
            </h4>
            <div className="space-y-1 mt-2 text-xs font-mono">
              <div className="flex justify-between items-center border-b border-slate-800 pb-1">
                <span className="text-slate-400">Master Password (APP)</span>
                <span className="text-yellow-400 font-bold">8888</span>
              </div>
              <div className="flex justify-between items-center border-b border-slate-800 pb-1">
                <span className="text-slate-400">Admin Password (Keypad)</span>
                <span className="text-yellow-400 font-bold">123456</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400">APP Enter Password</span>
                <span className="text-yellow-400 font-bold">1234</span>
              </div>
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-2">
            <div className="bg-emerald-500/10 border border-emerald-500/20 rounded-lg p-2 text-center">
              <p className="text-emerald-400 font-bold text-sm">🔓 ARM</p>
              <p className="text-[10px] text-slate-400 font-mono">8888 + #</p>
              <p className="text-[8px] text-slate-500">System armed</p>
            </div>
            <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-2 text-center">
              <p className="text-red-400 font-bold text-sm">🔒 DISARM</p>
              <p className="text-[10px] text-slate-400 font-mono">8888 + *</p>
              <p className="text-[8px] text-slate-500">System disarmed</p>
            </div>
          </div>

          <div className="bg-slate-950 border border-slate-800 rounded-lg p-2">
            <p className="text-[10px] text-slate-400 font-mono">
              📱 Also controllable via: APP, Remote Control, SMS, Phone Call
            </p>
          </div>
        </div>
      )
    },
    {
      id: 'admin',
      icon: <Settings className="w-4 h-4" />,
      title: 'Admin Features',
      content: (
        <div className="space-y-2">
          <p className="text-sm text-slate-300">
            Admin users have full control over the system.
          </p>
          <div className="space-y-1">
            <div className="flex items-start gap-2 text-xs text-slate-300">
              <Users className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" />
              <div>
                <span className="text-white font-medium">User Management</span>
                <p className="text-slate-400 text-[10px]">Create users, reset passwords, assign systems</p>
              </div>
            </div>
            <div className="flex items-start gap-2 text-xs text-slate-300">
              <Cpu className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" />
              <div>
                <span className="text-white font-medium">System Management</span>
                <p className="text-slate-400 text-[10px]">Register systems, auto-generate codes, delete systems</p>
              </div>
            </div>
            <div className="flex items-start gap-2 text-xs text-slate-300">
              <Layers className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" />
              <div>
                <span className="text-white font-medium">Zone Management</span>
                <p className="text-slate-400 text-[10px]">Rename zones, change types, activate/deactivate</p>
              </div>
            </div>
          </div>
        </div>
      )
    },
    {
      id: 'shortcuts',
      icon: <Key className="w-4 h-4" />,
      title: 'Quick Shortcuts',
      content: (
        <div className="space-y-1">
          <div className="flex justify-between items-center border-b border-slate-800 pb-1 text-xs font-mono">
            <span className="text-slate-400">Login</span>
            <span className="text-white">admin / admin123</span>
          </div>
          <div className="flex justify-between items-center border-b border-slate-800 pb-1 text-xs font-mono">
            <span className="text-slate-400">User Login</span>
            <span className="text-white">user1 / user123</span>
          </div>
          <div className="flex justify-between items-center border-b border-slate-800 pb-1 text-xs font-mono">
            <span className="text-slate-400">ARM Code</span>
            <span className="text-yellow-400">8888 + #</span>
          </div>
          <div className="flex justify-between items-center border-b border-slate-800 pb-1 text-xs font-mono">
            <span className="text-slate-400">DISARM Code</span>
            <span className="text-yellow-400">8888 + *</span>
          </div>
          <div className="flex justify-between items-center text-xs font-mono">
            <span className="text-slate-400">Admin Panel</span>
            <span className="text-red-400">System Access (Top Right)</span>
          </div>
        </div>
      )
    }
  ];

  const toggleSection = (id) => {
    setExpandedSection(expandedSection === id ? null : id);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-hidden shadow-2xl shadow-blue-500/10">
        
        {/* Header */}
        <div className="flex justify-between items-center p-5 border-b border-slate-800 bg-slate-950/40">
          <div className="flex items-center gap-3">
            <div className="bg-blue-500/10 p-2 rounded-lg border border-blue-500/20">
              <BookOpen className="w-5 h-5 text-blue-400" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">User Manual</h2>
              <p className="text-xs text-slate-400 font-mono">Alarm Security System Guide</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
          >
            <X className="w-5 h-5 text-slate-400 hover:text-white" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 overflow-y-auto max-h-[calc(90vh-120px)]">
          <div className="space-y-2">
            {sections.map((section) => (
              <div 
                key={section.id}
                className="border border-slate-800 rounded-xl overflow-hidden"
              >
                <button
                  onClick={() => toggleSection(section.id)}
                  className="w-full flex items-center justify-between p-3 hover:bg-slate-800/50 transition-colors text-left"
                >
                  <div className="flex items-center gap-3">
                    <span className="text-blue-400">{section.icon}</span>
                    <span className="text-sm font-medium text-white">{section.title}</span>
                  </div>
                  {expandedSection === section.id ? (
                    <ChevronDown className="w-4 h-4 text-slate-400" />
                  ) : (
                    <ChevronRight className="w-4 h-4 text-slate-400" />
                  )}
                </button>
                {expandedSection === section.id && (
                  <div className="p-3 border-t border-slate-800 bg-slate-950/20">
                    {section.content}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Footer */}
          <div className="mt-4 p-3 bg-slate-950 border border-slate-800 rounded-xl text-center">
            <p className="text-[10px] text-slate-500 font-mono">
              🔐 For security, change default passwords after first login
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

UserManual.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
};