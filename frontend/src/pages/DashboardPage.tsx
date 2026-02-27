import { Card, Col, Empty, List, Row, Statistic, Tag, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { getDashboardSummary } from '../api/dashboard';

type KV = Record<string, unknown>;

function asNumber(v: unknown, fallback = 0) {
  const n = Number(v);
  return Number.isFinite(n) ? n : fallback;
}

export function DashboardPage() {
  const [summary, setSummary] = useState<KV>({});

  useEffect(() => {
    getDashboardSummary().then(setSummary).catch(() => setSummary({}));
  }, []);

  const severityMap = (summary.todayAlarmBySeverity || {}) as Record<string, unknown>;
  const observingCows = asNumber(summary.observingCows);
  const treatmentCows = asNumber(summary.treatmentCows);
  const totalAlarms: number = (Object.values(severityMap) as unknown[]).reduce<number>(
    (acc, cur) => acc + asNumber(cur),
    0
  );

  const trend = useMemo(() => {
    const arr = (summary.trend24h || summary.trend || summary.timeSeries || summary.series || []) as KV[];
    return Array.isArray(arr) ? arr : [];
  }, [summary]);

  const topMetrics = useMemo(() => {
    const arr = (summary.topMetrics || []) as KV[];
    return Array.isArray(arr) ? arr : [];
  }, [summary]);

  const chartOption = useMemo(() => {
    const x = trend.map((i) => String(i.hourBucket || i.ts || i.time || ''));
    const y = trend.map((i) => asNumber(i.cnt || i.count || i.value));
    return {
      color: ['#1677ff'],
      tooltip: { trigger: 'axis' },
      grid: { left: 36, right: 20, top: 26, bottom: 28 },
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
          data: y,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { width: 2.5 },
          areaStyle: { color: 'rgba(22,119,255,0.10)' }
        }
      ]
    };
  }, [trend]);

  return (
    <>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        牛群健康概览
      </Typography.Title>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="今日告警总数" value={totalAlarms} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="观察中牛只" value={observingCows} valueStyle={{ color: '#d48806' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="治疗中牛只" value={treatmentCows} valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="panel-card" bordered={false}>
            <Statistic title="告警等级种类" value={Object.keys(severityMap).length} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 2 }}>
        <Col xs={24} xl={16}>
          <Card className="panel-card" bordered={false} title="24小时告警趋势">
            {trend.length > 0 ? <ReactECharts option={chartOption} style={{ height: 330 }} /> : <Empty description="暂无趋势数据" />}
          </Card>
        </Col>

        <Col xs={24} xl={8}>
          <Card className="panel-card" bordered={false} title="今日告警分级">
            {Object.keys(severityMap).length > 0 ? (
              <List
                dataSource={Object.entries(severityMap)}
                renderItem={([level, count]) => (
                  <List.Item>
                    <Typography.Text>{level}</Typography.Text>
                    <Tag color={level === 'P1' ? 'red' : level === 'P2' ? 'orange' : 'blue'}>{asNumber(count)}</Tag>
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="暂无数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>

        <Col xs={24}>
          <Card className="panel-card" bordered={false} title="异常指标排行（Top）">
            {topMetrics.length > 0 ? (
              <List
                dataSource={topMetrics}
                renderItem={(item, idx) => (
                  <List.Item>
                    <Typography.Text>
                      {idx + 1}. {String(item.metric || item.metricName || '-')}
                    </Typography.Text>
                    <Tag color="processing">{asNumber(item.cnt || item.count || item.value)}</Tag>
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="暂无排行数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
      </Row>
    </>
  );
}
