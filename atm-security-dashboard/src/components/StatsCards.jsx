// import React from 'react';
import PropTypes from 'prop-types';
import { Radio, AlertTriangle, CheckCircle2 } from 'lucide-react';

export default function StatsCards({ stats }) {
  const cards = [
    {
      title: 'Total Incidents',
      value: stats.total || 0,
      icon: Radio,
      color: 'blue'
    },
    {
      title: 'Active Threats',
      value: stats.pending || 0,
      icon: AlertTriangle,
      color: 'red'
    },
    {
      title: 'Resolved Cases',
      value: stats.resolved || 0,
      icon: CheckCircle2,
      color: 'emerald'
    }
  ];

  return (
    <div className="grid grid-cols-1 xs:grid-cols-3 gap-3 sm:gap-4 md:gap-5">
      {cards.map((card, index) => (
        <div 
          key={index}
          className="bg-slate-950 border border-slate-800 rounded-xl sm:rounded-2xl p-4 sm:p-5 flex items-center justify-between shadow-lg shadow-black/20"
        >
          <div className="min-w-0">
            <p className="text-[10px] sm:text-xs font-medium text-slate-400 uppercase tracking-wider">
              {card.title}
            </p>
            <h3 className={`text-xl sm:text-2xl md:text-3xl font-bold mt-0.5 sm:mt-1 ${
              card.color === 'red' ? 'text-red-500' : 
              card.color === 'emerald' ? 'text-emerald-500' : 
              'text-white'
            }`}>
              {card.value}
            </h3>
          </div>
          <div className={`p-2 sm:p-3 rounded-xl border flex-shrink-0 ${
            card.color === 'red' ? 'bg-red-500/10 border-red-500/20 text-red-500' :
            card.color === 'emerald' ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' :
            'bg-blue-500/10 border-blue-500/20 text-blue-400'
          }`}>
            <card.icon className="w-4 h-4 sm:w-5 sm:h-5" />
          </div>
        </div>
      ))}
    </div>
  );
}

StatsCards.propTypes = {
  stats: PropTypes.shape({
    total: PropTypes.number,
    pending: PropTypes.number,
    resolved: PropTypes.number,
  }).isRequired,
};