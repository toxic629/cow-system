import { Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/auth';
import { AppLayout } from '../layouts/AppLayout';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { CowsPage } from '../pages/CowsPage';
import { CowDetailPage } from '../pages/CowDetailPage';
import { AlarmsPage } from '../pages/AlarmsPage';
import { RulesPage } from '../pages/RulesPage';
import { BarnEnvPage } from '../pages/BarnEnvPage';

function ProtectedRoute() {
  const token = useAuthStore((s) => s.token);
  const location = useLocation();
  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <Outlet />;
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/cows" element={<CowsPage />} />
          <Route path="/cows/:cowId" element={<CowDetailPage />} />
          <Route path="/barn-env" element={<BarnEnvPage />} />
          <Route path="/alarms" element={<AlarmsPage />} />
          <Route path="/rules" element={<RulesPage />} />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
