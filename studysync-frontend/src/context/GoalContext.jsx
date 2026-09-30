import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import * as goalsApi from '../api/goals';

const GoalContext = createContext(null);

export function GoalProvider({ children }) {
  const [goals, setGoals] = useState([]);
  const [selectedGoalId, setSelectedGoalId] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadGoals = useCallback(async () => {
    setLoading(true);
    try {
      const data = await goalsApi.listGoals();
      setGoals(data);

      const active = data.filter((g) => g.status === 'ACTIVE');
      const sorted = [...active].sort((a, b) => a.startDate.localeCompare(b.startDate));
      const primary = sorted.length > 0 ? sorted[0].id : null;

      setSelectedGoalId((prev) => {
        const prevStillValid = prev && data.some((g) => g.id === prev);
        return prevStillValid ? prev : primary;
      });
    } catch {
      // ignore -- pages relying on this will show their own error states
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadGoals(); }, [loadGoals]);

  const activeGoals = goals.filter((g) => g.status === 'ACTIVE');
  const sortedActive = [...activeGoals].sort((a, b) => a.startDate.localeCompare(b.startDate));
  const primaryGoalId = sortedActive.length > 0 ? sortedActive[0].id : null;
  const selectedGoal = goals.find((g) => g.id === selectedGoalId) || null;

  return (
    <GoalContext.Provider value={{
      goals, activeGoals, selectedGoalId, setSelectedGoalId, primaryGoalId,
      selectedGoal, loading, refreshGoals: loadGoals,
    }}>
      {children}
    </GoalContext.Provider>
  );
}

export function useGoalContext() {
  const ctx = useContext(GoalContext);
  if (!ctx) throw new Error('useGoalContext must be used within GoalProvider');
  return ctx;
}
