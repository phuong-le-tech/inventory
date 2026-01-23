import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Package } from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const location = useLocation();

  const navItems = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/inventory', label: 'Inventory', icon: Package },
  ];

  return (
    <div className="min-h-screen bg-gray-50 flex">
      <aside className="w-64 bg-white border-r border-gray-200 fixed h-full">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center">
            <Package className="h-8 w-8 text-blue-600" />
            <span className="ml-2 text-xl font-bold text-gray-900">Inventory</span>
          </div>
        </div>
        <nav className="p-4 space-y-1">
          {navItems.map((item) => {
            const isActive = location.pathname === item.to ||
              (item.to !== '/' && location.pathname.startsWith(item.to));
            return (
              <Link
                key={item.to}
                to={item.to}
                className={`flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors ${
                  isActive
                    ? 'text-blue-600 bg-blue-50'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                }`}
              >
                <item.icon className="h-5 w-5 mr-3" />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </aside>

      <main className="flex-1 ml-64 p-8">
        {children}
      </main>
    </div>
  );
}
