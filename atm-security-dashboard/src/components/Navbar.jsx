import React from 'react';
import { Shield, RefreshCw } from 'lucide-react';

export default function Navbar({ onRefresh }) {
  return (
    <nav className="border-b border-slate-800 bg-slate-950/50 backdrop-blur px-6 py-4 flex flex-col sm:flex-row justify-between items-center gap-3 sticky top-0 z-50">
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
      <button 
        onClick={onRefresh}
        className="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 px-4 py-2 rounded-lg text-sm transition-all border border-slate-700 w-full sm:w-auto justify-center"
      >
        <RefreshCw className="w-4 h-4" /> Refresh Now
      </button>
    </nav>
  );
}