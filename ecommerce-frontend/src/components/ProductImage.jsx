import { useEffect, useState } from 'react';

// Soft pastel per category, used as background / fallback when there is no image
export const tint = (seed = 0) => {
  const h = (Number(seed) * 47) % 360;
  return { background: `linear-gradient(145deg, hsl(${h} 80% 94%), hsl(${(h + 35) % 360} 75% 86%))`, color: `hsl(${h} 45% 30%)` };
};

// Shows product.imageUrl; falls back to the first letter if missing or broken
export default function ProductImage({ product }) {
  const [failed, setFailed] = useState(false);
  useEffect(() => setFailed(false), [product.imageUrl]);

  if (product.imageUrl && !failed) {
    return <img src={product.imageUrl} alt={product.name} loading="lazy" onError={() => setFailed(true)} />;
  }
  return <span>{product.name?.charAt(0).toUpperCase()}</span>;
}
