import React, { useState, useEffect } from 'react';
import { Modal, Spin, message } from 'antd';
import { getHandoverPreviewHtml } from '@/api/handover';
import './HandoverRecordPreview.less';

interface HandoverRecordPreviewProps {
  visible: boolean;
  onClose: () => void;
  equipmentId: number | null;
  recordMonth: string;
  title?: string;
}

/**
 * 交接班记录表预览 - 与下载的 Excel 效果一致（HTML 渲染）
 */
const HandoverRecordPreview: React.FC<HandoverRecordPreviewProps> = ({
  visible,
  onClose,
  equipmentId,
  recordMonth,
  title = '交接班记录表预览',
}) => {
  const [loading, setLoading] = useState(false);
  const [htmlContent, setHtmlContent] = useState<string>('');

  useEffect(() => {
    if (visible && equipmentId && recordMonth) {
      setHtmlContent('');
      setLoading(true);
      getHandoverPreviewHtml(equipmentId, recordMonth)
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
  }, [visible, equipmentId, recordMonth]);

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
            title="交接班记录表预览"
            className="handover-record-preview-iframe"
            sandbox="allow-same-origin"
          />
        ) : null}
      </Spin>
    </Modal>
  );
};

export default HandoverRecordPreview;
