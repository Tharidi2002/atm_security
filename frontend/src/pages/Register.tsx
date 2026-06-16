import { Building2, Lock, Mail, Shield, User } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { bankApi } from '../services/api';
import type { Bank } from '../types';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [banks, setBanks] = useState<Bank[]>([]);
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    bankId: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    bankApi
      .list()
      .then((res) => setBanks(res.data.data))
      .catch(() => setError('Failed to load banks'));
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (!form.bankId) {
      setError('Please select a bank');
      return;
    }

    setLoading(true);
    try {
      await register({
        username: form.username,
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        bankId: Number(form.bankId),
      });
      navigate('/');
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Registration failed';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const update = (field: string, value: string) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-950 via-slate-900 to-primary-900 px-4 py-8">
      <div className="w-full max-w-lg">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary-600/20 border border-primary-500/30 mb-4">
            <Shield className="w-8 h-8 text-primary-500" />
          </div>
          <h1 className="text-2xl font-bold">Create Account</h1>
          <p className="text-slate-400 mt-2">Register for bank-scoped monitoring access</p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="bg-slate-900/80 backdrop-blur border border-slate-800 rounded-2xl p-8 shadow-2xl"
        >
          {error && (
            <div className="mb-4 p-3 rounded-lg bg-danger-500/10 border border-danger-500/30 text-danger-500 text-sm">
              {error}
            </div>
          )}

          <div className="grid grid-cols-1 gap-4">
            <Field icon={User} label="Full Name">
              <input
                type="text"
                value={form.fullName}
                onChange={(e) => update('fullName', e.target.value)}
                className="input-field"
                required
              />
            </Field>

            <Field icon={User} label="Username">
              <input
                type="text"
                value={form.username}
                onChange={(e) => update('username', e.target.value)}
                className="input-field"
                required
                minLength={3}
              />
            </Field>

            <Field icon={Mail} label="Email">
              <input
                type="email"
                value={form.email}
                onChange={(e) => update('email', e.target.value)}
                className="input-field"
                required
              />
            </Field>

            <Field icon={Building2} label="Bank">
              <select
                value={form.bankId}
                onChange={(e) => update('bankId', e.target.value)}
                className="input-field"
                required
              >
                <option value="">Select your bank</option>
                {banks.map((bank) => (
                  <option key={bank.id} value={bank.id}>
                    {bank.name}
                  </option>
                ))}
              </select>
            </Field>

            <Field icon={Lock} label="Password">
              <input
                type="password"
                value={form.password}
                onChange={(e) => update('password', e.target.value)}
                className="input-field"
                required
                minLength={8}
              />
            </Field>

            <Field icon={Lock} label="Confirm Password">
              <input
                type="password"
                value={form.confirmPassword}
                onChange={(e) => update('confirmPassword', e.target.value)}
                className="input-field"
                required
                minLength={8}
              />
            </Field>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full mt-6 py-2.5 bg-primary-600 hover:bg-primary-700 disabled:opacity-50 rounded-lg font-medium"
          >
            {loading ? 'Creating account...' : 'Register'}
          </button>

          <p className="text-center text-sm text-slate-400 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-500 hover:text-primary-400">
              Sign In
            </Link>
          </p>
        </form>
      </div>

      <style>{`
        .input-field {
          width: 100%;
          padding: 0.625rem 1rem;
          background: rgb(30 41 59);
          border: 1px solid rgb(51 65 85);
          border-radius: 0.5rem;
          color: white;
          outline: none;
        }
        .input-field:focus {
          ring: 2px;
          border-color: rgb(59 130 246);
          box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
        }
      `}</style>
    </div>
  );
}

function Field({
  icon: Icon,
  label,
  children,
}: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <label className="block text-sm text-slate-400 mb-1.5">{label}</label>
      <div className="relative">
        <Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500 pointer-events-none" />
        <div className="pl-10">{children}</div>
      </div>
    </div>
  );
}
