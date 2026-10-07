import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import toast from 'react-hot-toast';
import { cartApi } from '../api/services.js';
import { getErrorMessage } from '../api/axios.js';
import { useAuth } from './AuthContext.jsx';

const CartContext = createContext(null);
const EMPTY = { items: [], totalAmount: 0 };

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(EMPTY);
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) return setCart(EMPTY);
    setLoading(true);
    try {
      setCart(await cartApi.get());
    } catch (e) {
      toast.error(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  // Wraps a cart call: updates state, shows errors, returns true/false
  const run = async (fn, successMsg) => {
    try {
      const res = await fn();
      if (res) setCart(res);
      if (successMsg) toast.success(successMsg);
      return true;
    } catch (e) {
      toast.error(getErrorMessage(e));
      return false;
    }
  };

  const addToCart = (productId, qty = 1) => run(() => cartApi.add(productId, qty), 'Added to cart');
  const updateItem = (productId, qty) => run(() => cartApi.update(productId, qty));
  const removeItem = (productId) => run(() => cartApi.remove(productId), 'Removed from cart');
  const clearCart = async () => {
    const ok = await run(() => cartApi.clear(), 'Cart cleared');
    if (ok) setCart(EMPTY);
    return ok;
  };

  const count = cart.items.reduce((sum, i) => sum + i.quantity, 0);
  const value = { cart, count, loading, refresh, addToCart, updateItem, removeItem, clearCart };
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export const useCart = () => useContext(CartContext);
