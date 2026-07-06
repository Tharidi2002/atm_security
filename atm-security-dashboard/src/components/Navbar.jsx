import React from 'react';
import { Shield, RefreshCw, LogOut, Settings } from 'lucide-react';

export default function Navbar({ user, onLogout, onOpenAdminPanel, onRefresh }) {
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

      {/* Actions & Profile */}
      <div className="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
        {/* User Badge */}
        <div className="bg-slate-900 border border-slate-850 px-3.5 py-1.5 rounded-lg flex items-center gap-2 w-full sm:w-auto justify-center sm:justify-start">
          <div className="w-2.5 h-2.5 bg-red-500 rounded-full animate-pulse" />
          <span className="text-xs font-mono text-slate-300">
            {user.username} <span className="text-slate-500">({user.role})</span>
          </span>
        </div>

        <div className="flex gap-2 w-full sm:w-auto">
          {/* Admin Panel Button */}
          {user.role === 'ADMIN' && (
            <button
              onClick={onOpenAdminPanel}
              className="flex items-center justify-center gap-1.5 bg-slate-800 hover:bg-red-650 hover:text-white border border-slate-700 hover:border-red-500 px-4 py-2 rounded-lg text-xs font-mono transition-all w-full sm:w-auto"
            >
              <Settings className="w-4 h-4" /> System Access
            </button>
          )}

          {/* Refresh Button */}
          <button 
            onClick={onRefresh}
            className="flex items-center justify-center gap-1.5 bg-slate-800 hover:bg-slate-700 px-4 py-2 rounded-lg text-xs font-mono transition-all border border-slate-700 w-full sm:w-auto"
          >
            <RefreshCw className="w-3.5 h-3.5" /> Refresh
          </button>

          {/* Logout Button */}
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