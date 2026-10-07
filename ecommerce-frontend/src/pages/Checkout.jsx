import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { orderApi } from '../api/services.js';
import { getErrorMessage } from '../api/axios.js';
import { useCart } from '../context/CartContext.jsx';
import { money } from '../utils/format.js';
import Empty from '../components/Empty.jsx';

export default function Checkout() {
  const { cart, refresh } = useCart();
  const navigate = useNavigate();
  const [address, setAddress] = useState('');
  const [busy, setBusy] = useState(false);

  if (cart.items.length === 0 && !busy) return <Empty title="Nothing to check out"><Link to="/">Browse products</Link></Empty>;

  const placeOrder = async (e) => {
    e.preventDefault();
    setBusy(true);
    try {
      const order = await orderApi.create(address.trim());
      toast.success(`Order #${order.id} placed`);
      navigate(`/orders/${order.id}`, { replace: true });
      refresh();
    } catch (err) {
      toast.error(getErrorMessage(err));
      setBusy(false);
    }
  };

  return (
    <section>
      <div className="page-head"><h1>Checkout</h1></div>
      <div className="cart-layout">
        <form className="card stack pad" onSubmit={placeOrder}>
          <h3>Shipping address</h3>
          <textarea rows="4" value={address} onChange={(e) => setAddress(e.target.value)} required
            placeholder="Street, city, postal code, country" />
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Placing order…' : `Place order · ${money(cart.totalAmount)}`}</button>
        </form>
        <aside className="card summary">
          <h3>Items</h3>
          {cart.items.map((i) => (
            <div className="row between" key={i.productId}>
              <span>{i.name} × {i.quantity}</span><span>{money(i.subtotal)}</span>
            </div>
          ))}
          <div className="row between total"><span>Total</span><strong>{money(cart.totalAmount)}</strong></div>
        </aside>
      </div>
    </section>
  );
}
