import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login/Login';
import Layout from './layout/Layout';
import PrivateRoute from './routes/PrivateRoute';
import Dashboard from './pages/Dashboard/Dashboard';
import UserList from './pages/User/UserList';
import RoleList from './pages/Role/RoleList';
import PermissionList from './pages/Permission/PermissionList';
import EmployeeList from './pages/Employee/EmployeeList';
import DepartmentList from './pages/Department/DepartmentList';
import ProductionManagement from './pages/Production/ProductionManagement';

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="user" element={<UserList />} />
        <Route path="role" element={<RoleList />} />
        <Route path="permission" element={<PermissionList />} />
        <Route path="employee" element={<EmployeeList />} />
        <Route path="department" element={<DepartmentList />} />
        <Route path="production" element={<ProductionManagement />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
