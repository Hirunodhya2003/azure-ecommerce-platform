import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext.jsx';
import { money } from '../utils/format.js';
import Spinner from '../components/Spinner.jsx';
import Empty from '../components/Empty.jsx';

export default function Cart() {
  const { cart, loading, updateItem, removeItem, clearCart } = useCart();
  const navigate = useNavigate();

  if (loading && cart.items.length === 0) return <Spinner />;
  if (cart.items.length === 0) return <Empty title="Your cart is empty"><Link to="/">Browse products</Link></Empty>;

  return (
    <section>
      <div className="page-head">
        <h1>Your cart</h1>
        <button className="btn btn-ghost" onClick={() => window.confirm('Remove all items?') && clearCart()}>Clear cart</button>
      </div>

      <div className="cart-layout">
        <div className="card">
          <table className="table">
            <thead><tr><th>Product</th><th>Price</th><th>Quantity</th><th>Subtotal</th><th /></tr></thead>
            <tbody>
              {cart.items.map((i) => (
                <tr key={i.productId}>
                  <td><Link to={`/products/${i.productId}`}>{i.name}</Link><div className="muted small">{i.sku}</div></td>
                  <td>{money(i.unitPrice)}</td>
                  <td>
                    <div className="row">
                      <button className="btn btn-ghost btn-sm" disabled={i.quantity <= 1} onClick={() => updateItem(i.productId, i.quantity - 1)}>−</button>
                      <span className="qty-text">{i.quantity}</span>
                      <button className="btn btn-ghost btn-sm" onClick={() => updateItem(i.productId, i.quantity + 1)}>+</button>
                    </div>
                  </td>
                  <td>{money(i.subtotal)}</td>
                  <td><button className="btn btn-danger btn-sm" onClick={() => removeItem(i.productId)}>Remove</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <aside className="card summary">
          <h3>Order summary</h3>
          <div className="row between"><span className="muted">Items</span><span>{cart.items.reduce((s, i) => s + i.quantity, 0)}</span></div>
          <div className="row between total"><span>Total</span><strong>{money(cart.totalAmount)}</strong></div>
          <button className="btn btn-primary btn-block" onClick={() => navigate('/checkout')}>Go to checkout</button>
        </aside>
      </div>
    </section>
  );
}
