import api from './axios.js';

// ---- Normalizers (tolerate small naming differences in your DTOs) ----
const normItem = (i) => ({
  productId: i.productId ?? i.product?.id,
  name: i.productName ?? i.name ?? i.product?.name,
  sku: i.sku ?? i.productSku ?? i.product?.sku,
  quantity: i.quantity,
  unitPrice: i.unitPrice ?? i.price,
  subtotal: i.subtotal ?? i.unitPrice * i.quantity,
});
export const normCart = (c) => ({ ...c, items: (c.items ?? c.cartItems ?? []).map(normItem), totalAmount: c.totalAmount ?? 0 });
export const normOrder = (o) => ({ ...o, items: (o.items ?? o.orderItems ?? []).map(normItem) });

// ---- Auth ----
export const authApi = {
  login: (data) => api.post('/auth/login', data).then((r) => r.data),
  register: (data) => api.post('/auth/register', data).then((r) => r.data),
};

// ---- Products ----
export const productApi = {
  list: ({ page = 0, size = 12, sortBy = 'id', direction = 'asc' } = {}) =>
    api.get('/products', { params: { page, size, sortBy, direction } }).then((r) => r.data),
  get: (id) => api.get(`/products/${id}`).then((r) => r.data),
  search: (keyword) => api.get('/products/search', { params: { keyword } }).then((r) => r.data),
  create: (data) => api.post('/products', data).then((r) => r.data),
  update: (id, data) => api.put(`/products/${id}`, data).then((r) => r.data),
  remove: (id) => api.delete(`/products/${id}`),
};

// ---- Categories ----
export const categoryApi = {
  list: () => api.get('/categories').then((r) => r.data),
  create: (data) => api.post('/categories', data).then((r) => r.data),
  update: (id, data) => api.put(`/categories/${id}`, data).then((r) => r.data),
  remove: (id) => api.delete(`/categories/${id}`),
};

// ---- Cart ----
export const cartApi = {
  get: () => api.get('/cart').then((r) => normCart(r.data)),
  add: (productId, quantity = 1) => api.post('/cart/items', { productId, quantity }).then((r) => normCart(r.data)),
  update: (productId, quantity) => api.put(`/cart/items/${productId}`, { quantity }).then((r) => normCart(r.data)),
  remove: (productId) => api.delete(`/cart/items/${productId}`).then((r) => normCart(r.data)),
  clear: () => api.delete('/cart'),
};

// ---- Orders ----
export const orderApi = {
  create: (shippingAddress) => api.post('/orders', { shippingAddress }).then((r) => normOrder(r.data)),
  mine: (status) => api.get('/orders', { params: status ? { status } : {} }).then((r) => r.data.map(normOrder)),
  get: (id) => api.get(`/orders/${id}`).then((r) => normOrder(r.data)),
  cancel: (id) => api.put(`/orders/${id}/cancel`).then((r) => normOrder(r.data)),
  adminList: (status) => api.get('/orders/admin', { params: status ? { status } : {} }).then((r) => r.data.map(normOrder)),
  adminUpdateStatus: (id, status) => api.put(`/orders/admin/${id}/status`, { status }).then((r) => normOrder(r.data)),
};

// ---- Users (admin) ----
export const userApi = {
  list: () => api.get('/users/admin').then((r) => r.data),
  setStatus: (id, active) => api.put(`/users/admin/${id}/status`, { active }).then((r) => r.data),
  setRole: (id, role) => api.put(`/users/admin/${id}/role`, { role }).then((r) => r.data),
};
