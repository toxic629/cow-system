import { http, unwrap } from './http';

export type Rule = {
  ruleId?: number;
  metricName: string;
  minValue?: number;
  maxValue?: number;
  durationSeconds?: number;
  enabled?: boolean;
  name?: string;
  severity?: string;
  type?: string;
  lower?: number;
  upper?: number;
  durationMinutes?: number;
};

type PageResp<T> = { records?: T[]; total?: number };

export async function getRules(page = 1, size = 10) {
  const data = await unwrap<PageResp<Rule> | Rule[]>(
    http.get('/api/rules', { params: { page, size } })
  );
  if (Array.isArray(data)) {
    return { records: data, total: data.length };
  }
  return { records: data.records || [], total: data.total || 0 };
}

function normalizePayload(rule: Rule, includeDuration = true) {
  const payload: Record<string, unknown> = {
    ruleId: rule.ruleId,
    metricName: rule.metricName,
    enabled: rule.enabled ?? true,
    lower: rule.minValue,
    upper: rule.maxValue,
    name: rule.name || `${rule.metricName} rule`,
    severity: rule.severity || 'P2',
    type: rule.type || 'THRESHOLD'
  };
  if (includeDuration && typeof rule.durationSeconds === 'number') {
    payload.durationMinutes = Math.max(1, Math.floor(rule.durationSeconds / 60));
  }
  return payload;
}

export async function createRule(rule: Rule) {
  try {
    return await unwrap(http.post('/api/rules', normalizePayload(rule, true)));
  } catch {
    return unwrap(http.post('/api/rules', normalizePayload(rule, false)));
  }
}

export async function updateRule(rule: Rule) {
  try {
    return await unwrap(http.put('/api/rules', normalizePayload(rule, true)));
  } catch {
    return unwrap(http.put('/api/rules', normalizePayload(rule, false)));
  }
}

export const enableRule = (id: number) => unwrap(http.post(`/api/rules/${id}/enable`));
export const disableRule = (id: number) => unwrap(http.post(`/api/rules/${id}/disable`));
