import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { Plus, Trash2, Save, ChevronLeft, ChevronRight, CalendarDays, ArrowRight, Car } from 'lucide-react';
import * as timetableApi from '../api/timetable';
import * as calendarApi from '../api/calendar';
import * as userApi from '../api/user';

function CommuteCard() {
  const [distance, setDistance] = useState('');
  const [saved, setSaved] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    userApi.getProfile().then((p) => {
      if (p.commuteDistanceKm != null) setDistance(String(p.commuteDistanceKm));
    }).catch(() => {});
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      const value = distance === '' ? null : parseFloat(distance);
      await userApi.updateCommuteDistance(value);
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-card p-5 mb-8 flex items-center gap-4 flex-wrap">
      <span className="w-10 h-10 rounded-xl bg-accent-500/10 text-accent-600 flex items-center justify-center shrink-0">
        <Car size={18} />
      </span>
      <div className="flex-1 min-w-[220px]">
        <p className="font-semibold text-slate-800 text-sm">One-way distance to college (km)</p>
        <p className="text-xs text-slate-400 mt-0.5">Used to calculate a realistic commute time in your schedule -- leave blank to use a sensible default.</p>
      </div>
      <div className="flex items-center gap-2">
        <input
          type="number" min="0" step="0.5" value={distance} onChange={(e) => setDistance(e.target.value)}
          placeholder="e.g. 8"
          className="w-28 px-3 py-2 rounded-xl border border-slate-200 text-sm focus:border-accent-500 outline-none transition-smooth"
        />
        <motion.button whileTap={{ scale: 0.96 }} onClick={handleSave} disabled={saving}
          className="px-4 py-2 rounded-xl bg-navy-800 text-white text-sm font-semibold hover:bg-navy-700 transition-smooth disabled:opacity-60">
          {saving ? 'Saving...' : saved ? 'Saved!' : 'Save'}
        </motion.button>
      </div>
    </div>
  );
}

function DayOrderEditor({ dayOrder, onSave, onDelete }) {
  const [label, setLabel] = useState(dayOrder.label || '');
  const [slots, setSlots] = useState(dayOrder.slots?.length ? dayOrder.slots : [{ subjectName: '', startTime: '09:00', endTime: '10:00' }]);
  const [saving, setSaving] = useState(false);

  const updateSlot = (idx, key, value) => {
    const next = [...slots];
    next[idx] = { ...next[idx], [key]: value };
    setSlots(next);
  };

  const addSlot = () => setSlots([...slots, { subjectName: '', startTime: '09:00', endTime: '10:00' }]);
  const removeSlot = (idx) => setSlots(slots.filter((_, i) => i !== idx));

  const handleSave = async () => {
    setSaving(true);
    try {
      await onSave({ dayOrderNumber: dayOrder.dayOrderNumber, label, slots });
    } finally {
      setSaving(false);
    }
  };

  return (
    <motion.div layout initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="bg-white rounded-2xl border border-navy-100 shadow-card p-5">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <span className="w-9 h-9 rounded-xl bg-accent-500 text-white flex items-center justify-center font-bold">
            {dayOrder.dayOrderNumber}
          </span>
          <input
            value={label} onChange={(e) => setLabel(e.target.value)} placeholder={`Day ${dayOrder.dayOrderNumber} label (optional)`}
            className="px-3 py-1.5 rounded-lg border border-navy-100 text-sm focus:border-accent-500 outline-none transition-smooth"
          />
        </div>
        {onDelete && (
          <button onClick={() => onDelete(dayOrder.id)} className="text-red-400 hover:text-red-600 transition-smooth">
            <Trash2 size={18} />
          </button>
        )}
      </div>

      <div className="space-y-2">
        {slots.map((slot, idx) => (
          <div key={idx} className="flex items-center gap-2">
            <input
              placeholder="Subject" value={slot.subjectName} onChange={(e) => updateSlot(idx, 'subjectName', e.target.value)}
              className="flex-1 px-3 py-2 rounded-lg border border-navy-100 text-sm focus:border-accent-500 outline-none transition-smooth"
            />
            <input
              type="time" value={slot.startTime} onChange={(e) => updateSlot(idx, 'startTime', e.target.value)}
              className="px-3 py-2 rounded-lg border border-navy-100 text-sm focus:border-accent-500 outline-none transition-smooth"
            />
            <span className="text-navy-300">-</span>
            <input
              type="time" value={slot.endTime} onChange={(e) => updateSlot(idx, 'endTime', e.target.value)}
              className="px-3 py-2 rounded-lg border border-navy-100 text-sm focus:border-accent-500 outline-none transition-smooth"
            />
            <button onClick={() => removeSlot(idx)} className="text-navy-300 hover:text-red-500 transition-smooth">
              <Trash2 size={16} />
            </button>
          </div>
        ))}
      </div>

      <div className="flex items-center justify-between mt-4">
        <button onClick={addSlot} className="text-sm text-accent-600 font-medium flex items-center gap-1 hover:underline">
          <Plus size={15} /> Add class
        </button>
        <motion.button
          whileTap={{ scale: 0.96 }} onClick={handleSave} disabled={saving}
          className="flex items-center gap-2 bg-navy-800 text-white text-sm font-semibold px-4 py-2 rounded-xl hover:bg-navy-700 transition-smooth disabled:opacity-60"
        >
          <Save size={15} /> {saving ? 'Saving...' : 'Save Day Order'}
        </motion.button>
      </div>
    </motion.div>
  );
}

const WEEKDAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

function toDateStr(d) {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function MonthCalendar({ dayOrders }) {
  const [monthDate, setMonthDate] = useState(() => {
    const d = new Date();
    d.setDate(1);
    return d;
  });
  const [entries, setEntries] = useState({});
  const [loading, setLoading] = useState(true);
  const [savingDate, setSavingDate] = useState(null);

  const year = monthDate.getFullYear();
  const month = monthDate.getMonth();
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  const startWeekday = firstDay.getDay();

  const load = async () => {
    setLoading(true);
    try {
      const start = toDateStr(firstDay);
      const end = toDateStr(lastDay);
      const data = await calendarApi.getRange(start, end);
      const map = {};
      data.forEach((e) => { map[e.date] = e; });
      setEntries(map);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [year, month]);

  const handleChange = async (dateStr, value) => {
    setSavingDate(dateStr);
    try {
      let payload;
      if (value === 'HOLIDAY') {
        payload = { date: dateStr, isHoliday: true, dayOrderNumber: null };
      } else if (value === 'UNSET') {
        payload = { date: dateStr, isHoliday: false, dayOrderNumber: null };
      } else {
        payload = { date: dateStr, isHoliday: false, dayOrderNumber: parseInt(value, 10) };
      }
      const saved = await calendarApi.setSingleEntry(payload);
      setEntries((prev) => ({ ...prev, [dateStr]: saved }));
    } finally {
      setSavingDate(null);
    }
  };

  const cells = [];
  for (let i = 0; i < startWeekday; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);

  const monthLabel = monthDate.toLocaleString('default', { month: 'long', year: 'numeric' });

  const getSelectValue = (entry) => {
    if (!entry) return 'UNSET';
    if (entry.isHoliday) return 'HOLIDAY';
    if (entry.dayOrderNumber) return String(entry.dayOrderNumber);
    return 'UNSET';
  };

  const cellStyle = (entry) => {
    if (!entry) return 'bg-navy-50/60 border-navy-100';
    if (entry.isHoliday) return 'bg-red-50 border-red-200';
    if (entry.dayOrderNumber) return 'bg-accent-500/10 border-accent-200';
    return 'bg-navy-50/60 border-navy-100';
  };

  return (
    <div className="bg-white rounded-2xl border border-navy-100 shadow-card p-6">
      <div className="flex items-center justify-between mb-5">
        <button onClick={() => setMonthDate(new Date(year, month - 1, 1))}
          className="p-2 rounded-lg hover:bg-navy-50 text-navy-500 transition-smooth">
          <ChevronLeft size={18} />
        </button>
        <h3 className="font-semibold text-navy-800">{monthLabel}</h3>
        <button onClick={() => setMonthDate(new Date(year, month + 1, 1))}
          className="p-2 rounded-lg hover:bg-navy-50 text-navy-500 transition-smooth">
          <ChevronRight size={18} />
        </button>
      </div>

      <div className="grid grid-cols-7 gap-1.5 text-center mb-2">
        {WEEKDAYS.map((w) => <div key={w} className="text-xs font-semibold text-navy-400 py-1">{w}</div>)}
      </div>

      {loading ? (
        <p className="text-navy-400 text-sm text-center py-8">Loading calendar...</p>
      ) : (
        <div className="grid grid-cols-7 gap-1.5">
          {cells.map((d, idx) => {
            if (d === null) return <div key={`empty-${idx}`} />;
            const dateObj = new Date(year, month, d);
            const dateStr = toDateStr(dateObj);
            const entry = entries[dateStr];
            const isSaving = savingDate === dateStr;

            return (
              <div key={dateStr} className={`rounded-xl border p-1.5 flex flex-col items-center gap-1 transition-smooth ${cellStyle(entry)}`}>
                <span className="text-xs font-semibold text-navy-700">{d}</span>
                <select
                  value={getSelectValue(entry)}
                  disabled={isSaving}
                  onChange={(e) => handleChange(dateStr, e.target.value)}
                  className="w-full text-[10px] rounded-md border-none bg-transparent focus:ring-1 focus:ring-accent-500 outline-none cursor-pointer disabled:opacity-50"
                >
                  <option value="UNSET">-</option>
                  {dayOrders.map((d2) => (
                    <option key={d2.dayOrderNumber} value={d2.dayOrderNumber}>Day {d2.dayOrderNumber}</option>
                  ))}
                  <option value="HOLIDAY">Holiday</option>
                </select>
              </div>
            );
          })}
        </div>
      )}

      <p className="text-xs text-navy-400 mt-4">
        Pick a Day-Order or "Holiday" for each date directly. If your college shifts the schedule mid-term, just change the affected days here — no need to redo anything else.
      </p>
    </div>
  );
}

export default function Timetable() {
  const [dayOrders, setDayOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const data = await timetableApi.getDayOrders();
      setDayOrders(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const addNewDayOrder = () => {
    const nextNumber = dayOrders.length ? Math.max(...dayOrders.map((d) => d.dayOrderNumber)) + 1 : 1;
    setDayOrders([...dayOrders, { dayOrderNumber: nextNumber, label: '', slots: [] }]);
  };

  const handleSaveDayOrder = async (payload) => {
    await timetableApi.saveDayOrder(payload);
    load();
  };

  const handleDeleteDayOrder = async (id) => {
    await timetableApi.deleteDayOrder(id);
    load();
  };

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
        <h1 className="font-display text-3xl font-bold text-navy-800">Timetable Setup</h1>
        <p className="text-navy-400 text-sm mt-1">
          Define your Day-Order class pattern, then mark each real calendar day with the Day-Order it follows.
        </p>
      </motion.div>

      <CommuteCard />

      <section className="mb-10">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-navy-800 text-lg">1. Day-Order Timetable Template</h2>
          <button onClick={addNewDayOrder} className="flex items-center gap-1 text-sm font-semibold text-accent-600 hover:underline">
            <Plus size={16} /> Add Day Order
          </button>
        </div>

        {loading ? (
          <p className="text-navy-400 text-sm">Loading...</p>
        ) : (
          <div className="space-y-4">
            {dayOrders.length === 0 && (
              <p className="text-navy-400 text-sm bg-white rounded-2xl p-6 text-center border border-navy-100">
                No day orders yet. Click "Add Day Order" to define Day 1, Day 2, etc.
              </p>
            )}
            {dayOrders.map((d, idx) => (
              <DayOrderEditor key={d.id ?? `new-${idx}`} dayOrder={d} onSave={handleSaveDayOrder} onDelete={d.id ? handleDeleteDayOrder : null} />
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="font-semibold text-navy-800 text-lg mb-4 flex items-center gap-2">
          <CalendarDays size={20} className="text-accent-500" /> 2. Academic Calendar
        </h2>
        {dayOrders.length === 0 ? (
          <p className="text-navy-400 text-sm bg-white rounded-2xl p-6 text-center border border-navy-100">
            Add at least one Day Order above before setting up your calendar.
          </p>
        ) : (
          <>
            <MonthCalendar dayOrders={dayOrders} />
            <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}
              className="mt-6 flex items-center justify-between bg-gradient-to-r from-navy-800 to-accent-600 rounded-2xl p-5 text-white">
              <div>
                <p className="font-semibold">Timetable looking good?</p>
                <p className="text-sm text-white/70 mt-0.5">Once you've marked your calendar through your target date, head to Goals to create your study plan.</p>
              </div>
              <Link to="/goals" className="shrink-0 flex items-center gap-2 bg-white text-navy-800 font-semibold px-4 py-2.5 rounded-xl hover:bg-ice transition-smooth">
                Go to Goals <ArrowRight size={16} />
              </Link>
            </motion.div>
          </>
        )}
      </section>
    </div>
  );
}
