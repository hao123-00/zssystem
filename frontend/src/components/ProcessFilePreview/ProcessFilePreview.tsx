import React, { useState, useEffect } from 'react';
import { Modal, Spin, message } from 'antd';
import { getProcessFilePreviewHtml } from '@/api/processFile';
import './ProcessFilePreview.less';

interface ProcessFilePreviewProps {
  visible: boolean;
  onClose: () => void;
  fileId: number | null;
  fileName?: string;
}

/**
 * 工艺文件预览 - 与下载的 Excel 效果一致（HTML 渲染）
 */
const ProcessFilePreview: React.FC<ProcessFilePreviewProps> = ({
  visible,
  onClose,
  fileId,
  fileName = '工艺文件',
}) => {
  const [loading, setLoading] = useState(false);
  const [htmlContent, setHtmlContent] = useState<string>('');

  useEffect(() => {
    if (visible && fileId) {
      setHtmlContent('');
      setLoading(true);
      getProcessFilePreviewHtml(fileId)
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
  }, [visible, fileId]);

  return (
    <Modal
      title={`预览: ${fileName}`}
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
            title="工艺文件预览"
            className="process-file-preview-iframe"
            sandbox="allow-same-origin"
          />
        ) : null}
      </Spin>
    </Modal>
  );
};

export default ProcessFilePreview;
