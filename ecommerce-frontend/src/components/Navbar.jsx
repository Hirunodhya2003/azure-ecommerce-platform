import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import Logo from './Logo.jsx';

const CartIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M6 7h12l-1 12a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2z" /><path d="M9 7V6a3 3 0 0 1 6 0v1" />
  </svg>
);

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { count } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Logo />
        <nav className="nav-links" aria-label="Main">
          {isAuthenticated && (
            <>
              <NavLink to="/" end>Shop</NavLink>
              <NavLink to="/orders">My orders</NavLink>
            </>
          )}
        </nav>
        <div className="nav-user">
          {isAuthenticated ? (
            <>
              <NavLink to="/cart" className="cart-btn" aria-label={`Cart, ${count} items`}>
                <CartIcon />{count > 0 && <span className="badge">{count}</span>}
              </NavLink>
              <span className="avatar" title={user?.email}>{user?.name?.charAt(0).toUpperCase()}</span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>Log out</button>
            </>
          ) : (
            <>
              <Link className="btn btn-ghost btn-sm" to="/login">Log in</Link>
              <Link className="btn btn-primary btn-sm" to="/register">Sign up</Link>
            </>
          )}
        </div>
      </div>

      {isAuthenticated && isAdmin && (
        <div className="adminbar">
          <div className="container adminbar-inner">
            <span className="adminbar-label">Admin</span>
            <NavLink to="/admin" end>Dashboard</NavLink>
            <NavLink to="/admin/products">Products</NavLink>
            <NavLink to="/admin/categories">Categories</NavLink>
            <NavLink to="/admin/orders">Orders</NavLink>
            <NavLink to="/admin/users">Users</NavLink>
          </div>
        </div>
      )}
    </header>
  );
}
