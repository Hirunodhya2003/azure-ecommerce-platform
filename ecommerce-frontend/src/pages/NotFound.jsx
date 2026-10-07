import { Link } from 'react-router-dom';
import Empty from '../components/Empty.jsx';

export default function NotFound() {
  return <Empty title="Page not found"><Link to="/">Go to products</Link></Empty>;
}
