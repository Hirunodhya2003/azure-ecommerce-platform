import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { orderApi } from '../api/services.js';
import { getErrorMessage } from '../api/axios.js';
import { money, dateTime } from '../utils/format.js';
import StatusBadge from '../components/StatusBadge.jsx';
import Spinner from '../components/Spinner.jsx';
import Empty from '../components/Empty.jsx';

export default function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    orderApi.get(id)
      .then(setOrder)
      .catch((e) => toast.error(getErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [id]);

  const cancel = async () => {
    if (!window.confirm('Cancel this order?')) return;
    setBusy(true);
    try {
      setOrder(await orderApi.cancel(id));
      toast.success('Order cancelled');
    } catch (e) {
      toast.error(getErrorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <Spinner />;
  if (!order) return <Empty title="Order not found"><Link to="/orders">Back to orders</Link></Empty>;

  const canCancel = ['PENDING', 'CONFIRMED', 'PROCESSING'].includes(order.status);

  return (
    <section>
      <Link to="/orders" className="muted">← Back to orders</Link>
      <div className="page-head">
        <h1>Order #{order.id}</h1>
        <StatusBadge status={order.status} />
      </div>

      <div className="cart-layout">
        <div className="card">
          <table className="table">
            <thead><tr><th>Product</th><th>Price</th><th>Qty</th><th>Subtotal</th></tr></thead>
            <tbody>
              {order.items.map((i) => (
                <tr key={i.productId}>
                  <td><Link to={`/products/${i.productId}`}>{i.name}</Link><div className="muted small">{i.sku}</div></td>
                  <td>{money(i.unitPrice)}</td><td>{i.quantity}</td><td>{money(i.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <aside className="card summary">
          <h3>Details</h3>
          <div><span className="muted small">Placed</span><div>{dateTime(order.createdAt)}</div></div>
          <div><span className="muted small">Last updated</span><div>{dateTime(order.updatedAt)}</div></div>
          <div><span className="muted small">Ship to</span><div style={{ whiteSpace: 'pre-line' }}>{order.shippingAddress}</div></div>
          <div className="row between total"><span>Total</span><strong>{money(order.totalAmount)}</strong></div>
          {canCancel && <button className="btn btn-danger btn-block" disabled={busy} onClick={cancel}>Cancel order</button>}
        </aside>
      </div>
    </section>
  );
}
