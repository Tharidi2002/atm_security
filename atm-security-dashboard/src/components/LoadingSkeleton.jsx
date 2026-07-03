import React from 'react';

export default function LoadingSkeleton() {
  return (
    <div className="bg-slate-950 border border-slate-800 rounded-xl overflow-hidden">
      <div className="hidden lg:block overflow-x-auto">
        <table className="w-full text-left">
          <thead className="bg-slate-900/50 text-slate-400 uppercase text-xs tracking-wider border-b border-slate-800 font-mono">
            <tr>
              <th className="py-4 px-6">Status</th>
              <th className="py-4 px-6">Alarm System</th>
              <th className="py-4 px-6">Location</th>
              <th className="py-4 px-6">Zone</th>
              <th className="py-4 px-6">Message</th>
              <th className="py-4 px-6">Time</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/50">
            {[...Array(5)].map((_, i) => (
              <tr key={i}>
                <td className="py-4 px-6">
                  <div className="h-6 w-20 bg-slate-800 rounded-full animate-pulse"></div>
                </td>
                <td className="py-4 px-6">
                  <div className="h-4 w-24 bg-slate-800 rounded animate-pulse"></div>
                </td>
                <td className="py-4 px-6">
                  <div className="h-4 w-32 bg-slate-800 rounded animate-pulse"></div>
                </td>
                <td className="py-4 px-6">
                  <div className="h-4 w-16 bg-slate-800 rounded animate-pulse"></div>
                </td>
                <td className="py-4 px-6">
                  <div className="h-4 w-48 bg-slate-800 rounded animate-pulse"></div>
                </td>
                <td className="py-4 px-6">
                  <div className="h-4 w-32 bg-slate-800 rounded animate-pulse"></div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile Skeleton */}
      <div className="lg:hidden divide-y divide-slate-800">
        {[...Array(3)].map((_, i) => (
          <div key={i} className="p-4 space-y-2">
            <div className="flex justify-between">
              <div className="h-6 w-24 bg-slate-800 rounded-full animate-pulse"></div>
              <div className="h-6 w-16 bg-slate-800 rounded animate-pulse"></div>
            </div>
            <div className="h-4 w-40 bg-slate-800 rounded animate-pulse"></div>
            <div className="h-4 w-32 bg-slate-800 rounded animate-pulse"></div>
          </div>
        ))}
      </div>
    </div>
  );
}