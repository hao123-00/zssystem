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
import ProcessFileList from './pages/Production/ProcessFile';
import ProcessFileUpload from './pages/Production/ProcessFile/ProcessFileUpload';
import ProcessFileForm from './pages/Production/ProcessFile/ProcessFileForm';
import ProcessFileDetail from './pages/Production/ProcessFile/ProcessFileDetail';
import PendingApprovalList from './pages/Production/ProcessFile/PendingApprovalList';
import EquipmentManagement from './pages/Equipment/EquipmentManagement';
import Site5sManagement from './pages/Site5s/Site5sManagement';

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
        <Route path="production/process-file" element={<ProcessFileList />} />
        <Route path="production/process-file/upload" element={<ProcessFileUpload />} />
        <Route path="production/process-file/upload/:id" element={<ProcessFileUpload />} />
        <Route path="production/process-file/form" element={<ProcessFileForm />} />
        <Route path="production/process-file/form/:id" element={<ProcessFileForm />} />
        <Route path="production/process-file/detail/:id" element={<ProcessFileDetail />} />
        <Route path="production/process-file/pending-approval" element={<PendingApprovalList />} />
        <Route path="equipment" element={<EquipmentManagement />} />
        <Route path="site5s" element={<Site5sManagement />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
