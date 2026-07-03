import React from 'react';
import { Radio, AlertTriangle, CheckCircle2 } from 'lucide-react';

export default function StatsCards({ stats }) {
  const cards = [
    {
      title: 'Total Incidents',
      value: stats.total,
      icon: Radio,
      color: 'blue'
    },
    {
      title: 'Active Threats',
      value: stats.pending,
      icon: AlertTriangle,
      color: 'red'
    },
    {
      title: 'Resolved Cases',
      value: stats.resolved,
      icon: CheckCircle2,
      color: 'emerald'
    }
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      {cards.map((card, index) => (
        <div 
          key={index}
          className="bg-slate-950 border border-slate-800 rounded-xl p-5 flex items-center justify-between shadow-lg shadow-black/20"
        >
          <div>
            <p className="text-sm font-medium text-slate-400 uppercase tracking-wider">
              {card.title}
            </p>
            <h3 className={`text-3xl font-bold mt-1 ${
              card.color === 'red' ? 'text-red-500' : 
              card.color === 'emerald' ? 'text-emerald-500' : 
              'text-white'
            }`}>
              {card.value}
            </h3>
          </div>
          <div className={`p-3 rounded-xl border ${
            card.color === 'red' ? 'bg-red-500/10 border-red-500/20 text-red-500' :
            card.color === 'emerald' ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' :
            'bg-blue-500/10 border-blue-500/20 text-blue-400'
          }`}>
            <card.icon />
          </div>
        </div>
      ))}
    </div>
  );
}