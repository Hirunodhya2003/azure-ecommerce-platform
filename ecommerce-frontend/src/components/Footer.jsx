import { Link } from 'react-router-dom';
import Logo from './Logo.jsx';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div>
          <Logo size={32} />
          <p className="muted small footer-tag">Quality products, fair prices, delivered to your door.</p>
        </div>
        <div className="footer-links">
          <Link to="/">Shop</Link>
          <Link to="/orders">My orders</Link>
          <Link to="/cart">Cart</Link>
        </div>
        <p className="muted small">© {new Date().getFullYear()} shopHere.lk. All rights reserved.</p>
      </div>
    </footer>
  );
}
