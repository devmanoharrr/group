import Dashboard from '../components/Dashboard';
import { API_CONFIG } from '../config/api';

export default function DashboardAuthorityC() {
  return (
    <Dashboard
      authorityId="c"
      authorityName={API_CONFIG.authorities.c.name}
      baseUrl={API_CONFIG.authorities.c.baseUrl}
    />
  );
}

