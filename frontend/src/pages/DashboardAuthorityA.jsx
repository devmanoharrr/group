import Dashboard from '../components/Dashboard';
import { API_CONFIG } from '../config/api';

export default function DashboardAuthorityA() {
  return (
    <Dashboard
      authorityId="a"
      authorityName={API_CONFIG.authorities.a.name}
      baseUrl={API_CONFIG.authorities.a.baseUrl}
    />
  );
}

