import { Routes, Route } from 'react-router-dom';
import { useEffect } from 'react';
import { useAppDispatch } from '@/store';
import { restoreSession } from '@/store/authSlice';
import Navbar from '@/components/common/Navbar';
import ProtectedRoute from '@/components/common/ProtectedRoute';
import LandingPage from '@/pages/LandingPage';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import DashboardPage from '@/pages/DashboardPage';
import FoodListingsPage from '@/pages/FoodListingsPage';
import CreateFoodListingPage from '@/pages/CreateFoodListingPage';
import FoodDetailPage from '@/pages/FoodDetailPage';
import MapPage from '@/pages/MapPage';
import PickupsPage from '@/pages/PickupsPage';
import AnalyticsPage from '@/pages/AnalyticsPage';
import ProfilePage from '@/pages/ProfilePage';
import AdminPage from '@/pages/AdminPage';

function App() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(restoreSession());
  }, [dispatch]);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route path="/food" element={<FoodListingsPage />} />
          <Route
            path="/food/new"
            element={
              <ProtectedRoute allowedRoles={['RESTAURANT']}>
                <CreateFoodListingPage />
              </ProtectedRoute>
            }
          />
          <Route path="/food/:id" element={<FoodDetailPage />} />
          <Route path="/map" element={<MapPage />} />
          <Route
            path="/pickups"
            element={
              <ProtectedRoute>
                <PickupsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/analytics"
            element={
              <ProtectedRoute>
                <AnalyticsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </div>
  );
}

export default App;
