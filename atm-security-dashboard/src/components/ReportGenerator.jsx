import { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import { 
  X, FileText, Download, Printer,
  RefreshCw, AlertCircle, CheckCircle,
  FileSpreadsheet, Clock, Zap
} from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api';

export default function ReportGenerator({ isOpen, onClose, user }) {
  const [reportType, setReportType] = useState('summary');
  const [dateRange, setDateRange] = useState('this_month');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [summaryData, setSummaryData] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const setDefaultDates = () => {
    const now = new Date();
    const from = new Date();
    from.setDate(now.getDate() - 30);
    
    setFromDate(from.toISOString().split('T')[0]);
    setToDate(now.toISOString().split('T')[0]);
  };

  const generateReport = useCallback(async () => {
    setLoading(true);
    setError('');
    setSuccess('');
    setSummaryData(null);

    try {
      const params = new URLSearchParams();
      params.append('from', fromDate);
      params.append('to', toDate);
      if (user.role === 'USER') {
        params.append('username', user.username);
      }

      const response = await fetch(`${API_BASE_URL}/reports/summary?${params}`);
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to generate report');
      }
      
      const data = await response.json();
      setSummaryData(data);
      setSuccess('✅ Report generated successfully!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message || 'Failed to generate report');
    } finally {
      setLoading(false);
    }
  }, [fromDate, toDate, user.role, user.username]);

  useEffect(() => {
    if (isOpen) {
      setDefaultDates();
      setTimeout(() => {
        generateReport();
      }, 500);
    }
  }, [isOpen, generateReport]);

  const handleDateRangeChange = (range) => {
    setDateRange(range);
    const now = new Date();
    const from = new Date();

    switch(range) {
      case 'today':
        setFromDate(now.toISOString().split('T')[0]);
        setToDate(now.toISOString().split('T')[0]);
        break;
      case 'this_week':
        from.setDate(now.getDate() - 7);
        setFromDate(from.toISOString().split('T')[0]);
        setToDate(now.toISOString().split('T')[0]);
        break;
      case 'this_month':
        from.setDate(now.getDate() - 30);
        setFromDate(from.toISOString().split('T')[0]);
        setToDate(now.toISOString().split('T')[0]);
        break;
      case 'custom':
        break;
      default:
        break;
    }
  };

  const downloadReport = async (type) => {
    setDownloading(true);
    setError('');
    setSuccess('');

    try {
      const params = new URLSearchParams();
      params.append('from', fromDate);
      params.append('to', toDate);
      if (user.role === 'USER') {
        params.append('username', user.username);
      }

      let endpoint = '';
      let filename = '';

      if (type === 'pdf') {
        endpoint = `${API_BASE_URL}/reports/export/pdf`;
        filename = `Alarm_Professional_Report_${new Date().toISOString().split('T')[0]}.pdf`;
      } else if (type === 'excel') {
        endpoint = `${API_BASE_URL}/reports/export/excel`;
        filename = `Alarm_Professional_Report_${new Date().toISOString().split('T')[0]}.xlsx`;
      }

      const response = await fetch(`${endpoint}?${params}`);
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to download report');
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      setSuccess(`✅ ${type.toUpperCase()} downloaded successfully!`);
      setTimeout(() => setSuccess(''), 3000);

    } catch (err) {
      setError(err.message || 'Failed to download report');
    } finally {
      setDownloading(false);
    }
  };

  const printReport = () => {
    window.print();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[160] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden shadow-2xl shadow-blue-500/10">
        
        {/* Header */}
        <div className="flex justify-between items-center p-5 border-b border-slate-800 bg-slate-950/40">
          <div className="flex items-center gap-3">
            <FileText className="w-6 h-6 text-blue-400" />
            <h2 className="text-xl font-bold text-white">📊 Report Generator</h2>
          </div>
          <button 
            onClick={onClose}
            className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
          >
            <X className="w-5 h-5 text-slate-400 hover:text-white" />
          </button>
        </div>

        <div className="p-5 overflow-y-auto max-h-[calc(90vh-80px)]">
          {/* Success/Error Messages */}
          {success && (
            <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-3 flex items-start gap-2.5 text-sm text-emerald-400 mb-4">
              <CheckCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{success}</span>
            </div>
          )}
          {error && (
            <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-3 flex items-start gap-2.5 text-sm text-red-400 mb-4">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Controls */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div>
              <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">Report Type</label>
              <select
                value={reportType}
                onChange={(e) => setReportType(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-blue-500/50"
              >
                <option value="summary">📊 Summary Report</option>
                <option value="detailed">📋 Detailed Report</option>
                <option value="health">💚 System Health</option>
                <option value="performance">👤 User Performance</option>
              </select>
            </div>

            <div>
              <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">Date Range</label>
              <select
                value={dateRange}
                onChange={(e) => handleDateRangeChange(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-blue-500/50"
              >
                <option value="today">📅 Today</option>
                <option value="this_week">📅 This Week</option>
                <option value="this_month">📅 This Month</option>
                <option value="custom">📅 Custom Range</option>
              </select>
            </div>

            <div>
              <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">From</label>
              <input
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-blue-500/50"
                disabled={dateRange !== 'custom'}
              />
            </div>

            <div>
              <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">To</label>
              <input
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-blue-500/50"
                disabled={dateRange !== 'custom'}
              />
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-wrap gap-3 mb-6">
            <button
              onClick={generateReport}
              disabled={loading}
              className="flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-500 text-white font-bold rounded-xl text-sm transition-all disabled:opacity-50"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <RefreshCw className="w-4 h-4" />
              )}
              Generate Report
            </button>

            <button
              onClick={() => downloadReport('pdf')}
              disabled={downloading || !summaryData}
              className="flex items-center gap-2 px-5 py-2.5 bg-red-600 hover:bg-red-500 text-white font-bold rounded-xl text-sm transition-all disabled:opacity-50"
            >
              {downloading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <Download className="w-4 h-4" />
              )}
              Download PDF
            </button>

            <button
              onClick={() => downloadReport('excel')}
              disabled={downloading || !summaryData}
              className="flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-xl text-sm transition-all disabled:opacity-50"
            >
              {downloading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <FileSpreadsheet className="w-4 h-4" />
              )}
              Download Excel
            </button>

            <button
              onClick={printReport}
              disabled={!summaryData}
              className="flex items-center gap-2 px-5 py-2.5 bg-slate-700 hover:bg-slate-600 text-white font-bold rounded-xl text-sm transition-all disabled:opacity-50"
            >
              <Printer className="w-4 h-4" />
              Print
            </button>

            <button
              onClick={onClose}
              className="flex items-center gap-2 px-5 py-2.5 border border-slate-700 text-slate-400 hover:text-white rounded-xl text-sm transition-all"
            >
              <X className="w-4 h-4" />
              Close
            </button>
          </div>

          {/* Summary Display */}
          {summaryData && (
            <div className="space-y-4 print:block">
              {/* Stats Cards */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                <StatCard 
                  label="Total Alerts" 
                  value={summaryData.totalAlerts || 0} 
                  color="blue"
                  icon={<AlertCircle className="w-5 h-5" />}
                />
                <StatCard 
                  label="Pending" 
                  value={summaryData.pending || 0} 
                  color="red"
                  icon={<Clock className="w-5 h-5" />}
                />
                <StatCard 
                  label="Resolved" 
                  value={summaryData.resolved || 0} 
                  color="green"
                  icon={<CheckCircle className="w-5 h-5" />}
                />
                <StatCard 
                  label="Avg Resolution" 
                  value={formatDuration(summaryData.avgResolutionSeconds)} 
                  color="yellow"
                  icon={<Zap className="w-5 h-5" />}
                />
              </div>

              {/* By System */}
              {summaryData.bySystem && Object.keys(summaryData.bySystem).length > 0 && (
                <div className="bg-slate-950/50 border border-slate-800 rounded-xl p-4">
                  <h3 className="text-sm font-bold text-white mb-3">📊 Alerts by System</h3>
                  <div className="space-y-2">
                    {Object.entries(summaryData.bySystem).map(([system, count]) => (
                      <div key={system} className="flex items-center gap-2">
                        <span className="text-sm text-slate-300 font-mono w-32 truncate">{system}</span>
                        <div className="flex-1 bg-slate-800 rounded-full h-2 overflow-hidden">
                          <div 
                            className="h-full bg-blue-500 rounded-full transition-all"
                            style={{ 
                              width: `${(count / summaryData.totalAlerts) * 100}%`,
                              minWidth: '4px'
                            }}
                          />
                        </div>
                        <span className="text-sm text-slate-400 font-mono w-12 text-right">{count}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* By Zone */}
              {summaryData.byZone && Object.keys(summaryData.byZone).length > 0 && (
                <div className="bg-slate-950/50 border border-slate-800 rounded-xl p-4">
                  <h3 className="text-sm font-bold text-white mb-3">📍 Alerts by Zone</h3>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                    {Object.entries(summaryData.byZone)
                      .sort((a, b) => b[1] - a[1])
                      .slice(0, 8)
                      .map(([zone, count]) => (
                        <div key={zone} className="flex justify-between items-center bg-slate-800/50 rounded-lg px-3 py-2">
                          <span className="text-xs text-slate-300">{zone}</span>
                          <span className="text-xs font-bold text-amber-400">{count}</span>
                        </div>
                      ))}
                  </div>
                </div>
              )}

              {/* Resolved By */}
              {summaryData.resolvedBy && Object.keys(summaryData.resolvedBy).length > 0 && (
                <div className="bg-slate-950/50 border border-slate-800 rounded-xl p-4">
                  <h3 className="text-sm font-bold text-white mb-3">👤 Resolved By</h3>
                  <div className="flex flex-wrap gap-2">
                    {Object.entries(summaryData.resolvedBy).map(([user, count]) => (
                      <div key={user} className="flex items-center gap-2 bg-slate-800/50 rounded-lg px-3 py-2">
                        <span className="text-xs text-slate-300">{user}</span>
                        <span className="text-xs font-bold text-emerald-400">{count}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Daily Trend */}
              {summaryData.dailyTrend && Object.keys(summaryData.dailyTrend).length > 0 && (
                <div className="bg-slate-950/50 border border-slate-800 rounded-xl p-4">
                  <h3 className="text-sm font-bold text-white mb-3">📈 Daily Trend</h3>
                  <div className="flex items-end gap-1 h-32">
                    {Object.entries(summaryData.dailyTrend)
                      .sort((a, b) => a[0].localeCompare(b[0]))
                      .slice(-14)
                      .map(([date, count]) => {
                        const max = Math.max(...Object.values(summaryData.dailyTrend));
                        const height = max > 0 ? (count / max) * 100 : 0;
                        return (
                          <div key={date} className="flex-1 flex flex-col items-center gap-1">
                            <div 
                              className="w-full bg-blue-500/50 rounded-t transition-all"
                              style={{ height: `${Math.max(height, 4)}%` }}
                            />
                            <span className="text-[8px] text-slate-500 font-mono">
                              {date.substring(5)}
                            </span>
                            <span className="text-[8px] text-slate-400 font-mono">
                              {count}
                            </span>
                          </div>
                        );
                      })}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ===== STAT CARD COMPONENT =====
function StatCard({ label, value, color, icon }) {
  const colors = {
    blue: 'bg-blue-500/10 border-blue-500/20 text-blue-400',
    red: 'bg-red-500/10 border-red-500/20 text-red-400',
    green: 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400',
    yellow: 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400'
  };

  return (
    <div className={`p-3 rounded-xl border ${colors[color]}`}>
      <div className="flex items-center gap-2">
        {icon}
        <div>
          <p className="text-xs text-slate-400">{label}</p>
          <p className="text-xl font-bold">{value}</p>
        </div>
      </div>
    </div>
  );
}

StatCard.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  color: PropTypes.string.isRequired,
  icon: PropTypes.node.isRequired,
};

function formatDuration(seconds) {
  if (!seconds || seconds === 0) return 'N/A';
  
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  
  if (mins > 60) {
    const hours = Math.floor(mins / 60);
    const remainingMins = mins % 60;
    return `${hours}h ${remainingMins}m`;
  }
  
  return `${mins}m ${secs}s`;
}

ReportGenerator.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  user: PropTypes.shape({
    username: PropTypes.string.isRequired,
    role: PropTypes.string.isRequired,
  }).isRequired,
};