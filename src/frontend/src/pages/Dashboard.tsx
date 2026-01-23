import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Package, TrendingUp, AlertTriangle, XCircle } from 'lucide-react';
import { dashboardApi } from '../services/api';
import { DashboardStats } from '../types/item';

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const data = await dashboardApi.getStats();
      setStats(data);
    } catch (error) {
      console.error('Failed to load stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">Loading...</div>;
  }

  const statusCards = [
    { label: 'Total Items', value: stats?.totalItems || 0, icon: Package, color: 'bg-blue-500' },
    { label: 'In Stock', value: stats?.countByStatus?.['In Stock'] || 0, icon: TrendingUp, color: 'bg-green-500' },
    { label: 'Low Stock', value: stats?.countByStatus?.['Low Stock'] || 0, icon: AlertTriangle, color: 'bg-yellow-500' },
    { label: 'Out of Stock', value: stats?.countByStatus?.['Out of Stock'] || 0, icon: XCircle, color: 'bg-red-500' },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600">Overview of your inventory</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statusCards.map((card) => (
          <div key={card.label} className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className={`${card.color} p-3 rounded-lg`}>
                <card.icon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">{card.label}</p>
                <p className="text-2xl font-semibold text-gray-900">{card.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {stats && Object.keys(stats.countByCategory).length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">Items by Category</h2>
          <div className="space-y-3">
            {Object.entries(stats.countByCategory).map(([category, count]) => (
              <div key={category} className="flex items-center justify-between">
                <span className="text-gray-700">{category}</span>
                <span className="bg-gray-100 px-3 py-1 rounded-full text-sm font-medium">
                  {count}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="mt-8">
        <Link
          to="/inventory"
          className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          View All Items
        </Link>
      </div>
    </div>
  );
}
