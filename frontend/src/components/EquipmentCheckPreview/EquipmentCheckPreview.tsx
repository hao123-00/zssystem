import React, { useState, useEffect } from 'react';
import { Modal, Spin, message } from 'antd';
import { getEquipmentCheckPreviewHtml } from '@/api/equipment';
import './EquipmentCheckPreview.less';

interface EquipmentCheckPreviewProps {
  visible: boolean;
  onClose: () => void;
  equipmentId: number | null;
  checkMonth: string;
  title?: string;
}

/**
 * 设备点检表预览 - 与下载的 Excel 效果一致（HTML 渲染）
 */
const EquipmentCheckPreview: React.FC<EquipmentCheckPreviewProps> = ({
  visible,
  onClose,
  equipmentId,
  checkMonth,
  title = '点检表预览',
}) => {
  const [loading, setLoading] = useState(false);
  const [htmlContent, setHtmlContent] = useState<string>('');

  useEffect(() => {
    if (visible && equipmentId && checkMonth) {
      setHtmlContent('');
      setLoading(true);
      getEquipmentCheckPreviewHtml(equipmentId, checkMonth)
        .then((res) => {
          const html = typeof res === 'string' ? res : (res as any)?.data ?? '';
          setHtmlContent(html);
        })
        .catch((err) => {
          message.error('预览失败: ' + (err.message || '未知错误'));
          setHtmlContent('<p style="padding:20px;color:red">加载预览失败</p>');
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [visible, equipmentId, checkMonth]);

  return (
    <Modal
      title={title}
      open={visible}
      onCancel={onClose}
      footer={null}
      width="95%"
      style={{ top: 12 }}
      styles={{ body: { padding: '12px 24px', maxHeight: 'calc(100vh - 100px)', overflow: 'auto' } }}
      destroyOnClose
    >
      <Spin spinning={loading} tip="正在加载预览...">
        {htmlContent ? (
          <iframe
            srcDoc={htmlContent}
            title="点检表预览"
            className="equipment-check-preview-iframe"
            sandbox="allow-same-origin"
          />
        ) : null}
      </Spin>
    </Modal>
  );
};

export default EquipmentCheckPreview;
