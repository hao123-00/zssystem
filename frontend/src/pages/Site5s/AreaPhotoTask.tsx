import React, { useEffect, useState, useRef, useCallback } from 'react';
import { DatePicker, Card, Button, message, Image, Tag, Popconfirm, Space } from 'antd';
import { CameraOutlined, CheckCircleOutlined, CloseCircleOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import {
  getSite5sAreaTasksRange,
  uploadSite5sAreaPhoto,
  setSite5sAreaDayOff,
  deleteSite5sAreaPhotosByDay,
  AreaDailyStatus,
  AreaTask,
  AreaTaskSlot,
  getSite5sAreaPhotoBlob,
} from '@/api/site5s';
import './AreaPhotoTask.less';

const { RangePicker } = DatePicker;
const isMobile = () =>
  /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ||
  (typeof window !== 'undefined' && window.innerWidth < 768);

const AuthPhoto: React.FC<{ id: number; photoUrls: Record<number, string>; loadPhotoUrl: (id: number) => void }> = ({
  id,
  photoUrls,
  loadPhotoUrl,
}) => {
  const url = photoUrls[id];
  useEffect(() => {
    if (!url) loadPhotoUrl(id);
  }, [id, url]);
  if (!url) return <span style={{ marginLeft: 8 }}>加载中</span>;
  return (
    <Image
      width={48}
      height={48}
      src={url}
      style={{ marginLeft: 8, objectFit: 'cover', borderRadius: 4 }}
    />
  );
};

const AreaPhotoTask: React.FC = () => {
  const today = dayjs();
  const monthStart = today.date(1);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([monthStart, today]);
  const [dataByDate, setDataByDate] = useState<AreaDailyStatus[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState<string | null>(null);
  const [dayOffLoading, setDayOffLoading] = useState<string | null>(null);
  const [deletingKey, setDeletingKey] = useState<string | null>(null);
  const [photoUrls, setPhotoUrls] = useState<Record<number, string>>({});
  const requestedRef = useRef<Set<number>>(new Set());
  const photoUrlsRef = useRef<Record<number, string>>({});
  photoUrlsRef.current = photoUrls;

  useEffect(() => {
    fetchTasks();
  }, [dateRange]);

  const fetchTasks = async () => {
    if (!dateRange?.[0] || !dateRange?.[1]) return;
    setLoading(true);
    try {
      const res = await getSite5sAreaTasksRange(
        dateRange[0].format('YYYY-MM-DD'),
        dateRange[1].format('YYYY-MM-DD')
      );
      setDataByDate(res || []);
    } catch (err: any) {
      message.error(err.message || '加载失败');
      setDataByDate([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    return () => {
      Object.values(photoUrlsRef.current).forEach((url) => URL.revokeObjectURL(url));
    };
  }, []);

  const loadPhotoUrl = useCallback((photoId: number) => {
    if (requestedRef.current.has(photoId) || photoUrls[photoId]) return;
    requestedRef.current.add(photoId);
    getSite5sAreaPhotoBlob(photoId)
      .then((blob) => {
        const url = URL.createObjectURL(blob);
        setPhotoUrls((p) => ({ ...p, [photoId]: url }));
      })
      .catch(() => {
        requestedRef.current.delete(photoId);
      });
  }, []);

  const handleUploadClick = (areaId: number, slotIndex: number, photoDate: string) => {
    const key = `${areaId}-${photoDate}-${slotIndex}`;
    setUploading(key);
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    if (isMobile()) input.setAttribute('capture', 'environment');
    input.onchange = async (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (!file || !file.type.startsWith('image/')) {
        message.error('请选择图片');
        setUploading(null);
        return;
      }
      try {
        await uploadSite5sAreaPhoto(areaId, slotIndex, photoDate, file);
        message.success('上传成功');
        fetchTasks();
      } catch (err: any) {
        message.error(err.message || '上传失败');
      } finally {
        setUploading(null);
      }
      input.value = '';
    };
    input.click();
  };

  const handleSetDayOff = async (areaId: number, photoDate: string, dayOff: boolean) => {
    const key = `${areaId}-${photoDate}`;
    setDayOffLoading(key);
    try {
      await setSite5sAreaDayOff(areaId, photoDate, dayOff);
      message.success(dayOff ? '已设为放假' : '已取消放假');
      fetchTasks();
    } catch (err: any) {
      message.error(err.message || '操作失败');
    } finally {
      setDayOffLoading(null);
    }
  };

  const handleDeleteDay = async (areaId: number, photoDate: string) => {
    const key = `${areaId}-${photoDate}`;
    setDeletingKey(key);
    try {
      await deleteSite5sAreaPhotosByDay(areaId, photoDate);
      setPhotoUrls({});
      requestedRef.current.clear();
      message.success('已删除当日拍照记录，可重新上传');
      fetchTasks();
    } catch (err: any) {
      message.error(err.message || '删除失败');
    } finally {
      setDeletingKey(null);
    }
  };

  const slotLabel = (idx: number) => (idx === 1 ? '早间拍照' : '晚间拍照');
  const fmtTime = (t: string | undefined) => (t ? String(t).slice(0, 5) : '');

  const renderSlot = (
    area: AreaTask,
    slot: AreaTaskSlot,
    photoDate: string,
    isDayOff: boolean
  ) => {
    const key = `${area.areaId}-${photoDate}-${slot.slotIndex}`;
    const isUp = uploading === key;
    const label = slotLabel(slot.slotIndex);
    if (slot.completed) {
      return (
        <div key={slot.slotIndex} className="area-slot area-slot-done">
          <span style={{ fontWeight: 500, minWidth: 72 }}>{label}</span>
          <span>
            {fmtTime(slot.scheduledTime)} {slot.onTime ? <CheckCircleOutlined style={{ color: '#52c41a' }} /> : <CloseCircleOutlined style={{ color: '#ff4d4f' }} />}
          </span>
          {slot.uploaderName && <span className="slot-uploader">{slot.uploaderName} {slot.uploadTimeStr}</span>}
          {slot.photoId && <AuthPhoto id={slot.photoId} photoUrls={photoUrls} loadPhotoUrl={loadPhotoUrl} />}
        </div>
      );
    }
    return (
      <div key={slot.slotIndex} className="area-slot">
        <span style={{ fontWeight: 500, minWidth: 72 }}>{label}</span>
        <span>{fmtTime(slot.scheduledTime)} ±{slot.toleranceMinutes || 30}分钟</span>
        {isDayOff ? (
          <span style={{ color: '#999' }}>当日放假，无需拍照</span>
        ) : (
          <Button
            type="primary"
            size="small"
            icon={<CameraOutlined />}
            loading={isUp}
            onClick={() => handleUploadClick(area.areaId, slot.slotIndex, photoDate)}
          >
            拍照上传
          </Button>
        )}
      </div>
    );
  };

  const getAreaForDate = (areaId: number, dateStr: string): AreaTask | undefined => {
    const daily = dataByDate.find((d) => d.statusDate === dateStr);
    return daily?.areas?.find((a) => a.areaId === areaId);
  };

  const dates: string[] = [];
  if (dateRange?.[0] && dateRange?.[1]) {
    let d = dateRange[0];
    while (!d.isAfter(dateRange[1])) {
      dates.push(d.format('YYYY-MM-DD'));
      d = d.add(1, 'day');
    }
  }

  const areas = dataByDate[0]?.areas ?? [];
  const hasCompletedSlot = (area: AreaTask) => area.slots?.some((s) => s.completed) ?? false;

  return (
    <div className="area-photo-task">
      <div className="task-header">
        <span>日期范围：</span>
        <RangePicker
          value={dateRange}
          onChange={(v) => v && v[0] && v[1] && setDateRange([v[0], v[1]])}
        />
        <span style={{ marginLeft: 16, color: '#666', fontSize: 12 }}>每日需完成早间、晚间两次拍照，放假不计入灯光管理</span>
      </div>

      {loading ? (
        <div style={{ padding: 24, textAlign: 'center' }}>加载中...</div>
      ) : (
        <div className="area-cards">
          {areas.map((area) => (
            <Card
              key={area.areaId}
              size="small"
              className="area-card"
              title={
                <span>
                  {area.areaName}（{area.checkItem}）
                  {(area.responsibleUserName || area.responsibleUserName2) && (
                    <span style={{ marginLeft: 8, color: '#666' }}>
                      负责人：{[area.responsibleUserName, area.responsibleUserName2].filter(Boolean).join('、')}
                    </span>
                  )}
                </span>
              }
            >
              <div className="daily-records">
                {dates.map((dateStr) => {
                  const areaTask = getAreaForDate(area.areaId, dateStr);
                  if (!areaTask) return null;
                  const isDayOff = areaTask.status === 2 || areaTask.dayOff;
                  const recordKey = `${area.areaId}-${dateStr}`;
                  return (
                    <div key={recordKey} className="daily-record">
                      <div className="daily-record-header">
                        <span className="daily-date">{dayjs(dateStr).format('MM月DD日')}</span>
                        {isDayOff ? (
                          <Tag color="default">放假</Tag>
                        ) : areaTask.status === 1 ? (
                          <Tag color="success">正常</Tag>
                        ) : (
                          <Tag color="error">异常</Tag>
                        )}
                        <Space size="small" style={{ marginLeft: 'auto' }}>
                          <Button
                            type="link"
                            size="small"
                            loading={dayOffLoading === recordKey}
                            onClick={() => handleSetDayOff(area.areaId, dateStr, !isDayOff)}
                          >
                            {isDayOff ? '取消放假' : '放假'}
                          </Button>
                          {!isDayOff && hasCompletedSlot(areaTask) && (
                            <Popconfirm
                              title="确认删除当日拍照记录？删除后可重新上传"
                              okText="删除"
                              cancelText="取消"
                              onConfirm={() => handleDeleteDay(area.areaId, dateStr)}
                            >
                              <Button
                                type="link"
                                size="small"
                                danger
                                icon={<DeleteOutlined />}
                                loading={deletingKey === recordKey}
                              >
                                删除
                              </Button>
                            </Popconfirm>
                          )}
                        </Space>
                      </div>
                      <div className="area-slots">
                        {(areaTask.slots ?? [])
                          .sort((a, b) => (a.slotIndex ?? 0) - (b.slotIndex ?? 0))
                          .map((slot) => renderSlot(areaTask, slot, dateStr, isDayOff))}
                      </div>
                    </div>
                  );
                })}
              </div>
            </Card>
          ))}
          {areas.length === 0 && !loading && (
            <div style={{ padding: 24, textAlign: 'center', color: '#999' }}>暂无区域配置，请先添加区域</div>
          )}
        </div>
      )}
    </div>
  );
};

export default AreaPhotoTask;
