import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import InventoryList from './pages/InventoryList';
import ItemForm from './pages/ItemForm';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/inventory" element={<InventoryList />} />
        <Route path="/inventory/new" element={<ItemForm />} />
        <Route path="/inventory/:id/edit" element={<ItemForm />} />
      </Routes>
    </Layout>
  );
}
