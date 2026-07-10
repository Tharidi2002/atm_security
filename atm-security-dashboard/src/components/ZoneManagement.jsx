import { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import { 
  X, Edit2, RefreshCw, AlertCircle, 
  CheckCircle, Search, Save,
  AlertTriangle, Wifi, Plug, Power, PowerOff
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
  const [zoneTypes, setZoneTypes] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('ALL');

  // ===== EDIT MODAL STATES =====
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingZone, setEditingZone] = useState(null);
  const [editForm, setEditForm] = useState({
    zoneName: '',
    zoneType: 1,
    isActive: true,
    description: ''
  });

  // ===== CONFIRMATION MODAL STATES =====
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [confirmChanges, setConfirmChanges] = useState([]);
  const [confirmLoading, setConfirmLoading] = useState(false);

  // ===== RESET CONFIRMATION STATES =====
  const [showResetConfirm, setShowResetConfirm] = useState(false);
  const [resetConfirmText, setResetConfirmText] = useState('');
  const [resetLoading, setResetLoading] = useState(false);

  // ===== STATS =====
  const [stats, setStats] = useState({
    wirelessCount: 0,
    wiredCount: 0,
    activeCount: 0,
    inactiveCount: 0
  });

  const loadZones = useCallback(async () => {
    if (!systemId) return;
    
    setLoading(true);
    setError('');
    try {
      const data = await fetchZones(systemId);
      setZones(data.zones || []);
      setStats({
        wirelessCount: data.wirelessCount || 0,
        wiredCount: data.wiredCount || 0,
        activeCount: data.activeCount || 0,
        inactiveCount: data.inactiveCount || 0
      });
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

  // ===== OPEN EDIT MODAL =====
  const openEditModal = (zone) => {
    setEditingZone(zone);
    setEditForm({
      zoneName: zone.zoneName || '',
      zoneType: zone.zoneType || 1,
      isActive: zone.isActive !== undefined ? zone.isActive : true,
      description: zone.description || ''
    });
    setEditModalOpen(true);
    setError('');
    setSuccess('');
  };

  // ===== CLOSE EDIT MODAL =====
  const closeEditModal = () => {
    setEditModalOpen(false);
    setEditingZone(null);
    setEditForm({
      zoneName: '',
      zoneType: 1,
      isActive: true,
      description: ''
    });
    setError('');
  };

  // ===== HANDLE FORM CHANGE =====
  const handleFormChange = (field, value) => {
    setEditForm(prev => ({ ...prev, [field]: value }));
  };

  // ===== PREPARE SAVE CONFIRMATION =====
  const prepareSave = () => {
    const changes = [];
    const original = editingZone;
    
    if (editForm.zoneName !== original.zoneName) {
      changes.push(`Zone Name: "${original.zoneName}" → "${editForm.zoneName}"`);
    }
    if (editForm.zoneType !== original.zoneType) {
      const oldLabel = ZONE_TYPE_LABELS[original.zoneType] || 'UNKNOWN';
      const newLabel = ZONE_TYPE_LABELS[editForm.zoneType] || 'UNKNOWN';
      changes.push(`Zone Type: ${oldLabel} → ${newLabel}`);
    }
    if (editForm.isActive !== original.isActive) {
      changes.push(`Status: ${original.isActive ? 'Active' : 'Inactive'} → ${editForm.isActive ? 'Active' : 'Inactive'}`);
    }
    if (editForm.description !== (original.description || '')) {
      changes.push('Description updated');
    }

    if (changes.length === 0) {
      setError('No changes to save');
      setTimeout(() => setError(''), 3000);
      return;
    }

    setConfirmChanges(changes);
    setConfirmModalOpen(true);
  };

  // ===== CONFIRM AND SAVE =====
  const confirmSave = async () => {
    setConfirmLoading(true);
    setError('');
    setSuccess('');

    try {
      await updateZone(editingZone.id, {
        zoneName: editForm.zoneName,
        zoneType: editForm.zoneType,
        isActive: editForm.isActive,
        description: editForm.description
      });
      
      await loadZones();
      setSuccess('✅ Zone updated successfully!');
      setConfirmModalOpen(false);
      setEditModalOpen(false);
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to update zone');
      setTimeout(() => setError(''), 3000);
    } finally {
      setConfirmLoading(false);
    }
  };

  // ===== TOGGLE ACTIVE (Direct from card) =====
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

  // ===== RESET ZONES =====
  const handleResetZones = () => {
    setShowResetConfirm(true);
    setResetConfirmText('');
    setError('');
    setSuccess('');
  };

  const confirmResetZones = async () => {
    if (resetConfirmText.toLowerCase() !== 'reset') {
      setError('Please type "reset" to confirm');
      return;
    }

    setResetLoading(true);
    setError('');
    setSuccess('');

    try {
      await resetZones(systemId);
      await loadZones();
      setSuccess('Zones reset to default successfully');
      setShowResetConfirm(false);
      setResetConfirmText('');
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to reset zones');
      setTimeout(() => setError(''), 3000);
    } finally {
      setResetLoading(false);
    }
  };

  const cancelReset = () => {
    setShowResetConfirm(false);
    setResetConfirmText('');
    setError('');
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

  const wirelessZones = filteredZones.filter(z => z.zoneCategory === 'WIRELESS');
  const wiredZones = filteredZones.filter(z => z.zoneCategory === 'WIRED');

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[150] flex items-center justify-end p-4 bg-black/80 backdrop-blur-sm">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-5xl w-full max-h-[90vh] overflow-hidden shadow-2xl shadow-red-500/10">
        
        {/* Header */}
        <div className="flex justify-between items-center p-5 border-b border-slate-800 bg-slate-950/40">
          <div>
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <span className="text-red-500">🔐</span> Zone Management
            </h2>
            <p className="text-xs text-slate-400 font-mono">{systemCode} - {zones.length} Zones</p>
          </div>
          <button 
            onClick={onClose}
            className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
          >
            <X className="w-5 h-5 text-slate-400 hover:text-white" />
          </button>
        </div>

        {/* Stats Bar */}
        <div className="px-5 py-3 border-b border-slate-800 bg-slate-950/20 flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-3 text-xs">
            <span className="text-slate-400">📶 Wireless:</span>
            <span className="text-white font-bold">{stats.wirelessCount}</span>
            <span className="text-slate-600">|</span>
            <span className="text-slate-400">🔌 Wired:</span>
            <span className="text-white font-bold">{stats.wiredCount}</span>
            <span className="text-slate-600">|</span>
            <span className="text-slate-400">🟢 Active:</span>
            <span className="text-emerald-400 font-bold">{stats.activeCount}</span>
            <span className="text-slate-600">|</span>
            <span className="text-slate-400">🔴 Inactive:</span>
            <span className="text-red-400 font-bold">{stats.inactiveCount}</span>
          </div>
        </div>

        {/* Content */}
        <div className="p-5 overflow-y-auto max-h-[calc(90vh-200px)]">
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
            <div className="space-y-6">
              
              {/* Wireless Zones */}
              {wirelessZones.length > 0 && (
                <div>
                  <div className="flex items-center gap-2 mb-3">
                    <Wifi className="w-4 h-4 text-blue-400" />
                    <h3 className="text-sm font-bold text-white">Wireless Zones</h3>
                    <span className="text-xs text-slate-500">({wirelessZones.length})</span>
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
                    {wirelessZones.map((zone) => (
                      <ZoneCard
                        key={zone.id}
                        zone={zone}
                        onEdit={openEditModal}
                        onToggleActive={handleToggleActive}
                        getZoneTypeLabel={getZoneTypeLabel}
                        getZoneTypeColor={getZoneTypeColor}
                      />
                    ))}
                  </div>
                </div>
              )}

              {/* Wired Zones */}
              {wiredZones.length > 0 && (
                <div>
                  <div className="flex items-center gap-2 mb-3">
                    <Plug className="w-4 h-4 text-amber-400" />
                    <h3 className="text-sm font-bold text-white">Wired Zones</h3>
                    <span className="text-xs text-slate-500">({wiredZones.length})</span>
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
                    {wiredZones.map((zone) => (
                      <ZoneCard
                        key={zone.id}
                        zone={zone}
                        onEdit={openEditModal}
                        onToggleActive={handleToggleActive}
                        getZoneTypeLabel={getZoneTypeLabel}
                        getZoneTypeColor={getZoneTypeColor}
                      />
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* ===== EDIT MODAL ===== */}
      {editModalOpen && editingZone && (
        <EditModal
          zone={editingZone}
          form={editForm}
          onFormChange={handleFormChange}
          onSave={prepareSave}
          onClose={closeEditModal}
          zoneTypes={zoneTypes}
        />
      )}

      {/* ===== CONFIRMATION MODAL ===== */}
      {confirmModalOpen && (
        <ConfirmModal
          changes={confirmChanges}
          onConfirm={confirmSave}
          onCancel={() => setConfirmModalOpen(false)}
          loading={confirmLoading}
        />
      )}

      {/* ===== RESET CONFIRMATION MODAL ===== */}
      {showResetConfirm && (
        <ResetConfirmModal
          systemCode={systemCode}
          resetConfirmText={resetConfirmText}
          setResetConfirmText={setResetConfirmText}
          onConfirm={confirmResetZones}
          onCancel={cancelReset}
          loading={resetLoading}
          error={error}
        />
      )}
    </div>
  );
}

// ===== ZONE CARD COMPONENT =====
function ZoneCard({ zone, onEdit, onToggleActive, getZoneTypeLabel, getZoneTypeColor }) {
  const isActive = zone.isActive;

  return (
    <div className={`bg-slate-950 border rounded-xl p-3 transition-all ${
      isActive 
        ? 'border-emerald-500/30 hover:border-emerald-500/50' 
        : 'border-red-500/20 opacity-60 hover:opacity-80'
    }`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className={`text-xs font-mono font-bold ${isActive ? 'text-emerald-400' : 'text-red-400'}`}>
            Z{String(zone.zoneNumber).padStart(2, '0')}
          </span>
          <span className={`text-[9px] font-mono px-1.5 py-0.5 rounded-full border ${getZoneTypeColor(zone.zoneType)}`}>
            {getZoneTypeLabel(zone.zoneType)}
          </span>
        </div>
        <div className="flex items-center gap-1">
          <button
            onClick={() => onToggleActive(zone.id, isActive)}
            className={`p-1 rounded-lg transition-all ${
              isActive 
                ? 'text-emerald-500 hover:bg-emerald-500/20' 
                : 'text-red-400 hover:bg-red-500/20'
            }`}
            title={isActive ? 'Active' : 'Inactive'}
          >
            {isActive ? <Power className="w-3.5 h-3.5" /> : <PowerOff className="w-3.5 h-3.5" />}
          </button>
          <button
            onClick={() => onEdit(zone)}
            className="p-1 text-slate-500 hover:text-white hover:bg-slate-800 rounded-lg transition-all"
            title="Edit zone"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
      <div className="mt-1">
        <p className={`text-xs font-medium truncate ${isActive ? 'text-white' : 'text-slate-400'}`}>
          {zone.zoneName}
        </p>
        <p className="text-[9px] text-slate-500 truncate font-mono">
          {zone.description || 'No description'}
        </p>
      </div>
    </div>
  );
}

ZoneCard.propTypes = {
  zone: PropTypes.object.isRequired,
  onEdit: PropTypes.func.isRequired,
  onToggleActive: PropTypes.func.isRequired,
  getZoneTypeLabel: PropTypes.func.isRequired,
  getZoneTypeColor: PropTypes.func.isRequired,
};

// ===== EDIT MODAL =====
function EditModal({ zone, form, onFormChange, onSave, onClose, zoneTypes }) {
  return (
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full shadow-2xl shadow-blue-500/10">
        <div className="flex justify-between items-center p-5 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <Edit2 className="w-5 h-5 text-blue-400" />
            <h3 className="text-lg font-bold text-white">Edit Zone - Z{String(zone.zoneNumber).padStart(2, '0')}</h3>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-slate-800 rounded-lg transition-colors">
            <X className="w-5 h-5 text-slate-400 hover:text-white" />
          </button>
        </div>

        <div className="p-5 space-y-4">
          {/* Zone Name */}
          <div>
            <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">Zone Name</label>
            <input
              type="text"
              value={form.zoneName}
              onChange={(e) => onFormChange('zoneName', e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-blue-500/50 transition-all"
            />
          </div>

          {/* Zone Type */}
          <div>
            <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">Zone Type</label>
            <select
              value={form.zoneType}
              onChange={(e) => onFormChange('zoneType', parseInt(e.target.value))}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-blue-500/50 transition-all"
            >
              {zoneTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label} - {type.description}
                </option>
              ))}
            </select>
          </div>

          {/* Description */}
          <div>
            <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">Description</label>
            <input
              type="text"
              value={form.description}
              onChange={(e) => onFormChange('description', e.target.value)}
              placeholder="Add description..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-blue-500/50 transition-all"
            />
          </div>

          {/* Status Toggle */}
          <div>
            <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">Status</label>
            <div className="flex gap-3 mt-1.5">
              <button
                onClick={() => onFormChange('isActive', true)}
                className={`flex-1 py-2 rounded-xl text-sm font-mono transition-all ${
                  form.isActive 
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/50' 
                    : 'bg-slate-800 text-slate-400 border border-slate-700 hover:border-slate-500'
                }`}
              >
                🟢 Active
              </button>
              <button
                onClick={() => onFormChange('isActive', false)}
                className={`flex-1 py-2 rounded-xl text-sm font-mono transition-all ${
                  !form.isActive 
                    ? 'bg-red-500/20 text-red-400 border border-red-500/50' 
                    : 'bg-slate-800 text-slate-400 border border-slate-700 hover:border-slate-500'
                }`}
              >
                🔴 Inactive
              </button>
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-2 border-t border-slate-800">
            <button
              onClick={onClose}
              className="flex-1 py-2.5 border border-slate-700 text-slate-400 hover:text-white rounded-xl text-sm font-mono transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={onSave}
              className="flex-1 py-2.5 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-500 hover:to-blue-600 text-white font-bold rounded-xl text-sm font-mono transition-all uppercase tracking-wide flex items-center justify-center gap-2"
            >
              <Save className="w-4 h-4" />
              Save Changes
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

EditModal.propTypes = {
  zone: PropTypes.object.isRequired,
  form: PropTypes.object.isRequired,
  onFormChange: PropTypes.func.isRequired,
  onSave: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  zoneTypes: PropTypes.array.isRequired,
};

// ===== CONFIRMATION MODAL =====
function ConfirmModal({ changes, onConfirm, onCancel, loading }) {
  return (
    <div className="fixed inset-0 z-[250] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="bg-slate-900 border border-yellow-500/30 rounded-2xl max-w-md w-full shadow-2xl shadow-yellow-500/10">
        <div className="flex justify-between items-center p-5 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-yellow-500" />
            <h3 className="text-lg font-bold text-white">Confirm Changes</h3>
          </div>
          <button onClick={onCancel} className="p-2 hover:bg-slate-800 rounded-lg transition-colors">
            <X className="w-5 h-5 text-slate-400 hover:text-white" />
          </button>
        </div>

        <div className="p-5 space-y-4">
          <p className="text-sm text-slate-400">You are about to make these changes:</p>
          <ul className="space-y-1.5">
            {changes.map((change, index) => (
              <li key={index} className="text-sm text-slate-300 font-mono flex items-start gap-2">
                <span className="text-yellow-400">•</span>
                {change}
              </li>
            ))}
          </ul>

          <div className="flex gap-3 pt-2">
            <button
              onClick={onCancel}
              className="flex-1 py-2.5 border border-slate-700 text-slate-400 hover:text-white rounded-xl text-sm font-mono transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={onConfirm}
              disabled={loading}
              className="flex-1 py-2.5 bg-gradient-to-r from-yellow-600 to-yellow-700 hover:from-yellow-500 hover:to-yellow-600 text-white font-bold rounded-xl text-sm font-mono transition-all uppercase tracking-wide flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>
                  <CheckCircle className="w-4 h-4" />
                  Confirm & Save
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

ConfirmModal.propTypes = {
  changes: PropTypes.array.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  loading: PropTypes.bool,
};

// ===== RESET CONFIRMATION MODAL =====
function ResetConfirmModal({ 
  systemCode, 
  resetConfirmText, 
  setResetConfirmText, 
  onConfirm, 
  onCancel, 
  loading, 
  error 
}) {
  return (
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="bg-slate-900 border border-yellow-500/30 rounded-2xl max-w-md w-full shadow-2xl shadow-yellow-500/10">
        <div className="flex justify-between items-center p-5 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-6 h-6 text-yellow-500" />
            <h3 className="text-lg font-bold text-white">⚠️ Reset All Zones?</h3>
          </div>
          <button onClick={onCancel} className="p-2 hover:bg-slate-800 rounded-lg transition-colors">
            <X className="w-5 h-5 text-slate-400 hover:text-white" />
          </button>
        </div>

        <div className="p-5 space-y-4">
          <div>
            <p className="text-sm text-slate-300">
              This will reset <span className="text-yellow-400 font-bold">ALL 24 zones</span> to default settings for <span className="text-white font-bold">{systemCode}</span>.
            </p>
            <ul className="mt-2 text-xs text-slate-400 space-y-1 list-disc list-inside">
              <li>Zone names will be reset to default</li>
              <li>Zone types will be reset to <span className="text-blue-400">PERIMETER</span></li>
              <li>All custom descriptions will be removed</li>
            </ul>
            <p className="mt-3 text-sm text-red-400 font-bold">⚠️ This action CANNOT be undone!</p>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold tracking-wide uppercase text-slate-400 font-mono">
              Type <span className="text-yellow-400">&quot;reset&quot;</span> to confirm
            </label>
            <input
              type="text"
              value={resetConfirmText}
              onChange={(e) => setResetConfirmText(e.target.value)}
              placeholder='Type "reset" here...'
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-600 focus:outline-none focus:border-yellow-500/50 focus:ring-1 focus:ring-yellow-500/50 transition-all font-mono"
              onKeyDown={(e) => {
                if (e.key === 'Enter' && resetConfirmText.toLowerCase() === 'reset') {
                  onConfirm();
                }
              }}
              autoFocus
            />
            {error && (
              <div className="text-red-400 text-xs font-mono">{error}</div>
            )}
          </div>

          <div className="flex gap-3 pt-2">
            <button
              onClick={onCancel}
              className="flex-1 py-2.5 border border-slate-700 text-slate-400 hover:text-white rounded-xl text-sm font-mono transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={onConfirm}
              disabled={loading || resetConfirmText.toLowerCase() !== 'reset'}
              className="flex-1 py-2.5 bg-gradient-to-r from-yellow-600 to-red-600 hover:from-yellow-500 hover:to-red-500 text-white font-bold rounded-xl text-sm font-mono transition-all uppercase tracking-wide flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>
                  <AlertTriangle className="w-4 h-4" />
                  Reset All Zones
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

ResetConfirmModal.propTypes = {
  systemCode: PropTypes.string.isRequired,
  resetConfirmText: PropTypes.string.isRequired,
  setResetConfirmText: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  loading: PropTypes.bool,
  error: PropTypes.string,
};

ZoneManagement.propTypes = {
  systemId: PropTypes.number,
  systemCode: PropTypes.string,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
};