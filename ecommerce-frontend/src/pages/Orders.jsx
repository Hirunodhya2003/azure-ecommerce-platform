import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { orderApi } from '../api/services.js';
import { getErrorMessage } from '../api/axios.js';
import { ORDER_STATUSES, money, dateTime } from '../utils/format.js';
import StatusBadge from '../components/StatusBadge.jsx';
import Spinner from '../components/Spinner.jsx';
import Empty from '../components/Empty.jsx';

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    orderApi.mine(status || undefined)
      .then(setOrders)
      .catch((e) => toast.error(getErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [status]);

  return (
    <section>
      <div className="page-head">
        <h1>My orders</h1>
        <select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Filter by status">
          <option value="">All statuses</option>
          {ORDER_STATUSES.map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>

      {loading ? <Spinner /> : orders.length === 0 ? (
        <Empty title="No orders yet"><Link to="/">Start shopping</Link></Empty>
      ) : (
        <div className="card">
          <table className="table">
            <thead><tr><th>Order</th><th>Placed</th><th>Status</th><th>Total</th><th /></tr></thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td>#{o.id}</td>
                  <td>{dateTime(o.createdAt)}</td>
                  <td><StatusBadge status={o.status} /></td>
                  <td>{money(o.totalAmount)}</td>
                  <td><Link className="btn btn-ghost btn-sm" to={`/orders/${o.id}`}>View</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
