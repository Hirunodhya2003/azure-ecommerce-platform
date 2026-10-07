import { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { categoryApi } from '../../api/services.js';
import { getErrorMessage } from '../../api/axios.js';
import Modal from '../../components/Modal.jsx';
import Spinner from '../../components/Spinner.jsx';
import Empty from '../../components/Empty.jsx';

export default function AdminCategories() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try { setList(await categoryApi.list()); }
    catch (e) { toast.error(getErrorMessage(e)); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const save = async (e) => {
    e.preventDefault();
    const payload = { name: editing.name.trim(), description: editing.description };
    setBusy(true);
    try {
      if (editing.id) await categoryApi.update(editing.id, payload); else await categoryApi.create(payload);
      toast.success(editing.id ? 'Category updated' : 'Category created');
      setEditing(null);
      load();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const remove = async (c) => {
    if (!window.confirm(`Delete category "${c.name}"?`)) return;
    try { await categoryApi.remove(c.id); toast.success('Category deleted'); load(); }
    catch (e) { toast.error(getErrorMessage(e)); }
  };

  return (
    <section>
      <div className="page-head">
        <h1>Categories</h1>
        <button className="btn btn-primary" onClick={() => setEditing({ name: '', description: '' })}>Add category</button>
      </div>

      {loading ? <Spinner /> : list.length === 0 ? <Empty title="No categories yet">Add one before creating products.</Empty> : (
        <div className="card">
          <table className="table">
            <thead><tr><th>ID</th><th>Name</th><th>Description</th><th /></tr></thead>
            <tbody>
              {list.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td><td>{c.name}</td><td className="muted">{c.description}</td>
                  <td>
                    <div className="row">
                      <button className="btn btn-ghost btn-sm" onClick={() => setEditing(c)}>Edit</button>
                      <button className="btn btn-danger btn-sm" onClick={() => remove(c)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {editing && (
        <Modal title={editing.id ? 'Edit category' : 'Add category'} onClose={() => setEditing(null)}>
          <form className="stack" onSubmit={save}>
            <label>Name<input value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} required /></label>
            <label>Description<textarea rows="3" value={editing.description ?? ''} onChange={(e) => setEditing({ ...editing, description: e.target.value })} /></label>
            <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save category'}</button>
          </form>
        </Modal>
      )}
    </section>
  );
}
