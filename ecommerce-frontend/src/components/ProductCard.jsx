import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext.jsx';
import { money } from '../utils/format.js';
import ProductImage, { tint } from './ProductImage.jsx';

export default function ProductCard({ product }) {
  const { addToCart } = useCart();
  const out = product.stock <= 0;
  const low = !out && product.stock <= 5;

  return (
    <article className="card product-card">
      <Link to={`/products/${product.id}`} className="product-thumb" style={tint(product.categoryId)} aria-label={product.name}>
        <ProductImage product={product} />
        {out && <em className="tag tag-out">Sold out</em>}
        {low && <em className="tag tag-low">Only {product.stock} left</em>}
      </Link>
      <div className="product-body">
        <span className="muted small">{product.categoryName}</span>
        <Link to={`/products/${product.id}`} className="product-title"><h3>{product.name}</h3></Link>
        <p className="muted clamp">{product.description}</p>
        <div className="row between product-foot">
          <strong className="price">{money(product.price)}</strong>
          <button className="btn btn-primary btn-sm" disabled={out} onClick={() => addToCart(product.id, 1)}>Add</button>
        </div>
      </div>
    </article>
  );
}
