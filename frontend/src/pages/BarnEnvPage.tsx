import { Card, Col, Empty, Row, Segmented, Select, Space, Statistic, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { getObservationSeries, ObservationPoint } from '../api/observations';
import { getCows } from '../api/cows';

const METRICS = [
  { value: 'temp_c', label: '温度(°C)' },
  { value: 'humidity', label: '湿度(%)' },
  { value: 'nh3_ppm', label: '氨气浓度(ppm)' }
];

function toValue(item: ObservationPoint) {
  const v = item.value ?? item.avgValue;
  return Number.isFinite(Number(v)) ? Number(v) : 0;
}

export function BarnEnvPage() {
  const [barnOptions, setBarnOptions] = useState<Array<{ value: string; label: string }>>([]);
  const [barnId, setBarnId] = useState<string>('B1');
  const [metric, setMetric] = useState<string>('temp_c');
  const [range, setRange] = useState<'24h' | '7d'>('24h');
  const [series, setSeries] = useState<ObservationPoint[]>([]);

  useEffect(() => {
    getCows(1, 500)
      .then((res) => {
        const barns = Array.from(new Set(res.records.map((i) => i.barnId).filter(Boolean))) as string[];
        const opts = barns.map((b) => ({ value: b, label: b }));
        setBarnOptions(opts);
        if (opts.length > 0) {
          setBarnId((prev) => (opts.some((x) => x.value === prev) ? prev : opts[0].value));
        }
      })
      .catch(() => {
        setBarnOptions([
          { value: 'B1', label: 'B1' },
          { value: 'B2', label: 'B2' }
        ]);
      });
  }, []);

  useEffect(() => {
    if (!barnId) return;
    getObservationSeries({ cowId: 'ENV', barnId, metric, range })
      .then(setSeries)
      .catch(() => setSeries([]));
  }, [barnId, metric, range]);

  const values = useMemo(() => series.map(toValue), [series]);
  const latest = values.length > 0 ? values[values.length - 1] : 0;
  const min = values.length > 0 ? Math.min(...values) : 0;
  const max = values.length > 0 ? Math.max(...values) : 0;
  const avg = values.length > 0 ? values.reduce((a, b) => a + b, 0) / values.length : 0;

  const chartOption = useMemo(() => {
    const x = series.map((i) => String(i.ts || i.timestamp || i.time || ''));
    return {
      color: ['#1677ff'],
      tooltip: { trigger: 'axis' },
      grid: { left: 36, right: 20, top: 24, bottom: 34 },
      xAxis: {
        type: 'category',
        data: x,
        axisLabel: { color: '#6b778c', formatter: (v: string) => v.slice(5, 16) }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#6b778c' },
        splitLine: { lineStyle: { color: '#eef3f8' } }
      },
      series: [
        {
          type: 'line',
          smooth: true,
          data: values,
          areaStyle: { color: 'rgba(22,119,255,0.08)' }
        }
      ]
    };
  }, [series, values]);

  return (
    <>
      <div className="page-header">
        <Typography.Title level={4} style={{ margin: 0 }}>
          牛舍环境监测
        </Typography.Title>
        <Space wrap>
          <Select
            value={barnId}
            style={{ width: 120 }}
            options={barnOptions}
            onChange={setBarnId}
            placeholder="选择牛舍"
          />
          <Select
            value={metric}
            style={{ width: 170 }}
            options={METRICS}
            onChange={setMetric}
          />
          <Segmented<'24h' | '7d'>
            value={range}
            onChange={setRange}
            options={[
              { value: '24h', label: '最近24小时' },
              { value: '7d', label: '最近7天' }
            ]}
          />
        </Space>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="最新值" value={latest.toFixed(2)} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="均值" value={avg.toFixed(2)} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="最小值" value={min.toFixed(2)} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="最大值" value={max.toFixed(2)} />
          </Card>
        </Col>
      </Row>

      <Card className="panel-card" bordered={false} style={{ marginTop: 16 }} title="环境趋势">
        {series.length > 0 ? (
          <ReactECharts option={chartOption} style={{ height: 360 }} />
        ) : (
          <Empty description="暂无环境数据" />
        )}
      </Card>
    </>
  );
}

