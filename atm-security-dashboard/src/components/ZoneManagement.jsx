import { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import { 
  X, Edit2, RefreshCw, AlertCircle, 
  CheckCircle, ToggleLeft, ToggleRight, Search, Save
} from 'lucide-react';
import { fetchZones, updateZone, resetZones, fetchZoneTypes } from '../services/api';

const ZONE_TYPE_LABELS = {
  0: 'OFF',
  1: 'PERIMETER',
  2: 'DELAY',
  3: 'AWAY',
  4: '24HR',
  5: 'MUTE',
  6: 'EXIT',
  7: 'BELL',
  8: 'SOS'
};

const ZONE_TYPE_COLORS = {
  0: 'text-slate-400 bg-slate-500/10 border-slate-500/20',
  1: 'text-blue-400 bg-blue-500/10 border-blue-500/20',
  2: 'text-yellow-400 bg-yellow-500/10 border-yellow-500/20',
  3: 'text-purple-400 bg-purple-500/10 border-purple-500/20',
  4: 'text-red-400 bg-red-500/10 border-red-500/20',
  5: 'text-slate-400 bg-slate-500/10 border-slate-500/20',
  6: 'text-green-400 bg-green-500/10 border-green-500/20',
  7: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20',
  8: 'text-orange-400 bg-orange-500/10 border-orange-500/20'
};

export default function ZoneManagement({ systemId, systemCode, isOpen, onClose }) {
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [editingZone, setEditingZone] = useState(null);
  const [editingName, setEditingName] = useState('');
//   const [editingDescription, setEditingDescription] = useState('');
  const [zoneTypes, setZoneTypes] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('ALL');
  const [updatingZone, setUpdatingZone] = useState(null);

  const loadZones = useCallback(async () => {
    if (!systemId) return;
    
    setLoading(true);
    setError('');
    try {
      const data = await fetchZones(systemId);
      setZones(data.zones || []);
    } catch {
      setError('Failed to load zones');
    } finally {
      setLoading(false);
    }
  }, [systemId]);

  const loadZoneTypes = async () => {
    try {
      const types = await fetchZoneTypes();
      setZoneTypes(types);
    } catch {
      console.error('Failed to load zone types');
    }
  };

  useEffect(() => {
    if (isOpen && systemId) {
      loadZones();
      loadZoneTypes();
    }
  }, [isOpen, systemId, loadZones]);

  // ===== UPDATE ZONE NAME =====
  const handleStartEditName = (zone) => {
    setEditingZone(zone.id);
    setEditingName(zone.zoneName);
  };

  const handleSaveName = async (zoneId) => {
    if (!editingName.trim()) {
      setError('Zone name cannot be empty');
      setTimeout(() => setError(''), 3000);
      return;
    }

    try {
      await updateZone(zoneId, { zoneName: editingName.trim() });
      await loadZones();
      setSuccess('Zone name updated successfully');
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to update zone name');
      setTimeout(() => setError(''), 3000);
    } finally {
      setEditingZone(null);
      setEditingName('');
    }
  };

  const handleCancelEdit = () => {
    setEditingZone(null);
    setEditingName('');
  };

  // ===== UPDATE ZONE TYPE =====
  const handleZoneTypeChange = async (zoneId, newType) => {
    setUpdatingZone(zoneId);
    try {
      await updateZone(zoneId, { zoneType: parseInt(newType) });
      await loadZones();
      setSuccess('Zone type updated successfully');
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to update zone type');
      setTimeout(() => setError(''), 3000);
    } finally {
      setUpdatingZone(null);
    }
  };

  // ===== UPDATE ZONE DESCRIPTION =====
  const handleDescriptionChange = async (zoneId, newDescription) => {
    try {
      await updateZone(zoneId, { description: newDescription });
      await loadZones();
      setSuccess('Description updated successfully');
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to update description');
      setTimeout(() => setError(''), 3000);
    }
  };

  // ===== TOGGLE ZONE ACTIVE =====
  const handleToggleActive = async (zoneId, currentStatus) => {
    try {
      await updateZone(zoneId, { isActive: !currentStatus });
      await loadZones();
      setSuccess(`Zone ${!currentStatus ? 'activated' : 'deactivated'} successfully`);
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to update zone status');
      setTimeout(() => setError(''), 3000);
    }
  };

  const handleResetZones = async () => {
    if (!window.confirm(`Are you sure you want to reset all zones for ${systemCode}? This will restore default names and types.`)) return;
    
    try {
      await resetZones(systemId);
      await loadZones();
      setSuccess('Zones reset to default successfully');
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to reset zones');
      setTimeout(() => setError(''), 3000);
    }
  };

  const getZoneTypeLabel = (type) => {
    return ZONE_TYPE_LABELS[type] || 'UNKNOWN';
  };

  const getZoneTypeColor = (type) => {
    return ZONE_TYPE_COLORS[type] || 'text-slate-400 bg-slate-500/10 border-slate-500/20';
  };

  const filteredZones = zones.filter(zone => {
    const matchesSearch = zone.zoneName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          zone.zoneNumber.toString().includes(searchQuery);
    const matchesType = filterType === 'ALL' || zone.zoneType === parseInt(filterType);
    return matchesSearch && matchesType;
  });

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[150] flex items-center justify-end p-4 bg-black/80 backdrop-blur-sm">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden shadow-2xl shadow-red-500/10">
        
        {/* Header */}
        <div className="flex justify-between items-center p-5 border-b border-slate-800 bg-slate-950/40">
          <div>
            <h2 className="text-lg font-bold text-white">Zone Management</h2>
            <p className="text-xs text-slate-400 font-mono">{systemCode} - {zones.length} Zones</p>
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
          {/* Error/Success */}
          {error && (
            <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-3 flex items-start gap-2.5 text-sm text-red-400 mb-4">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}
          {success && (
            <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-3 flex items-start gap-2.5 text-sm text-emerald-400 mb-4">
              <CheckCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{success}</span>
            </div>
          )}

          {/* Toolbar */}
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div className="flex items-center gap-2 flex-wrap">
              <div className="relative">
                <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-500" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search zones..."
                  className="bg-slate-800 border border-slate-700 rounded-lg pl-8 pr-3 py-1.5 text-xs font-mono text-white placeholder-slate-500 focus:outline-none focus:border-red-500/50 w-40"
                />
              </div>
              <select
                value={filterType}
                onChange={(e) => setFilterType(e.target.value)}
                className="bg-slate-800 border border-slate-700 rounded-lg px-3 py-1.5 text-xs font-mono text-white focus:outline-none focus:border-red-500/50"
              >
                <option value="ALL">All Types</option>
                {zoneTypes.map((type) => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>
            <button
              onClick={handleResetZones}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-yellow-500/10 hover:bg-yellow-500/20 text-yellow-400 border border-yellow-500/30 rounded-lg text-xs font-mono transition-all"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              Reset to Default
            </button>
          </div>

          {/* Zones Grid */}
          {loading ? (
            <div className="text-center text-slate-400 py-8">Loading zones...</div>
          ) : filteredZones.length === 0 ? (
            <div className="text-center text-slate-500 py-8 font-mono text-sm">No zones found</div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {filteredZones.map((zone) => {
                const isEditing = editingZone === zone.id;
                const isUpdating = updatingZone === zone.id;
                
                return (
                  <div
                    key={zone.id}
                    className={`bg-slate-950 border rounded-xl p-4 transition-all ${
                      zone.isActive 
                        ? 'border-slate-800 hover:border-slate-700' 
                        : 'border-slate-800/50 opacity-50 hover:opacity-75'
                    }`}
                  >
                    {/* Zone Header */}
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-mono font-bold text-slate-400">
                          Z{String(zone.zoneNumber).padStart(2, '0')}
                        </span>
                        <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded-full border ${getZoneTypeColor(zone.zoneType)}`}>
                          {getZoneTypeLabel(zone.zoneType)}
                        </span>
                      </div>
                      <button
                        onClick={() => handleToggleActive(zone.id, zone.isActive)}
                        className="text-slate-500 hover:text-white transition-colors"
                        title={zone.isActive ? 'Deactivate' : 'Activate'}
                      >
                        {zone.isActive ? 
                          <ToggleRight className="w-4 h-4 text-emerald-500" /> : 
                          <ToggleLeft className="w-4 h-4 text-slate-500" />
                        }
                      </button>
                    </div>

                    {/* Zone Name - Editable with Save button */}
                    <div className="flex items-center gap-2">
                      {isEditing ? (
                        <>
                          <input
                            type="text"
                            value={editingName}
                            onChange={(e) => setEditingName(e.target.value)}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter') {
                                handleSaveName(zone.id);
                              }
                              if (e.key === 'Escape') {
                                handleCancelEdit();
                              }
                            }}
                            className="flex-1 bg-slate-800 border border-red-500/50 rounded-lg px-2 py-1 text-sm text-white font-medium focus:outline-none"
                            autoFocus
                          />
                          <button
                            onClick={() => handleSaveName(zone.id)}
                            className="p-1 bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 rounded-lg transition-colors"
                            title="Save name"
                          >
                            <Save className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={handleCancelEdit}
                            className="p-1 bg-slate-800 hover:bg-slate-700 text-slate-400 rounded-lg transition-colors"
                            title="Cancel"
                          >
                            <X className="w-3.5 h-3.5" />
                          </button>
                        </>
                      ) : (
                        <>
                          <span className="text-sm text-white font-medium flex-1 truncate">
                            {zone.zoneName}
                          </span>
                          <button
                            onClick={() => handleStartEditName(zone)}
                            className="text-slate-500 hover:text-white transition-colors"
                            title="Edit name"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                        </>
                      )}
                    </div>

                    {/* Zone Type Select - Auto Save on change */}
                    <div className="mt-2">
                      <select
                        value={zone.zoneType}
                        onChange={(e) => handleZoneTypeChange(zone.id, e.target.value)}
                        disabled={isUpdating}
                        className={`w-full bg-slate-800 border border-slate-700 rounded-lg px-2 py-1 text-xs font-mono text-white focus:outline-none focus:border-red-500/50 ${
                          isUpdating ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                      >
                        {zoneTypes.map((type) => (
                          <option key={type.value} value={type.value}>
                            {type.label} - {type.description}
                          </option>
                        ))}
                      </select>
                      {isUpdating && (
                        <span className="text-[8px] text-slate-500 ml-1">Updating...</span>
                      )}
                    </div>

                    {/* Description - Auto Save on blur */}
                    <div className="mt-1.5">
                      <input
                        type="text"
                        defaultValue={zone.description || ''}
                        placeholder="Add description..."
                        onBlur={(e) => {
                          const newDesc = e.target.value;
                          if (newDesc !== (zone.description || '')) {
                            handleDescriptionChange(zone.id, newDesc);
                          }
                        }}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') {
                            e.target.blur();
                          }
                        }}
                        className="w-full bg-slate-800/50 border border-slate-700/50 rounded-lg px-2 py-0.5 text-[10px] font-mono text-slate-400 placeholder-slate-600 focus:outline-none focus:border-red-500/50"
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

ZoneManagement.propTypes = {
  systemId: PropTypes.number,
  systemCode: PropTypes.string,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
};