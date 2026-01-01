import Dashboard from '../components/Dashboard';
import { API_CONFIG } from '../config/api';

export default function DashboardAuthorityD() {
  return (
    <Dashboard
      authorityId="d"
      authorityName={API_CONFIG.authorities.d.name}
      baseUrl={API_CONFIG.authorities.d.baseUrl}
    />
  );
}

