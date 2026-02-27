import { http, unwrap } from './http';

export type Cow = {
  cowId: string;
  name: string;
  earTag?: string;
  barnId?: string;
  pen?: string;
  ageDays?: number;
  parity?: number;
  lactationDay?: number;
  status?: string;
};

type PageResp<T> = {
  records?: T[];
  total?: number;
  page?: number;
  size?: number;
};

export async function getCows(page = 1, size = 10) {
  const data = await unwrap<PageResp<Cow> | Cow[]>(
    http.get('/api/cows', { params: { page, size } })
  );
  if (Array.isArray(data)) {
    return { records: data, total: data.length };
  }
  return {
    records: data.records || [],
    total: data.total || (data.records?.length || 0)
  };
}

export async function createCow(payload: Cow) {
  return unwrap(http.post('/api/cows', payload));
}

export async function updateCow(payload: Cow) {
  return unwrap(http.put('/api/cows', payload));
}

export async function deleteCow(cowId: string) {
  try {
    return await unwrap(http.delete(`/api/cows/${cowId}`));
  } catch {
    try {
      return await unwrap(http.delete('/api/cows', { params: { cowId } }));
    } catch {
      return unwrap(http.delete('/api/cows', { data: { cowId } }));
    }
  }
}
