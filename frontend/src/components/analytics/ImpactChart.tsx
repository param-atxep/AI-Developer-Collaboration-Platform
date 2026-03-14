import { useState } from 'react';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import type { TimeSeriesDataPoint } from '@/types';

interface ImpactChartProps {
  data: TimeSeriesDataPoint[];
  title?: string;
  className?: string;
}

type ChartType = 'line' | 'bar';

export default function ImpactChart({ data, title = 'Food Saved Over Time', className = '' }: ImpactChartProps) {
  const [chartType, setChartType] = useState<ChartType>('line');

  const formattedData = data.map((point) => ({
    ...point,
    date: new Date(point.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
  }));

  const CustomTooltip = ({
    active,
    payload,
    label,
  }: {
    active?: boolean;
    payload?: Array<{ value: number; color: string }>;
    label?: string;
  }) => {
    if (!active || !payload?.length) return null;

    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-3 text-sm">
        <p className="font-medium text-gray-900 dark:text-white mb-1">{label}</p>
        <p className="text-primary-600 dark:text-primary-400">
          <span className="font-semibold">{payload[0].value.toLocaleString()}</span> kg saved
        </p>
      </div>
    );
  };

  return (
    <div className={`card p-6 ${className}`}>
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{title}</h3>
        <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 p-0.5 rounded-lg">
          <button
            onClick={() => setChartType('line')}
            className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
              chartType === 'line'
                ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow-sm'
                : 'text-gray-500 dark:text-gray-400'
            }`}
          >
            Line
          </button>
          <button
            onClick={() => setChartType('bar')}
            className={`px-3 py-1 rounded text-xs font-medium transition-colors ${
              chartType === 'bar'
                ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow-sm'
                : 'text-gray-500 dark:text-gray-400'
            }`}
          >
            Bar
          </button>
        </div>
      </div>

      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          {chartType === 'line' ? (
            <LineChart data={formattedData}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-gray-700" />
              <XAxis
                dataKey="date"
                className="text-xs"
                tick={{ fill: '#9ca3af' }}
                axisLine={{ stroke: '#e5e7eb' }}
              />
              <YAxis
                className="text-xs"
                tick={{ fill: '#9ca3af' }}
                axisLine={{ stroke: '#e5e7eb' }}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend />
              <Line
                type="monotone"
                dataKey="value"
                name="Food Saved (kg)"
                stroke="#22c55e"
                strokeWidth={2.5}
                dot={{ fill: '#22c55e', r: 4 }}
                activeDot={{ r: 6, stroke: '#22c55e', strokeWidth: 2, fill: '#fff' }}
              />
            </LineChart>
          ) : (
            <BarChart data={formattedData}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-gray-700" />
              <XAxis
                dataKey="date"
                className="text-xs"
                tick={{ fill: '#9ca3af' }}
                axisLine={{ stroke: '#e5e7eb' }}
              />
              <YAxis
                className="text-xs"
                tick={{ fill: '#9ca3af' }}
                axisLine={{ stroke: '#e5e7eb' }}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend />
              <Bar
                dataKey="value"
                name="Food Saved (kg)"
                fill="#22c55e"
                radius={[4, 4, 0, 0]}
              />
            </BarChart>
          )}
        </ResponsiveContainer>
      </div>
    </div>
  );
}
