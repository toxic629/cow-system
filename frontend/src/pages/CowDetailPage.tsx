import { Card, Empty, Segmented, Select, Space, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { useLocation, useParams } from 'react-router-dom';
import { getAlarms } from '../api/alarms';
import { getObservationSeries, ObservationPoint } from '../api/observations';

const METRICS = [
  { value: 'resp_rate', label: '呼吸频率' },
  { value: 'heart_rate', label: '心率' },
  { value: 'rumination_minutes', label: '反刍时长' },
  { value: 'activity_index', label: '活动指数' }
];

type AlarmMark = { startTime?: string; endTime?: string };

export function CowDetailPage() {
  const { cowId = '' } = useParams();
  const location = useLocation();
  const barnId = (location.state as { barnId?: string } | undefined)?.barnId;
  const [metric, setMetric] = useState('resp_rate');
  const [range, setRange] = useState<'24h' | '7d'>('24h');
  const [series, setSeries] = useState<ObservationPoint[]>([]);
  const [alarmMarks, setAlarmMarks] = useState<AlarmMark[]>([]);

  useEffect(() => {
    getObservationSeries({ cowId, barnId, metric, range }).then(setSeries).catch(() => setSeries([]));
  }, [cowId, barnId, metric, range]);

  useEffect(() => {
    getAlarms({ cowId, metric, page: 1, size: 100 }).then((res) => setAlarmMarks(res.records)).catch(() => setAlarmMarks([]));
  }, [cowId, metric, range]);

  const chartOption = useMemo(() => {
    const points = series.map((i) => {
      const x = i.ts || i.timestamp || i.time || '';
      const y = i.value ?? i.avgValue ?? 0;
      return [x, y];
    });

    return {
      color: ['#1677ff'],
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: { type: 'category', data: points.map((p) => p[0]), axisLabel: { color: '#6b778c' } },
      yAxis: { type: 'value', axisLabel: { color: '#6b778c' }, splitLine: { lineStyle: { color: '#eef3f8' } } },
      series: [{ type: 'line', smooth: true, data: points.map((p) => p[1]) }],
      markArea: {
        itemStyle: { color: 'rgba(255,77,79,0.12)' },
        data: alarmMarks.filter((a) => a.startTime && a.endTime).map((a) => [{ xAxis: a.startTime }, { xAxis: a.endTime }])
      }
    };
  }, [series, alarmMarks]);

  return (
    <Card className="panel-card" bordered={false}>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        单牛详情：{cowId}
      </Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
        当前页面仅展示牛只个体指标；环境指标（温度、湿度、氨气）建议在牛舍环境页面查看。
      </Typography.Paragraph>
      <Space wrap style={{ marginBottom: 16 }}>
        <Select value={metric} style={{ width: 220 }} onChange={setMetric} options={METRICS} />
        <Segmented<'24h' | '7d'> value={range} onChange={setRange} options={[{ value: '24h', label: '最近24小时' }, { value: '7d', label: '最近7天' }]} />
      </Space>
      {series.length > 0 ? <ReactECharts option={chartOption} style={{ height: 390 }} /> : <Empty description="暂无观测数据" />}
    </Card>
  );
}
