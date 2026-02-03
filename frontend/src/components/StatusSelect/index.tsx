import React, { useMemo } from 'react';
import { Select, Input, Space } from 'antd';

/** 异常无原因时的占位存储值（用于区分 正常 的空），提交时转为空字符串 */
export const ABNORMAL_EMPTY = '\u200B';

/** 状态值与存储值映射：正常√ 异常(存原因) 修理△ 停机○ 修复▲ */
const STATUS_MAP = {
  normal: '\u221A',  // √ 对号（U+221A 兼容性最好）
  repair: '△',
  downtime: '○',
  fixed: '▲',
} as const;

type StatusKey = keyof typeof STATUS_MAP | 'abnormal';

const STATUS_OPTIONS: { value: StatusKey; label: string }[] = [
  { value: 'normal', label: '正常' },
  { value: 'abnormal', label: '异常' },
  { value: 'repair', label: '修理' },
  { value: 'downtime', label: '停机' },
  { value: 'fixed', label: '修复' },
];

function parseValue(val: string | undefined): { status: StatusKey | undefined; reason: string } {
  if (val === ABNORMAL_EMPTY) return { status: 'abnormal', reason: '' };
  if (!val || val === '') return { status: undefined, reason: '' };  // 空值显示占位，必须显式选择
  if (val === '\u221A' || val === '\u2714' || val === '√') return { status: 'normal', reason: '' };
  if (val === '△') return { status: 'repair', reason: '' };
  if (val === '○') return { status: 'downtime', reason: '' };
  if (val === '▲') return { status: 'fixed', reason: '' };
  return { status: 'abnormal', reason: val };
}

interface StatusSelectProps {
  value?: string;
  onChange?: (v: string) => void;
  placeholder?: string;
}

const StatusSelect: React.FC<StatusSelectProps> = ({ value, onChange, placeholder }) => {
  const { status, reason } = useMemo(() => parseValue(value), [value]);

  const handleStatusChange = (s: StatusKey) => {
    if (s === 'abnormal') {
      onChange?.(ABNORMAL_EMPTY);  // 异常无原因时存占位，输入框为空
    } else {
      onChange?.(STATUS_MAP[s as keyof typeof STATUS_MAP]);
    }
  };

  const handleReasonChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const v = e.target.value;
    onChange?.(v.trim() || ABNORMAL_EMPTY);
  };

  return (
    <Space.Compact style={{ width: '100%' }}>
      <Select
        value={status}
        onChange={handleStatusChange}
        options={STATUS_OPTIONS}
        placeholder={placeholder || '请选择'}
        style={{ width: 120, flexShrink: 0 }}
      />
      {status === 'abnormal' && (
        <Input
          value={reason}
          onChange={handleReasonChange}
          allowClear
          style={{ flex: 1, minWidth: 140 }}
        />
      )}
    </Space.Compact>
  );
};

export default StatusSelect;
