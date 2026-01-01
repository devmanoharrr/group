import Dashboard from '../components/Dashboard';
import { API_CONFIG } from '../config/api';

export default function DashboardAuthorityB() {
  return (
    <Dashboard
      authorityId="b"
      authorityName={API_CONFIG.authorities.b.name}
      baseUrl={API_CONFIG.authorities.b.baseUrl}
    />
  );
}

