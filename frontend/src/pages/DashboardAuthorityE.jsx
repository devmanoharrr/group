import Dashboard from '../components/Dashboard';
import { API_CONFIG } from '../config/api';

export default function DashboardAuthorityE() {
  return (
    <Dashboard
      authorityId="e"
      authorityName={API_CONFIG.authorities.e.name}
      baseUrl={API_CONFIG.authorities.e.baseUrl}
    />
  );
}

