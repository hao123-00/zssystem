import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Button,
  Space,
  Tag,
  Timeline,
  Modal,
  Form,
  Input,
  Radio,
  message,
  Row,
  Col,
  Image,
  Spin,
} from 'antd';
import {
  DownloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  HistoryOutlined,
  ArrowLeftOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getProcessFileById,
  downloadProcessFile,
  approveProcessFile,
  uploadSignature,
} from '@/api/processFile';
import type { ProcessFileInfo } from '@/api/processFile';
import SignaturePad from '@/components/SignaturePad/SignaturePad';
import ProcessFilePreview from '@/components/ProcessFilePreview/ProcessFilePreview';

/**
 * 工艺文件详情页
 */
const ProcessFileDetail: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [fileDetail, setFileDetail] = useState<ProcessFileInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [approvalModalVisible, setApprovalModalVisible] = useState(false);
  const [approvalLoading, setApprovalLoading] = useState(false);
  const [signatureModalVisible, setSignatureModalVisible] = useState(false);
  const [pendingSignature, setPendingSignature] = useState<string | null>(null);
  const [previewVisible, setPreviewVisible] = useState(false);

  useEffect(() => {
    if (id) {
      fetchDetail();
    }
  }, [id]);

  const fetchDetail = async () => {
    setLoading(true);
    try {
      console.log('正在获取工艺文件详情，ID:', id);
      const response = await getProcessFileById(Number(id));
      console.log('工艺文件详情响应:', response);
      
      // request拦截器已经返回了data，所以response直接就是ProcessFileInfo
      if (response) {
        setFileDetail(response);
        console.log('工艺文件详情加载成功');
      } else {
        setFileDetail(null);
        console.warn('工艺文件详情为空');
      }
    } catch (error: any) {
      console.error('加载工艺文件详情失败:', error);
      message.error('加载详情失败: ' + (error.message || '未知错误'));
      setFileDetail(null);
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    if (!fileDetail) return;
    
    try {
      console.log('开始下载工艺文件，ID:', fileDetail.id, '文件名:', fileDetail.fileName);
      const response = await downloadProcessFile(fileDetail.id);
      console.log('下载响应:', response);
      
      // 检查响应状态
      if (!response || !response.data) {
        throw new Error('下载响应为空');
      }
      
      // 检查是否是错误响应（可能是文本错误信息）
      if (response.data instanceof Blob) {
        // 检查blob的type，如果是text/plain或application/json，可能是错误信息
        if (response.data.type === 'text/plain' || response.data.type === 'application/json' || response.data.size < 100) {
          const text = await response.data.text();
          throw new Error(text || '下载失败');
        }
      }
      
      // 对于blob响应，response本身就是axios响应对象，response.data是blob
      const blob = response.data instanceof Blob 
        ? response.data 
        : new Blob([response.data]);
      
      // 从响应头获取文件名（如果后端设置了）
      const contentDisposition = response.headers['content-disposition'];
      let fileName = fileDetail.fileName || `工艺文件_${fileDetail.id}.xlsx`;
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (fileNameMatch && fileNameMatch[1]) {
          fileName = decodeURIComponent(fileNameMatch[1].replace(/['"]/g, ''));
        }
      }
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      message.success('下载成功');
    } catch (error: any) {
      console.error('下载工艺文件失败:', error);
      const errorMessage = error.message || '未知错误';
      message.error('下载失败: ' + errorMessage);
    }
  };

  const handlePreview = () => {
    if (!fileDetail) return;
    setPreviewVisible(true);
  };

  const handleApprove = async () => {
    try {
      const values = await form.validateFields();
      
      if (values.approvalResult === 0 && !values.approvalOpinion) {
        message.error('驳回时必须填写审批意见');
        return;
      }

      // 先显示签名弹窗
      setApprovalModalVisible(false);
      setSignatureModalVisible(true);
      // 保存审批数据，等待签名完成后提交
      setPendingSignature(JSON.stringify(values));
    } catch (error: any) {
      if (error.errorFields) {
        return;
      }
      message.error(error.message || '验证失败');
    }
  };

  const handleSignatureConfirm = async (signatureData: string) => {
    if (!fileDetail || !pendingSignature) return;

    try {
      const values = JSON.parse(pendingSignature);

      setApprovalLoading(true);

      // 提交审批（包含电子签名）
      await approveProcessFile({
        fileId: fileDetail.id,
        approvalResult: values.approvalResult,
        approvalOpinion: values.approvalOpinion,
        signatureData: signatureData, // 包含签名数据
      });

      message.success(values.approvalResult === 1 ? '审批通过' : '已驳回');
      setSignatureModalVisible(false);
      setPendingSignature(null);
      fetchDetail();
    } catch (error: any) {
      message.error(error.message || '审批失败');
    } finally {
      setApprovalLoading(false);
    }
  };

  // 审批流程：车间主任审核(1) → 生产技术部经理批准(2) → 注塑部经理会签(3)
  const getStatusTag = (status: number) => {
    const statusConfig: { [key: number]: { color: string; text: string } } = {
      0: { color: 'default', text: '草稿' },
      1: { color: 'processing', text: '待车间主任审核' },
      2: { color: 'processing', text: '待生产技术部经理批准' },
      3: { color: 'processing', text: '待注塑部经理会签' },
      5: { color: 'success', text: '已批准（生效中）' },
      '-1': { color: 'error', text: '已驳回' },
      '-2': { color: 'default', text: '已作废' },
    };
    const config = statusConfig[status] || { color: 'default', text: '未知' };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  if (loading) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '50px 0' }}>
          <Spin size="large" />
        </div>
      </Card>
    );
  }

  if (!fileDetail) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '50px 0' }}>
          <p>工艺文件不存在或已被删除</p>
          <Button type="primary" onClick={() => navigate('/production/process-file')}>
            返回列表
          </Button>
        </div>
      </Card>
    );
  }

  return (
    <div className="process-file-detail">
      <Card
        title="工艺文件详情"
        extra={
          <Space>
            <Button 
              icon={<EyeOutlined />} 
              onClick={handlePreview}
            >
              预览Excel
            </Button>
            <Button icon={<DownloadOutlined />} onClick={handleDownload}>
              下载文件
            </Button>
            {[1, 2, 3].includes(fileDetail.status) && (
              <Button
                type="primary"
                icon={<CheckCircleOutlined />}
                onClick={() => setApprovalModalVisible(true)}
              >
                审批
              </Button>
            )}
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/production/process-file')}>
              返回列表
            </Button>
          </Space>
        }
      >
        <Descriptions bordered column={2}>
          <Descriptions.Item label="工艺文件编号">
            {fileDetail.fileNo}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            {getStatusTag(fileDetail.status)}
          </Descriptions.Item>
          <Descriptions.Item label="设备编号">
            {fileDetail.equipmentNo}
          </Descriptions.Item>
          <Descriptions.Item label="机台号">
            {fileDetail.machineNo}
          </Descriptions.Item>
          <Descriptions.Item label="文件名称" span={2}>
            {fileDetail.fileName}
          </Descriptions.Item>
          <Descriptions.Item label="文件大小">
            {fileDetail.fileSizeText}
          </Descriptions.Item>
          <Descriptions.Item label="版本">
            {fileDetail.versionText}
          </Descriptions.Item>
          <Descriptions.Item label="创建人">
            {fileDetail.creatorName}
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {fileDetail.createTime}
          </Descriptions.Item>
          {fileDetail.submitTime && (
            <Descriptions.Item label="提交时间">
              {fileDetail.submitTime}
            </Descriptions.Item>
          )}
          {fileDetail.approvalTime && (
            <Descriptions.Item label="批准时间">
              {fileDetail.approvalTime}
            </Descriptions.Item>
          )}
          {fileDetail.effectiveTime && (
            <Descriptions.Item label="生效时间">
              {fileDetail.effectiveTime}
            </Descriptions.Item>
          )}
          {fileDetail.changeReason && (
            <Descriptions.Item label="变更原因" span={2}>
              {fileDetail.changeReason}
            </Descriptions.Item>
          )}
          {fileDetail.remark && (
            <Descriptions.Item label="备注" span={2}>
              {fileDetail.remark}
            </Descriptions.Item>
          )}
        </Descriptions>

        {fileDetail.sealInfo && (
          <Card
            title="电子受控章信息"
            size="small"
            style={{ marginTop: 16 }}
          >
            <Row gutter={16}>
              <Col span={12}>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="印章编号">
                    {fileDetail.sealInfo.sealNo}
                  </Descriptions.Item>
                  <Descriptions.Item label="盖章人">
                    {fileDetail.sealInfo.sealByName}
                  </Descriptions.Item>
                  <Descriptions.Item label="盖章时间">
                    {fileDetail.sealInfo.sealTime}
                  </Descriptions.Item>
                </Descriptions>
              </Col>
              <Col span={12}>
                {fileDetail.sealInfo.sealImagePath && (
                  <div style={{ textAlign: 'center' }}>
                    <Image
                      src={fileDetail.sealInfo.sealImagePath}
                      alt="电子受控章"
                      width={150}
                      fallback="/seal-placeholder.png"
                    />
                  </div>
                )}
              </Col>
            </Row>
          </Card>
        )}

        {fileDetail.approvalHistory && fileDetail.approvalHistory.length > 0 && (
          <Card title="审批历史" size="small" style={{ marginTop: 16 }}>
            <Timeline>
              {fileDetail.approvalHistory.map((record) => (
                <Timeline.Item
                  key={record.id}
                  color={record.approvalResult === 1 ? 'green' : 'red'}
                >
                  <div>
                    <strong>{record.approvalLevelText}</strong>
                    <Tag
                      color={record.approvalResult === 1 ? 'success' : 'error'}
                      style={{ marginLeft: 8 }}
                    >
                      {record.approvalResultText}
                    </Tag>
                  </div>
                  <div style={{ color: '#666', fontSize: 12 }}>
                    审批人：{record.approverName} ({record.approverRole})
                  </div>
                  {record.approvalOpinion && (
                    <div style={{ color: '#666', fontSize: 12 }}>
                      审批意见：{record.approvalOpinion}
                    </div>
                  )}
                  {record.signatureInfo && (
                    <div style={{ marginTop: 8 }}>
                      <div style={{ color: '#666', fontSize: 12, marginBottom: 4 }}>
                        电子签名：
                      </div>
                      <Image
                        src={record.signatureInfo.signatureImageUrl}
                        alt="电子签名"
                        width={150}
                        style={{ border: '1px solid #d9d9d9', borderRadius: 4 }}
                        fallback="/signature-placeholder.png"
                      />
                      <div style={{ color: '#999', fontSize: 11, marginTop: 4 }}>
                        签名时间：{record.signatureInfo.signatureTime}
                      </div>
                    </div>
                  )}
                  <div style={{ color: '#999', fontSize: 12, marginTop: 8 }}>
                    {record.approvalTime}
                  </div>
                </Timeline.Item>
              ))}
            </Timeline>
          </Card>
        )}
      </Card>

      {/* 审批弹窗 */}
      <Modal
        title="审批工艺文件"
        open={approvalModalVisible}
        onOk={handleApprove}
        onCancel={() => setApprovalModalVisible(false)}
        confirmLoading={approvalLoading}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="approvalResult"
            label="审批结果"
            rules={[{ required: true, message: '请选择审批结果' }]}
            initialValue={1}
          >
            <Radio.Group>
              <Radio value={1}>通过</Radio>
              <Radio value={0}>驳回</Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            name="approvalOpinion"
            label="审批意见"
            rules={[
              {
                validator: async (_, value) => {
                  const result = form.getFieldValue('approvalResult');
                  if (result === 0 && !value) {
                    throw new Error('驳回时必须填写审批意见');
                  }
                },
              },
            ]}
          >
            <Input.TextArea
              rows={4}
              placeholder="请输入审批意见（驳回时必填）"
              maxLength={500}
              showCount
            />
          </Form.Item>
          </Form>
        </Modal>

        {/* 审批签名弹窗 */}
        <Modal
          title="电子签名 - 审批确认"
          open={signatureModalVisible}
          onCancel={() => {
            setSignatureModalVisible(false);
            setPendingSignature(null);
            setApprovalModalVisible(true);
          }}
          footer={null}
          width={700}
          destroyOnClose
        >
          <SignaturePad
            onConfirm={handleSignatureConfirm}
            onCancel={() => {
              setSignatureModalVisible(false);
              setPendingSignature(null);
              setApprovalModalVisible(true);
            }}
          />
        </Modal>

        {/* 工艺文件预览弹窗（与下载 Excel 效果一致） */}
        <ProcessFilePreview
          visible={previewVisible}
          onClose={() => setPreviewVisible(false)}
          fileId={fileDetail?.id ?? null}
          fileName={fileDetail?.fileName}
        />
      </div>
    );
  };

  export default ProcessFileDetail;
