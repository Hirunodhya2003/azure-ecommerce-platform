import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { orderApi, userApi, productApi, categoryApi } from '../../api/services.js';
import { getErrorMessage } from '../../api/axios.js';
import { ORDER_STATUSES, money, dateTime } from '../../utils/format.js';
import StatusBadge from '../../components/StatusBadge.jsx';
import Spinner from '../../components/Spinner.jsx';

export default function Dashboard() {
  const [s, setS] = useState(null);

  useEffect(() => {
    Promise.all([orderApi.adminList(), userApi.list(), productApi.list({ size: 1 }), categoryApi.list()])
      .then(([orders, users, products, categories]) => {
        const counts = Object.fromEntries(ORDER_STATUSES.map((st) => [st, 0]));
        orders.forEach((o) => { counts[o.status] = (counts[o.status] || 0) + 1; });
        const revenue = orders.filter((o) => o.status !== 'CANCELLED').reduce((sum, o) => sum + Number(o.totalAmount), 0);
        setS({ orders, counts, revenue, users: users.length, products: products.totalElements ?? 0, categories: categories.length });
      })
      .catch((e) => toast.error(getErrorMessage(e)));
  }, []);

  if (!s) return <Spinner />;

  const stats = [
    { label: 'Revenue (excl. cancelled)', value: money(s.revenue) },
    { label: 'Orders', value: s.orders.length },
    { label: 'Active products', value: s.products },
    { label: 'Categories', value: s.categories },
    { label: 'Users', value: s.users },
  ];

  return (
    <section>
      <div className="page-head"><h1>Dashboard</h1></div>

      <div className="stats">
        {stats.map((x) => (
          <div className="card stat" key={x.label}>
            <span className="muted small">{x.label}</span>
            <strong>{x.value}</strong>
          </div>
        ))}
      </div>

      <h3 className="mt">Orders by status</h3>
      <div className="row wrap">
        {ORDER_STATUSES.map((st) => (
          <div className="card stat compact" key={st}><StatusBadge status={st} /><strong>{s.counts[st]}</strong></div>
        ))}
      </div>

      <div className="page-head mt">
        <h3>Recent orders</h3>
        <Link to="/admin/orders" className="btn btn-ghost btn-sm">View all</Link>
      </div>
      <div className="card">
        <table className="table">
          <thead><tr><th>Order</th><th>User</th><th>Placed</th><th>Status</th><th>Total</th></tr></thead>
          <tbody>
            {s.orders.slice(0, 5).map((o) => (
              <tr key={o.id}><td>#{o.id}</td><td>{o.userId}</td><td>{dateTime(o.createdAt)}</td><td><StatusBadge status={o.status} /></td><td>{money(o.totalAmount)}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
