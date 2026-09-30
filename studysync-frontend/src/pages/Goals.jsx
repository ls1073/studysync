import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, X, Search, Sparkles, Pencil, Trash2 } from 'lucide-react';
import * as goalsApi from '../api/goals';
import GoalCard from '../components/GoalCard';
import { useGoalContext } from '../context/GoalContext';

const emptyTopic = () => ({ name: '', estimatedHours: 2, weight: 2 });
const emptySubject = () => ({ name: '', topics: [emptyTopic()] });

function CreateGoalModal({ onClose, onCreated }) {
  const [mode, setMode] = useState('AUTO');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [targetDate, setTargetDate] = useState('');
  const [startDate, setStartDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [query, setQuery] = useState('');
  const [templates, setTemplates] = useState([]);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [manualSubjects, setManualSubjects] = useState([emptySubject()]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (mode === 'AUTO') {
      goalsApi.searchTemplates(query).then(setTemplates).catch(() => {});
    }
  }, [mode, query]);

  const updateManualSubject = (idx, key, value) => {
    const next = [...manualSubjects];
    next[idx] = { ...next[idx], [key]: value };
    setManualSubjects(next);
  };
  const updateManualTopic = (sIdx, tIdx, key, value) => {
    const next = [...manualSubjects];
    next[sIdx].topics[tIdx] = { ...next[sIdx].topics[tIdx], [key]: value };
    setManualSubjects(next);
  };
  const addManualSubject = () => setManualSubjects([...manualSubjects, emptySubject()]);
  const removeManualSubject = (sIdx) => setManualSubjects(manualSubjects.filter((_, i) => i !== sIdx));
  const addManualTopic = (sIdx) => {
    const next = [...manualSubjects];
    next[sIdx].topics.push(emptyTopic());
    setManualSubjects(next);
  };
  const removeManualTopic = (sIdx, tIdx) => {
    const next = [...manualSubjects];
    next[sIdx] = { ...next[sIdx], topics: next[sIdx].topics.filter((_, i) => i !== tIdx) };
    setManualSubjects(next);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const payload = { title, description, startDate, targetDate, mode };
      if (mode === 'AUTO') {
        if (!selectedTemplate) { setError('Please select a template'); setSubmitting(false); return; }
        payload.templateName = selectedTemplate.name;
      } else {
        // Blank subjects, and blank topics within a subject, are dropped here so an
        // accidental empty row never becomes real (invisible, confusing) goal data.
        const cleanedSubjects = manualSubjects
          .filter((s) => s.name.trim())
          .map((s) => ({ ...s, topics: s.topics.filter((t) => t.name.trim()) }))
          .filter((s) => s.topics.length > 0);

        if (cleanedSubjects.length === 0) {
          setError('Please add at least one subject with at least one named topic.');
          setSubmitting(false);
          return;
        }
        payload.subjects = cleanedSubjects;
      }
      await goalsApi.createGoal(payload);
      onCreated();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create goal');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      className="fixed inset-0 bg-navy-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={onClose}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 10 }} animate={{ opacity: 1, scale: 1, y: 0 }} exit={{ opacity: 0, scale: 0.95 }}
        onClick={(e) => e.stopPropagation()}
        className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[85vh] overflow-y-auto p-6"
      >
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-display text-2xl font-bold text-navy-800">New Goal</h2>
          <button onClick={onClose} className="text-navy-400 hover:text-navy-700"><X size={22} /></button>
        </div>

        {error && <div className="mb-4 text-sm text-red-600 bg-red-50 px-4 py-2 rounded-lg">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="text-sm font-medium text-navy-700">Goal Title</label>
            <input required value={title} onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Crack UPSC CSE 2027"
              className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 outline-none transition-smooth" />
          </div>
          <div>
            <label className="text-sm font-medium text-navy-700">Description (optional)</label>
            <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={2}
              className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 outline-none transition-smooth" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-navy-700">Start Date</label>
              <input type="date" required value={startDate} onChange={(e) => setStartDate(e.target.value)}
                className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 outline-none transition-smooth" />
            </div>
            <div>
              <label className="text-sm font-medium text-navy-700">Target Date</label>
              <input type="date" required value={targetDate} onChange={(e) => setTargetDate(e.target.value)}
                className="mt-1 w-full px-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 outline-none transition-smooth" />
            </div>
          </div>

          <div className="flex gap-3">
            <button type="button" onClick={() => setMode('AUTO')}
              className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl border-2 font-semibold text-sm transition-smooth ${
                mode === 'AUTO' ? 'border-accent-500 bg-accent-500/10 text-accent-600' : 'border-navy-100 text-navy-400'
              }`}>
              <Sparkles size={16} /> Auto (Suggested)
            </button>
            <button type="button" onClick={() => setMode('MANUAL')}
              className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl border-2 font-semibold text-sm transition-smooth ${
                mode === 'MANUAL' ? 'border-accent-500 bg-accent-500/10 text-accent-600' : 'border-navy-100 text-navy-400'
              }`}>
              <Pencil size={16} /> Manual
            </button>
          </div>

          <AnimatePresence mode="wait">
            {mode === 'AUTO' ? (
              <motion.div key="auto" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="space-y-3">
                <div className="relative">
                  <Search size={16} className="absolute left-3 top-3 text-navy-300" />
                  <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search: UPSC, GATE, Placement, Bank PO..."
                    className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-navy-100 focus:border-accent-500 outline-none transition-smooth" />
                </div>
                <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto">
                  {templates.map((t) => (
                    <button type="button" key={t.id} onClick={() => setSelectedTemplate(t)}
                      className={`text-left p-3 rounded-xl border-2 transition-smooth ${
                        selectedTemplate?.id === t.id ? 'border-accent-500 bg-accent-500/10' : 'border-navy-100 hover:border-navy-200'
                      }`}>
                      <p className="font-semibold text-sm text-navy-800">{t.name}</p>
                      <p className="text-xs text-navy-400 mt-0.5 line-clamp-2">{t.description}</p>
                    </button>
                  ))}
                </div>
                {templates.length === 0 && (
                  <p className="text-sm text-navy-400 text-center py-4">
                    No template found. Switch to Manual mode to define your own subjects.
                  </p>
                )}
              </motion.div>
            ) : (
              <motion.div key="manual" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="space-y-4">
                {manualSubjects.map((subject, sIdx) => (
                  <div key={sIdx} className="border border-navy-100 rounded-xl p-4">
                    <div className="flex gap-2 mb-2">
                      <input value={subject.name} onChange={(e) => updateManualSubject(sIdx, 'name', e.target.value)}
                        placeholder="Subject name" className="flex-1 px-3 py-2 rounded-lg border border-navy-100 text-sm outline-none focus:border-accent-500" />
                      {manualSubjects.length > 1 && (
                        <button type="button" onClick={() => removeManualSubject(sIdx)}
                          className="px-2 text-navy-300 hover:text-red-500 transition-smooth" title="Remove subject">
                          <Trash2 size={16} />
                        </button>
                      )}
                    </div>
                    {subject.topics.map((topic, tIdx) => (
                      <div key={tIdx} className="flex gap-2 mb-1.5 items-center">
                        <input value={topic.name} onChange={(e) => updateManualTopic(sIdx, tIdx, 'name', e.target.value)}
                          placeholder="Topic name" className="flex-1 px-3 py-1.5 rounded-lg border border-navy-100 text-sm outline-none focus:border-accent-500" />
                        <input type="number" min="0.5" step="0.5" value={topic.estimatedHours}
                          onChange={(e) => updateManualTopic(sIdx, tIdx, 'estimatedHours', parseFloat(e.target.value))}
                          title="Estimated hours"
                          className="w-16 px-2 py-1.5 rounded-lg border border-navy-100 text-sm outline-none focus:border-accent-500" />
                        <select value={topic.weight} onChange={(e) => updateManualTopic(sIdx, tIdx, 'weight', parseFloat(e.target.value))}
                          className="px-2 py-1.5 rounded-lg border border-navy-100 text-xs outline-none focus:border-accent-500 bg-white">
                          <option value={1}>Low</option>
                          <option value={2}>Medium</option>
                          <option value={3}>High</option>
                        </select>
                        {subject.topics.length > 1 && (
                          <button type="button" onClick={() => removeManualTopic(sIdx, tIdx)}
                            className="text-navy-300 hover:text-red-500 transition-smooth shrink-0" title="Remove topic">
                            <Trash2 size={14} />
                          </button>
                        )}
                      </div>
                    ))}
                    <button type="button" onClick={() => addManualTopic(sIdx)} className="text-xs text-accent-600 font-medium hover:underline mt-1">
                      + Add topic
                    </button>
                  </div>
                ))}
                <button type="button" onClick={addManualSubject} className="text-sm text-accent-600 font-semibold hover:underline">
                  + Add subject
                </button>
              </motion.div>
            )}
          </AnimatePresence>

          <motion.button whileTap={{ scale: 0.98 }} type="submit" disabled={submitting}
            className="w-full bg-navy-800 text-white font-semibold py-3 rounded-xl hover:bg-navy-700 transition-smooth disabled:opacity-60">
            {submitting ? 'Creating your plan...' : 'Create Goal & Generate Schedule'}
          </motion.button>
        </form>
      </motion.div>
    </motion.div>
  );
}

export default function Goals() {
  const { goals, loading, refreshGoals } = useGoalContext();
  const [showModal, setShowModal] = useState(false);

  return (
    <div className="p-8 max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="font-display text-3xl font-bold text-navy-800">Goals</h1>
          <p className="text-navy-400 text-sm mt-1">Every goal builds its own adaptive schedule.</p>
        </div>
        <motion.button whileTap={{ scale: 0.96 }} onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-accent-500 text-white font-semibold px-5 py-2.5 rounded-xl hover:bg-accent-600 transition-smooth shadow-glow">
          <Plus size={18} /> New Goal
        </motion.button>
      </div>

      {loading ? (
        <p className="text-navy-400 text-sm">Loading...</p>
      ) : goals.length === 0 ? (
        <div className="bg-white rounded-2xl p-12 text-center border border-navy-100">
          <p className="text-navy-500">No goals yet. Create your first one to get a full study plan.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {goals.map((g) => <GoalCard key={g.id} goal={g} />)}
        </div>
      )}

      <AnimatePresence>
        {showModal && (
          <CreateGoalModal onClose={() => setShowModal(false)} onCreated={() => { setShowModal(false); refreshGoals(); }} />
        )}
      </AnimatePresence>
    </div>
  );
}
