import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { productApi, categoryApi } from '../api/services.js';
import { getErrorMessage } from '../api/axios.js';
import ProductCard from '../components/ProductCard.jsx';
import Pagination from '../components/Pagination.jsx';
import Spinner from '../components/Spinner.jsx';
import Empty from '../components/Empty.jsx';

const SORTS = [
  { label: 'Newest', sortBy: 'createdAt', direction: 'desc' },
  { label: 'Price: low to high', sortBy: 'price', direction: 'asc' },
  { label: 'Price: high to low', sortBy: 'price', direction: 'desc' },
  { label: 'Name: A to Z', sortBy: 'name', direction: 'asc' },
];

export default function Products() {
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [categories, setCategories] = useState([]);
  const [page, setPage] = useState(0);
  const [sortIdx, setSortIdx] = useState(0);
  const [categoryId, setCategoryId] = useState('');
  const [input, setInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    categoryApi.list().then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      try {
        if (keyword) {
          const list = await productApi.search(keyword);
          if (!cancelled) setData({ content: list.filter((p) => p.active), totalPages: 1 });
        } else {
          const { sortBy, direction } = SORTS[sortIdx];
          const res = await productApi.list({ page, size: 12, sortBy, direction });
          if (!cancelled) setData(res);
        }
      } catch (e) {
        toast.error(getErrorMessage(e));
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [page, sortIdx, keyword]);

  const submitSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setKeyword(input.trim());
  };

  const clearSearch = () => { setInput(''); setKeyword(''); setPage(0); };

  const visible = categoryId ? data.content.filter((p) => String(p.categoryId) === categoryId) : data.content;

  return (
    <section>
      <div className="hero">
        <div className="hero-copy">
          <span className="hero-pill">New arrivals every week</span>
          <h1>Find something you will love.</h1>
          <p>Browse our latest products, add them to your cart and get them delivered to your door.</p>
        </div>
        <div className="hero-art" aria-hidden="true"><i /><i /><i /></div>
      </div>

      <div className="toolbar">
        <form onSubmit={submitSearch} className="row grow">
          <input placeholder="Search by name or description" value={input} onChange={(e) => setInput(e.target.value)} />
          <button className="btn btn-primary">Search</button>
          {keyword && <button type="button" className="btn btn-ghost" onClick={clearSearch}>Clear</button>}
        </form>
        <select value={sortIdx} disabled={!!keyword} onChange={(e) => { setSortIdx(Number(e.target.value)); setPage(0); }} aria-label="Sort products">
          {SORTS.map((s, i) => <option key={s.label} value={i}>{s.label}</option>)}
        </select>
      </div>

      <div className="chips" role="tablist" aria-label="Categories">
        <button className={`chip ${categoryId === '' ? 'on' : ''}`} onClick={() => setCategoryId('')}>All</button>
        {categories.map((c) => (
          <button key={c.id} className={`chip ${categoryId === String(c.id) ? 'on' : ''}`} onClick={() => setCategoryId(String(c.id))}>{c.name}</button>
        ))}
      </div>

      {loading ? (
        <Spinner />
      ) : visible.length === 0 ? (
        <Empty title="No products found">Try a different search or category.</Empty>
      ) : (
        <div className="grid">{visible.map((p) => <ProductCard key={p.id} product={p} />)}</div>
      )}

      {!keyword && <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />}
    </section>
  );
}
