import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuth } from '../context/AuthContext';
import { UserPlus } from 'lucide-react';

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-navy-800 via-navy-700 to-accent-600 px-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="w-full max-w-md bg-white rounded-2xl shadow-2xl p-8"
      >
        <div className="text-center mb-8">
          <h1 className="font-display text-3xl font-bold text-navy-800">Create your account</h1>
          <p className="text-navy-400 text-sm mt-2">Set up StudySync in a minute.</p>
        </div>

        {error && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="mb-4 text-sm text-red-600 bg-red-50 px-4 py-2 rounded-lg">
            {error}
          </motion.div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-medium text-navy-700">Full Name</label>
            <input value={form.fullName} onChange={update('fullName')}
              className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 focus:ring-2 focus:ring-accent-500/20 outline-none transition-smooth" />
          </div>
          <div>
            <label className="text-sm font-medium text-navy-700">Username</label>
            <input required value={form.username} onChange={update('username')}
              className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 focus:ring-2 focus:ring-accent-500/20 outline-none transition-smooth" />
          </div>
          <div>
            <label className="text-sm font-medium text-navy-700">Email</label>
            <input type="email" required value={form.email} onChange={update('email')}
              className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 focus:ring-2 focus:ring-accent-500/20 outline-none transition-smooth" />
          </div>
          <div>
            <label className="text-sm font-medium text-navy-700">Password</label>
            <input type="password" required minLength={6} value={form.password} onChange={update('password')}
              className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 focus:ring-2 focus:ring-accent-500/20 outline-none transition-smooth" />
          </div>

          <motion.button
            whileTap={{ scale: 0.98 }}
            type="submit" disabled={loading}
            className="w-full flex items-center justify-center gap-2 bg-accent-500 text-white font-semibold py-2.5 rounded-xl hover:bg-accent-600 transition-smooth disabled:opacity-60"
          >
            <UserPlus size={18} /> {loading ? 'Creating account...' : 'Create Account'}
          </motion.button>
        </form>

        <p className="text-center text-sm text-navy-400 mt-6">
          Already have an account? <Link to="/login" className="text-accent-600 font-semibold hover:underline">Sign in</Link>
        </p>
      </motion.div>
    </div>
  );
}
