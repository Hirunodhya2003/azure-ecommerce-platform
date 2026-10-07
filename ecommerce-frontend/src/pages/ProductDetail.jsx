import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { productApi } from '../api/services.js';
import { getErrorMessage } from '../api/axios.js';
import { useCart } from '../context/CartContext.jsx';
import ProductImage, { tint } from '../components/ProductImage.jsx';
import { money } from '../utils/format.js';
import Spinner from '../components/Spinner.jsx';
import Empty from '../components/Empty.jsx';

export default function ProductDetail() {
  const { id } = useParams();
  const { addToCart } = useCart();
  const [product, setProduct] = useState(null);
  const [qty, setQty] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    productApi.get(id)
      .then(setProduct)
      .catch((e) => { setProduct(null); toast.error(getErrorMessage(e)); })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Spinner />;
  if (!product) return <Empty title="Product not found"><Link to="/">Back to products</Link></Empty>;

  const out = product.stock <= 0 || !product.active;

  return (
    <section className="detail">
      <Link to="/" className="muted">← Back to products</Link>
      <div className="detail-grid">
        <div className="card product-thumb big" style={tint(product.categoryId)}><ProductImage product={product} /></div>
        <div className="stack">
          <span className="muted">{product.categoryName}</span>
          <h1>{product.name}</h1>
          <p className="price big-price">{money(product.price)}</p>
          <p>{product.description}</p>
          <p className="muted small">SKU: {product.sku}</p>
          <p className={out ? 'text-danger' : 'muted'}>{out ? 'Currently unavailable' : `${product.stock} in stock`}</p>
          {!out && (
            <div className="row">
              <input type="number" min="1" max={product.stock} value={qty} className="qty"
                onChange={(e) => setQty(Math.max(1, Math.min(product.stock, Number(e.target.value) || 1)))} />
              <button className="btn btn-primary" onClick={() => addToCart(product.id, qty)}>Add to cart</button>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
