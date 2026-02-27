import dayjs from 'dayjs';
import { http, unwrap } from './http';

export type ObservationPoint = {
  ts?: string;
  timestamp?: string;
  time?: string;
  value?: number;
  avgValue?: number;
  minValue?: number;
  maxValue?: number;
};

export async function getObservationSeries(params: {
  cowId: string;
  barnId?: string;
  metric: string;
  range: '24h' | '7d';
}) {
  const to = dayjs();
  const from = params.range === '24h' ? to.subtract(24, 'hour') : to.subtract(7, 'day');
  try {
    return await unwrap<ObservationPoint[]>(
      http.get('/api/observations/query', {
        params: {
          cowId: params.cowId,
          barnId: params.barnId,
          metricName: params.metric,
          from: from.format('YYYY-MM-DD HH:mm:ss'),
          to: to.format('YYYY-MM-DD HH:mm:ss')
        }
      })
    );
  } catch {
    return unwrap<ObservationPoint[]>(
      http.get('/api/observations/query', {
        params: {
          cow_id: params.cowId,
          barn_id: params.barnId,
          metric: params.metric,
          start: from.valueOf(),
          end: to.valueOf()
        }
      })
    );
  }
}
