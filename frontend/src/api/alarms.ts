import { http, unwrap } from './http';

export type Alarm = {
  alarmId?: number;
  id?: number;
  cowId?: string;
  barnId?: string;
  metricName?: string;
  severity?: string;
  status?: string;
  startTime?: string;
  endTime?: string;
  thresholdJson?: string;
  evidenceJson?: string;
  deviationScore?: number;
};

type PageResp<T> = { records?: T[]; total?: number };

export async function getAlarms(params: Record<string, unknown>) {
  const data = await unwrap<PageResp<Alarm> | Alarm[]>(
    http.get('/api/alarms', { params })
  );
  if (Array.isArray(data)) {
    return { records: data, total: data.length };
  }
  return { records: data.records || [], total: data.total || 0 };
}

async function postAction(id: number, path: string, note: string) {
  try {
    return await unwrap(http.post(`/api/alarms/${id}/${path}`, { note }));
  } catch {
    return unwrap(http.post(`/api/alarms/${id}/${path}`, { remark: note }));
  }
}

export const ackAlarm = (id: number, note: string) => postAction(id, 'ack', note);
export const resolveAlarm = (id: number, note: string) => postAction(id, 'resolve', note);
export const falsePositiveAlarm = (id: number, note: string) =>
  postAction(id, 'false_positive', note);
