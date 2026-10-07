import { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { orderApi } from '../../api/services.js';
import { getErrorMessage } from '../../api/axios.js';
import { ORDER_STATUSES, NEXT_STATUSES, money, dateTime } from '../../utils/format.js';
import StatusBadge from '../../components/StatusBadge.jsx';
import Modal from '../../components/Modal.jsx';
import Spinner from '../../components/Spinner.jsx';
import Empty from '../../components/Empty.jsx';

export default function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try { setOrders(await orderApi.adminList(status || undefined)); }
    catch (e) { toast.error(getErrorMessage(e)); }
    finally { setLoading(false); }
  }, [status]);

  useEffect(() => { load(); }, [load]);

  const changeStatus = async (order, next) => {
    try {
      const updated = await orderApi.adminUpdateStatus(order.id, next);
      toast.success(`Order #${order.id} is now ${next}`);
      setOrders((list) => list.map((o) => (o.id === updated.id ? updated : o)));
    } catch (e) {
      toast.error(getErrorMessage(e));
    }
  };

  return (
    <section>
      <div className="page-head">
        <h1>All orders</h1>
        <select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Filter by status">
          <option value="">All statuses</option>
          {ORDER_STATUSES.map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>

      {loading ? <Spinner /> : orders.length === 0 ? <Empty title="No orders found" /> : (
        <div className="card">
          <table className="table">
            <thead><tr><th>Order</th><th>User</th><th>Placed</th><th>Total</th><th>Status</th><th>Move to</th><th /></tr></thead>
            <tbody>
              {orders.map((o) => {
                const next = NEXT_STATUSES[o.status] || [];
                return (
                  <tr key={o.id}>
                    <td>#{o.id}</td><td>{o.userId}</td><td>{dateTime(o.createdAt)}</td><td>{money(o.totalAmount)}</td>
                    <td><StatusBadge status={o.status} /></td>
                    <td>
                      {next.length === 0 ? <span className="muted">Final</span> : (
                        <select value="" onChange={(e) => e.target.value && changeStatus(o, e.target.value)} aria-label={`Change status of order ${o.id}`}>
                          <option value="">Select…</option>
                          {next.map((s) => <option key={s}>{s}</option>)}
                        </select>
                      )}
                    </td>
                    <td><button className="btn btn-ghost btn-sm" onClick={() => setSelected(o)}>Details</button></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {selected && (
        <Modal title={`Order #${selected.id}`} onClose={() => setSelected(null)}>
          <div className="stack">
            <div><span className="muted small">Ship to</span><div style={{ whiteSpace: 'pre-line' }}>{selected.shippingAddress}</div></div>
            <table className="table">
              <thead><tr><th>Product</th><th>Qty</th><th>Subtotal</th></tr></thead>
              <tbody>
                {selected.items.map((i) => <tr key={i.productId}><td>{i.name}</td><td>{i.quantity}</td><td>{money(i.subtotal)}</td></tr>)}
              </tbody>
            </table>
            <div className="row between total"><span>Total</span><strong>{money(selected.totalAmount)}</strong></div>
          </div>
        </Modal>
      )}
    </section>
  );
}
