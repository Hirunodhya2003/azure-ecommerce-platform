import { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { productApi, categoryApi } from '../../api/services.js';
import { getErrorMessage } from '../../api/axios.js';
import { money } from '../../utils/format.js';
import Modal from '../../components/Modal.jsx';
import Pagination from '../../components/Pagination.jsx';
import Spinner from '../../components/Spinner.jsx';

const BLANK = { name: '', sku: '', description: '', imageUrl: '', price: '', stock: '', categoryId: '', active: true };

export default function AdminProducts() {
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [categories, setCategories] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setData(await productApi.list({ page, size: 10, sortBy: 'id', direction: 'desc' }));
    } catch (e) {
      toast.error(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { categoryApi.list().then(setCategories).catch(() => {}); }, []);

  const set = (e) => {
    const { name, value, type, checked } = e.target;
    setEditing((f) => ({ ...f, [name]: type === 'checkbox' ? checked : value }));
  };

  const save = async (e) => {
    e.preventDefault();
    const { id, ...f } = editing;
    const payload = {
      name: f.name.trim(), sku: f.sku.trim(), description: f.description, imageUrl: f.imageUrl?.trim() || null,
      price: Number(f.price), stock: Number(f.stock), categoryId: Number(f.categoryId), active: f.active,
    };
    setBusy(true);
    try {
      if (id) await productApi.update(id, payload); else await productApi.create(payload);
      toast.success(id ? 'Product updated' : 'Product created');
      setEditing(null);
      load();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const remove = async (p) => {
    if (!window.confirm(`Delete "${p.name}"?`)) return;
    try {
      await productApi.remove(p.id);
      toast.success('Product deleted');
      load();
    } catch (e) {
      toast.error(getErrorMessage(e));
    }
  };

  return (
    <section>
      <div className="page-head">
        <h1>Manage products</h1>
        <button className="btn btn-primary" onClick={() => setEditing({ ...BLANK, categoryId: categories[0]?.id ?? '' })}>Add product</button>
      </div>

      {loading ? <Spinner /> : (
        <div className="card">
          <table className="table">
            <thead><tr><th>ID</th><th>Name</th><th>SKU</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th /></tr></thead>
            <tbody>
              {data.content.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td><td><div className="row">{p.imageUrl && <img className="mini-thumb" src={p.imageUrl} alt="" />}{p.name}</div></td><td>{p.sku}</td><td>{p.categoryName}</td>
                  <td>{money(p.price)}</td><td>{p.stock}</td>
                  <td>{p.active ? 'Active' : 'Inactive'}</td>
                  <td>
                    <div className="row">
                      <button className="btn btn-ghost btn-sm" onClick={() => setEditing({ ...p })}>Edit</button>
                      <button className="btn btn-danger btn-sm" onClick={() => remove(p)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />

      {editing && (
        <Modal title={editing.id ? 'Edit product' : 'Add product'} onClose={() => setEditing(null)}>
          <form className="stack" onSubmit={save}>
            <label>Name<input name="name" value={editing.name} onChange={set} required /></label>
            <label>SKU<input name="sku" value={editing.sku} onChange={set} required /></label>
            <label>Description<textarea name="description" rows="3" value={editing.description ?? ''} onChange={set} /></label>
            <label>Image URL
              <input type="url" name="imageUrl" placeholder="https://..." value={editing.imageUrl ?? ''} onChange={set} />
            </label>
            {editing.imageUrl && <img className="img-preview" src={editing.imageUrl} alt="Preview" onError={(e) => (e.currentTarget.style.display = 'none')} onLoad={(e) => (e.currentTarget.style.display = 'block')} />}
            <div className="row">
              <label className="grow">Price<input type="number" step="0.01" min="0" name="price" value={editing.price} onChange={set} required /></label>
              <label className="grow">Stock<input type="number" min="0" name="stock" value={editing.stock} onChange={set} required /></label>
            </div>
            <label>Category
              <select name="categoryId" value={editing.categoryId} onChange={set} required>
                <option value="" disabled>Select a category</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </label>
            <label className="check"><input type="checkbox" name="active" checked={!!editing.active} onChange={set} /> Visible in the shop</label>
            <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save product'}</button>
          </form>
        </Modal>
      )}
    </section>
  );
}
