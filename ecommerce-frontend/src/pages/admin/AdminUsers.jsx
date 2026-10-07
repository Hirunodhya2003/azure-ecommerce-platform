import { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { userApi } from '../../api/services.js';
import { getErrorMessage } from '../../api/axios.js';
import { useAuth } from '../../context/AuthContext.jsx';
import Spinner from '../../components/Spinner.jsx';

export default function AdminUsers() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    try { setUsers(await userApi.list()); }
    catch (e) { toast.error(getErrorMessage(e)); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const replace = (u) => setUsers((list) => list.map((x) => (x.id === u.id ? u : x)));

  const toggleActive = async (u) => {
    try { replace(await userApi.setStatus(u.id, !u.active)); toast.success(u.active ? 'User deactivated' : 'User activated'); }
    catch (e) { toast.error(getErrorMessage(e)); }
  };

  const changeRole = async (u, role) => {
    try { replace(await userApi.setRole(u.id, role)); toast.success('Role updated'); }
    catch (e) { toast.error(getErrorMessage(e)); }
  };

  if (loading) return <Spinner />;

  return (
    <section>
      <div className="page-head"><h1>Users</h1></div>
      <div className="card">
        <table className="table">
          <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th /></tr></thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td><td>{u.name}</td><td>{u.email}</td>
                <td>
                  <select value={u.role} disabled={u.id === me?.id} onChange={(e) => changeRole(u, e.target.value)} aria-label={`Role of ${u.name}`}>
                    <option>CUSTOMER</option><option>ADMIN</option>
                  </select>
                </td>
                <td>{u.active ? 'Active' : 'Inactive'}</td>
                <td>
                  <button className={`btn btn-sm ${u.active ? 'btn-danger' : 'btn-ghost'}`} disabled={u.id === me?.id} onClick={() => toggleActive(u)}>
                    {u.active ? 'Deactivate' : 'Activate'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
