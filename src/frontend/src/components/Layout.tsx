import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, List, Package, Menu, X } from 'lucide-react';
import { UserMenu } from './UserMenu';

interface LayoutProps {
  children: React.ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Close sidebar on route change (mobile navigation)
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  // Close sidebar on Escape key
  useEffect(() => {
    if (!sidebarOpen) return;
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') setSidebarOpen(false);
    }
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [sidebarOpen]);

  const navItems = [
    { to: '/', label: 'Tableau de bord', icon: LayoutDashboard },
    { to: '/lists', label: 'Mes Listes', icon: List },
  ];

  return (
    <div className="min-h-screen flex flex-col md:flex-row">
      {/* Mobile header */}
      <div className="md:hidden sticky top-0 z-40 flex items-center gap-3 px-4 py-3 bg-surface-base/95 backdrop-blur-sm border-b border-white/[0.06]">
        <button
          onClick={() => setSidebarOpen(true)}
          aria-label="Ouvrir le menu"
          className="p-2 -ml-2 text-stone-400 hover:text-stone-200 transition-colors"
        >
          <Menu className="h-5 w-5" />
        </button>
        <Package className="h-5 w-5 text-amber-500" />
        <span className="font-display text-lg text-stone-100">Inventory</span>
      </div>

      {/* Backdrop overlay (mobile) */}
      {sidebarOpen && (
        <div
          className="md:hidden fixed inset-0 z-40 bg-black/60 backdrop-blur-sm"
          onClick={() => setSidebarOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={`w-64 bg-gradient-to-b from-surface-elevated to-surface-base border-r border-white/[0.06] fixed h-full flex flex-col z-50 transition-transform duration-300 ease-in-out ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0 md:static md:z-auto`}
      >
        <div className="p-6 border-b border-white/[0.06]">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <div className="w-10 h-10 bg-gradient-to-br from-amber-500 to-amber-600 rounded-xl flex items-center justify-center shadow-glow-amber">
                <Package className="h-5 w-5 text-surface-base" />
              </div>
              <div className="ml-3">
                <span className="font-display text-xl text-stone-100">Inventory</span>
              </div>
            </div>
            <button
              onClick={() => setSidebarOpen(false)}
              aria-label="Fermer le menu"
              className="md:hidden p-1 text-stone-400 hover:text-stone-200 transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        <div className="h-px bg-gradient-to-r from-transparent via-white/[0.06] to-transparent mx-4" />

        <nav aria-label="Navigation principale" className="p-4 space-y-1 flex-1">
          {navItems.map((item) => {
            const isActive = location.pathname === item.to ||
              (item.to !== '/' && location.pathname.startsWith(item.to));
            return (
              <Link
                key={item.to}
                to={item.to}
                className={`group flex items-center px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 ${
                  isActive
                    ? 'text-amber-400 bg-amber-500/10 border border-amber-500/20'
                    : 'text-stone-400 hover:text-stone-200 hover:bg-white/[0.04] border border-transparent'
                }`}
              >
                <item.icon className={`h-5 w-5 mr-3 transition-transform duration-200 ${!isActive ? 'group-hover:scale-110' : ''}`} />
                {item.label}
                {isActive && (
                  <div className="ml-auto w-1.5 h-1.5 rounded-full bg-amber-400 shadow-glow-amber animate-glow-pulse" />
                )}
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-white/[0.06]">
          <UserMenu />
        </div>
      </aside>

      <main className="flex-1 md:ml-64 p-4 md:p-8">
        {children}
      </main>
    </div>
  );
}
