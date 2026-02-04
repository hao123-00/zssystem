import React, { useEffect, useState, useRef } from 'react';
import { Card, Spin, message } from 'antd';
import * as echarts from 'echarts';
import { getLightingStats, LightingStats } from '@/api/site5s';

const Dashboard: React.FC = () => {
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstance = useRef<echarts.ECharts | null>(null);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<LightingStats | null>(null);

  useEffect(() => {
    getLightingStats()
      .then((res) => {
        setStats(res);
      })
      .catch(() => {
        message.error('加载灯光管理数据失败');
        setStats({ completionCount: 0, days: 0, completionRate: 0 });
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!chartRef.current || !stats) return;

    if (chartInstance.current) chartInstance.current.dispose();
    chartInstance.current = echarts.init(chartRef.current);

    const completionRatePercent = Math.round(stats.completionRate * 100);
    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          const p0 = params?.[0];
          const p1 = params?.[1];
          let s = '';
          if (p0) s += `${p0.marker} ${p0.seriesName}: ${p0.value}<br/>`;
          if (p1) s += `${p1.marker} ${p1.seriesName}: ${p1.value}%<br/>`;
          return s || '';
        },
      },
      grid: {
        left: 56,
        right: 56,
        top: 24,
        bottom: 40,
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: ['本月统计'],
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666', fontSize: 12 },
      },
      yAxis: [
        {
          type: 'value',
          name: '完成次数',
          nameTextStyle: { color: '#5b8ff9', fontSize: 12 },
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } },
          axisLabel: { color: '#666' },
        },
        {
          type: 'value',
          name: '完成率(%)',
          min: 0,
          max: 100,
          nameTextStyle: { color: '#5ad8a6', fontSize: 12 },
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { color: '#666', formatter: '{value}%' },
        },
      ],
      series: [
        {
          name: '完成次数',
          type: 'bar',
          data: [stats.completionCount],
          barWidth: '36%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#5b8ff9' },
              { offset: 1, color: '#93c5fd' },
            ]),
            borderRadius: [6, 6, 0, 0],
          },
          yAxisIndex: 0,
        },
        {
          name: '拍照完成率',
          type: 'bar',
          data: [completionRatePercent],
          barWidth: '36%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#5ad8a6' },
              { offset: 1, color: '#93e7b7' },
            ]),
            borderRadius: [6, 6, 0, 0],
          },
          yAxisIndex: 1,
        },
      ],
    };

    chartInstance.current.setOption(option);

    const onResize = () => chartInstance.current?.resize();
    window.addEventListener('resize', onResize);
    return () => {
      window.removeEventListener('resize', onResize);
    };
  }, [stats]);

  useEffect(() => {
    return () => {
      chartInstance.current?.dispose();
      chartInstance.current = null;
    };
  }, []);

  if (loading) {
    return (
      <div style={{ padding: 48, textAlign: 'center' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      <div style={{ width: '25%', minWidth: 280 }}>
        <Card
          bordered={false}
          style={{
            borderRadius: 12,
            boxShadow: '0 2px 12px rgba(0,0,0,0.06)',
            overflow: 'hidden',
          }}
        >
          <div ref={chartRef} style={{ width: '100%', height: 220 }} />
          <div style={{ textAlign: 'center', paddingTop: 12, fontSize: 16, fontWeight: 600, color: '#1a1a2e' }}>
            灯光管理示意图
          </div>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
