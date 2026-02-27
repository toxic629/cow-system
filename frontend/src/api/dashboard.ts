import { http, unwrap } from './http';

export async function getDashboardSummary() {
  return unwrap<Record<string, unknown>>(http.get('/api/dashboard/summary'));
}
