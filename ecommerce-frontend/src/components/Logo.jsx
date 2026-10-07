import { Link } from 'react-router-dom';

export function LogoMark({ size = 36 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-hidden="true">
      <rect width="64" height="64" rx="16" fill="var(--primary)" />
      <path d="M20 26h24l-2 22a3 3 0 0 1-3 3H25a3 3 0 0 1-3-3z" fill="none" stroke="#fff" strokeWidth="3.5" strokeLinejoin="round" />
      <path d="M26 26v-3a6 6 0 0 1 12 0v3" fill="none" stroke="#fff" strokeWidth="3.5" strokeLinecap="round" />
      <circle cx="32" cy="38" r="4.5" fill="var(--accent)" />
    </svg>
  );
}

export default function Logo({ to = '/', size = 36 }) {
  return (
    <Link to={to} className="logo" aria-label="shopHere.lk home">
      <LogoMark size={size} />
      <span className="logo-text">shop<b>Here</b><span className="logo-tld">.lk</span></span>
    </Link>
  );
}
