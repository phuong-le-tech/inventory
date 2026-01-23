import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Pencil, Trash2, Search } from 'lucide-react';
import { itemsApi } from '../services/api';
import { Item, STATUS_OPTIONS } from '../types/item';

export default function InventoryList() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    loadItems();
  }, []);

  const loadItems = async () => {
    try {
      const data = await itemsApi.getAll();
      setItems(data);
    } catch (error) {
      console.error('Failed to load items:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this item?')) return;
    try {
      await itemsApi.delete(id);
      setItems(items.filter((item) => item.id !== id));
    } catch (error) {
      console.error('Failed to delete item:', error);
    }
  };

  const filteredItems = items.filter((item) => {
    const matchesSearch = item.name.toLowerCase().includes(search.toLowerCase()) ||
      item.category?.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = !statusFilter || item.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'In Stock':
        return 'bg-green-100 text-green-800';
      case 'Low Stock':
        return 'bg-yellow-100 text-yellow-800';
      case 'Out of Stock':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">Loading...</div>;
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Inventory</h1>
          <p className="text-gray-600">{items.length} items total</p>
        </div>
        <Link
          to="/inventory/new"
          className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          <Plus className="h-5 w-5 mr-2" />
          Add Item
        </Link>
      </div>

      <div className="bg-white rounded-lg shadow mb-6 p-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search items..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">All Status</option>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>{status}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredItems.map((item) => (
          <div key={item.id} className="bg-white rounded-lg shadow overflow-hidden">
            <div className="h-48 bg-gray-200 flex items-center justify-center">
              {item.imageBase64 ? (
                <img
                  src={`data:${item.contentType};base64,${item.imageBase64}`}
                  alt={item.name}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="text-gray-400">No image</div>
              )}
            </div>
            <div className="p-4">
              <div className="flex justify-between items-start mb-2">
                <h3 className="font-semibold text-lg">{item.name}</h3>
                <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusBadgeClass(item.status)}`}>
                  {item.status}
                </span>
              </div>
              <p className="text-gray-600 text-sm mb-4">{item.category || 'Uncategorized'}</p>
              <div className="flex gap-2">
                <Link
                  to={`/inventory/${item.id}/edit`}
                  className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  <Pencil className="h-4 w-4 mr-1" />
                  Edit
                </Link>
                <button
                  onClick={() => handleDelete(item.id)}
                  className="inline-flex items-center justify-center px-3 py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-50"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {filteredItems.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          No items found. {items.length === 0 && <Link to="/inventory/new" className="text-blue-600 hover:underline">Add your first item</Link>}
        </div>
      )}
    </div>
  );
}
