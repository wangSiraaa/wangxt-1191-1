import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import Login from './pages/Login';
import MainLayout from './components/MainLayout';
import BlasterDashboard from './pages/blaster/Dashboard';
import CreateShift from './pages/blaster/CreateShift';
import CreateApplication from './pages/blaster/CreateApplication';
import StorekeeperDashboard from './pages/storekeeper/Dashboard';
import Outbound from './pages/storekeeper/Outbound';
import Inbound from './pages/storekeeper/Inbound';
import SafetyDashboard from './pages/safety/Dashboard';
import ReviewApplication from './pages/safety/ReviewApplication';
import Verification from './pages/safety/Verification';
import AnomalyManagement from './pages/safety/AnomalyManagement';

function App() {
  const { isAuthenticated, hasRole, user } = useAuthStore();

  const getDefaultRoute = () => {
    if (!isAuthenticated) return '/login';
    switch (user?.role) {
      case 'BLASTER': return '/blaster';
      case 'STOREKEEPER': return '/storekeeper';
      case 'SAFETY_OFFICER': return '/safety';
      default: return '/login';
    }
  };

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={!isAuthenticated ? <Login /> : <Navigate to={getDefaultRoute()} />} />
        
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to={getDefaultRoute()} replace />} />
          
          <Route path="blaster">
            <Route index element={hasRole('BLASTER') ? <BlasterDashboard /> : <Navigate to="/login" />} />
            <Route path="create-shift" element={hasRole('BLASTER') ? <CreateShift /> : <Navigate to="/login" />} />
            <Route path="create-application" element={hasRole('BLASTER') ? <CreateApplication /> : <Navigate to="/login" />} />
          </Route>

          <Route path="storekeeper">
            <Route index element={hasRole('STOREKEEPER') ? <StorekeeperDashboard /> : <Navigate to="/login" />} />
            <Route path="outbound" element={hasRole('STOREKEEPER') ? <Outbound /> : <Navigate to="/login" />} />
            <Route path="inbound" element={hasRole('STOREKEEPER') ? <Inbound /> : <Navigate to="/login" />} />
          </Route>

          <Route path="safety">
            <Route index element={hasRole('SAFETY_OFFICER') ? <SafetyDashboard /> : <Navigate to="/login" />} />
            <Route path="review" element={hasRole(['SAFETY_OFFICER', 'STOREKEEPER']) ? <ReviewApplication /> : <Navigate to="/login" />} />
            <Route path="verification" element={hasRole('SAFETY_OFFICER') ? <Verification /> : <Navigate to="/login" />} />
            <Route path="anomalies" element={hasRole('SAFETY_OFFICER') ? <AnomalyManagement /> : <Navigate to="/login" />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to={getDefaultRoute()} replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
