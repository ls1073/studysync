import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { AuthProvider, useAuth } from './context/AuthContext';
import { GoalProvider } from './context/GoalContext';
import ProtectedRoute from './components/ProtectedRoute';
import Sidebar from './components/Sidebar';
import GoalSwitcher from './components/GoalSwitcher';
import useTaskNotifications from './hooks/useTaskNotifications';

import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Goals from './pages/Goals';
import GoalDetail from './pages/GoalDetail';
import Timetable from './pages/Timetable';
import Schedule from './pages/Schedule';
import Tasks from './pages/Tasks';
import MentalLoad from './pages/MentalLoad';
import Reports from './pages/Reports';

function PageTransition({ children }) {
  const location = useLocation();
  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={location.pathname}
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -8 }}
        transition={{ duration: 0.2 }}
      >
        {children}
      </motion.div>
    </AnimatePresence>
  );
}

function AppLayout({ children, showGoalSwitcher }) {
  const { isAuthenticated } = useAuth();
  useTaskNotifications(isAuthenticated);

  return (
    <div className="flex min-h-screen bg-surface">
      <Sidebar />
      <main className="flex-1">
        {showGoalSwitcher && (
          <div className="px-8 pt-6">
            <GoalSwitcher />
          </div>
        )}
        <PageTransition>{children}</PageTransition>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <GoalProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route path="/" element={
              <ProtectedRoute><AppLayout showGoalSwitcher><Dashboard /></AppLayout></ProtectedRoute>
            } />
            <Route path="/goals" element={
              <ProtectedRoute><AppLayout><Goals /></AppLayout></ProtectedRoute>
            } />
            <Route path="/goals/:id" element={
              <ProtectedRoute><AppLayout><GoalDetail /></AppLayout></ProtectedRoute>
            } />
            <Route path="/timetable" element={
              <ProtectedRoute><AppLayout><Timetable /></AppLayout></ProtectedRoute>
            } />
            <Route path="/schedule" element={
              <ProtectedRoute><AppLayout showGoalSwitcher><Schedule /></AppLayout></ProtectedRoute>
            } />
            <Route path="/tasks" element={
              <ProtectedRoute><AppLayout showGoalSwitcher><Tasks /></AppLayout></ProtectedRoute>
            } />
            <Route path="/mental-load" element={
              <ProtectedRoute><AppLayout><MentalLoad /></AppLayout></ProtectedRoute>
            } />
            <Route path="/reports" element={
              <ProtectedRoute><AppLayout showGoalSwitcher><Reports /></AppLayout></ProtectedRoute>
            } />
          </Routes>
        </BrowserRouter>
      </GoalProvider>
    </AuthProvider>
  );
}
