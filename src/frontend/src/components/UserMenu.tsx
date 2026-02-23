import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Settings, LogOut, ChevronUp } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

export function UserMenu() {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!isOpen) return;
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    }
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <div ref={menuRef} className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        aria-haspopup="true"
        aria-expanded={isOpen}
        className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-left transition-colors hover:bg-white/[0.04] border border-transparent hover:border-white/[0.06]"
      >
        <div className="w-9 h-9 rounded-full bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center text-white text-sm font-medium shadow-inner-light">
          {user.pictureUrl ? (
            <img src={user.pictureUrl} alt="" className="w-full h-full rounded-full object-cover" />
          ) : (
            user.email[0].toUpperCase()
          )}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-stone-200 truncate">{user.email}</p>
          <p className="text-xs text-stone-500 capitalize">{user.role.toLowerCase()}</p>
        </div>
        <ChevronUp className={`w-4 h-4 text-stone-500 transition-transform ${isOpen ? '' : 'rotate-180'}`} />
      </button>

      {isOpen && (
        <div role="menu" className="absolute bottom-full left-0 right-0 mb-2 bg-surface-elevated rounded-xl border border-white/[0.06] shadow-premium-hover overflow-hidden animate-fade-in">
          {isAdmin && (
            <Link
              to="/admin/users"
              role="menuitem"
              onClick={() => setIsOpen(false)}
              className="flex items-center gap-3 px-4 py-3 text-sm text-stone-300 hover:text-stone-100 hover:bg-white/[0.04] transition-colors"
            >
              <Settings className="w-4 h-4" />
              Manage Users
            </Link>
          )}
          <button
            role="menuitem"
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 text-sm text-stone-300 hover:text-red-400 hover:bg-red-500/10 transition-colors"
          >
            <LogOut className="w-4 h-4" />
            Sign out
          </button>
        </div>
      )}
    </div>
  );
}
