import React, { useEffect, useState, useRef, useCallback } from 'react';
import { DatePicker, Card, Button, message, Image, Tag } from 'antd';
import { CameraOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import {
  getSite5sAreaTasks,
  uploadSite5sAreaPhoto,
  AreaDailyStatus,
  AreaTask,
  AreaTaskSlot,
  getSite5sAreaPhotoBlob,
} from '@/api/site5s';
import './AreaPhotoTask.less';

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
  const [photoDate, setPhotoDate] = useState(dayjs());
  const [data, setData] = useState<AreaDailyStatus | null>(null);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchTasks();
  }, [photoDate]);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const res = await getSite5sAreaTasks(photoDate.format('YYYY-MM-DD'));
      setData(res);
    } catch (err: any) {
      message.error(err.message || '加载失败');
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadClick = (areaId: number, slotIndex: number) => {
    const key = `${areaId}-${slotIndex}`;
    setUploading(key);
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    if (isMobile()) {
      input.setAttribute('capture', 'environment');
    }
    input.onchange = async (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (!file || !file.type.startsWith('image/')) {
        message.error('请选择图片');
        setUploading(null);
        return;
      }
      try {
        await uploadSite5sAreaPhoto(areaId, slotIndex, photoDate.format('YYYY-MM-DD'), file);
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

  const [photoUrls, setPhotoUrls] = useState<Record<number, string>>({});
  const requestedRef = useRef<Set<number>>(new Set());

  const photoUrlsRef = useRef<Record<number, string>>({});
  photoUrlsRef.current = photoUrls;
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

  const renderSlot = (area: AreaTask, slot: AreaTaskSlot) => {
    const key = `${area.areaId}-${slot.slotIndex}`;
    const isUp = uploading === key;
    if (slot.completed) {
      return (
        <div key={slot.slotIndex} className="area-slot area-slot-done">
          <span>
            {slot.scheduledTime} {slot.onTime ? <CheckCircleOutlined style={{ color: '#52c41a' }} /> : <CloseCircleOutlined style={{ color: '#ff4d4f' }} />}
          </span>
          {slot.uploaderName && <span className="slot-uploader">{slot.uploaderName} {slot.uploadTimeStr}</span>}
          {slot.photoId && (
            <AuthPhoto id={slot.photoId} photoUrls={photoUrls} loadPhotoUrl={loadPhotoUrl} />
          )}
        </div>
      );
    }
    return (
      <div key={slot.slotIndex} className="area-slot">
        <span>{slot.scheduledTime} ±{slot.toleranceMinutes || 30}分钟</span>
        <Button
          type="primary"
          size="small"
          icon={<CameraOutlined />}
          loading={isUp}
          onClick={() => handleUploadClick(area.areaId, slot.slotIndex)}
        >
          拍照上传
        </Button>
      </div>
    );
  };

  return (
    <div className="area-photo-task">
      <div className="task-header">
        <span>拍照日期：</span>
        <DatePicker value={photoDate} onChange={(d) => d && setPhotoDate(d)} />
      </div>

      {loading ? (
        <div style={{ padding: 24, textAlign: 'center' }}>加载中...</div>
      ) : (
        <div className="area-cards">
          {data?.areas?.map((area) => (
            <Card key={area.areaId} size="small" className="area-card" title={
              <span>
                {area.areaName}（{area.dutyName}）
                {area.status === 1 ? (
                  <Tag color="success" style={{ marginLeft: 8 }}>正常</Tag>
                ) : (
                  <Tag color="error" style={{ marginLeft: 8 }}>异常</Tag>
                )}
              </span>
            }>
              <div className="area-slots">
                {area.slots?.map((slot) => renderSlot(area, slot))}
              </div>
            </Card>
          ))}
          {(!data?.areas || data.areas.length === 0) && !loading && (
            <div style={{ padding: 24, textAlign: 'center', color: '#999' }}>暂无区域配置，请先添加区域</div>
          )}
        </div>
      )}

      <input ref={fileInputRef} type="file" accept="image/*" style={{ display: 'none' }} />
    </div>
  );
};

export default AreaPhotoTask;
