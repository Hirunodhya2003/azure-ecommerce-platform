export default function Spinner({ label = 'Loading…' }) {
  return (
    <div className="center-box" role="status">
      <div className="spinner" />
      <span className="muted">{label}</span>
    </div>
  );
}
