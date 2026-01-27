import React, { useState, useEffect } from 'react';
import {
  Form,
  Select,
  Input,
  InputNumber,
  Button,
  message,
  Card,
  Row,
  Col,
  Space,
  Modal,
} from 'antd';
import { SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getEquipmentList } from '@/api/equipment';
import { saveProcessFileForm } from '@/api/processFile';
import './ProcessFileForm.less';

const { TextArea } = Input;

/**
 * 工艺文件表单填写页
 */
const ProcessFileForm: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [equipmentList, setEquipmentList] = useState<any[]>([]);
  const navigate = useNavigate();
  const { id } = useParams(); // 修改时传入文件ID

  useEffect(() => {
    fetchEquipmentList();
  }, []);

  const fetchEquipmentList = async () => {
    try {
      const response = await getEquipmentList({ pageNum: 1, pageSize: 1000 });
      if (response && response.list) {
        // 过滤正常状态的设备，按机台号排序
        const filtered = response.list
          .filter((item: any) => item.status === 1)
          .sort((a: any, b: any) => {
            const aNo = a.machineNo || '';
            const bNo = b.machineNo || '';
            if (!aNo && !bNo) return 0;
            if (!aNo) return 1;
            if (!bNo) return -1;
            return aNo.localeCompare(bNo, undefined, { numeric: true });
          });
        setEquipmentList(filtered);
      }
    } catch (error: any) {
      console.error('获取设备列表失败:', error);
      message.error('获取设备列表失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      setLoading(true);
      const response = await saveProcessFileForm({
        ...values,
        id: id ? Number(id) : undefined,
      });
      
      message.success('保存成功');
      
      // 如果有返回的文件ID，跳转到详情页，否则返回列表页
      if (response && response.data) {
        navigate(`/production/process-file/detail/${response.data}`);
      } else {
        navigate('/production/process-file');
      }
    } catch (error: any) {
      if (error.errorFields) {
        // 表单验证错误
        return;
      }
      message.error(error.message || '保存失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    Modal.confirm({
      title: '确认取消',
      content: '确定要取消吗？未保存的数据将丢失',
      onOk: () => {
        navigate('/production/process-file');
      },
    });
  };

  return (
    <div className="process-file-form-container">
      <Card
        title={
          <Space>
            <Button
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('/production/process-file')}
            >
              返回
            </Button>
            <span>{id ? '修改工艺文件' : '新建工艺文件'}</span>
          </Space>
        }
        extra={
          <Space>
            <Button onClick={handleCancel}>取消</Button>
            <Button type="primary" icon={<SaveOutlined />} onClick={handleSubmit} loading={loading}>
              保存
            </Button>
          </Space>
        }
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            // 设置默认值
            cavityQuantity: 1,
            ejectionCount: 1,
          }}
        >
          {/* 表头信息 */}
          <Card title="基本信息" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="产品型号"
                  name="productModel"
                >
                  <Input placeholder="请输入产品型号" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="产品名称"
                  name="productName"
                >
                  <Input placeholder="请输入产品名称" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="模具制造公司"
                  name="moldManufacturingCompany"
                >
                  <Input placeholder="请输入模具制造公司" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="零件名称"
                  name="partName"
                >
                  <Input placeholder="请输入零件名称" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="项目负责人"
                  name="projectLeader"
                >
                  <Input placeholder="请输入项目负责人" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="选择设备（机台号）"
                  name="equipmentId"
                  rules={[{ required: true, message: '请选择设备' }]}
                >
                  <Select
                    placeholder="请选择机台号"
                    showSearch
                    optionFilterProp="label"
                    filterOption={(input, option: any) => {
                      const label = option?.label || '';
                      return label.toLowerCase().includes(input.toLowerCase());
                    }}
                  >
                    {equipmentList.map((item) => {
                      const label = item.machineNo
                        ? `${item.machineNo} - ${item.equipmentName} (${item.equipmentNo})`
                        : `${item.equipmentNo} - ${item.equipmentName} [无机台号]`;
                      return (
                        <Select.Option key={item.id} value={item.id} label={label}>
                          {label}
                        </Select.Option>
                      );
                    })}
                  </Select>
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* 材料信息 */}
          <Card title="材料信息" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={6}>
                <Form.Item label="材料名称" name="materialName">
                  <Input placeholder="请输入材料名称" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="材料牌号" name="materialGrade">
                  <Input placeholder="请输入材料牌号" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="材料颜色" name="materialColor">
                  <Input placeholder="请输入材料颜色" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="颜料名称" name="pigmentName">
                  <Input placeholder="请输入颜料名称" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="比例(%)" name="pigmentRatio">
                  <InputNumber
                    placeholder="请输入比例"
                    min={0}
                    max={100}
                    precision={2}
                    style={{ width: '100%' }}
                    addonAfter="%"
                  />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="零件净重(g)" name="partNetWeight">
                  <InputNumber
                    placeholder="请输入零件净重"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    addonAfter="g"
                  />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="零件毛重(g)" name="partGrossWeight">
                  <InputNumber
                    placeholder="请输入零件毛重"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    addonAfter="g"
                  />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="消耗定额(g)" name="consumptionQuota">
                  <InputNumber
                    placeholder="请输入消耗定额"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    addonAfter="g"
                  />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* 模具信息 */}
          <Card title="模具信息" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item label="模具编号" name="moldNumber">
                  <Input placeholder="请输入模具编号" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="型腔数量" name="cavityQuantity">
                  <InputNumber
                    placeholder="请输入型腔数量"
                    min={1}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item label="锁模力(Mpa)" name="clampingForce">
                  <InputNumber
                    placeholder="请输入锁模力"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    addonAfter="Mpa"
                  />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item label="产品关键尺寸" name="productKeyDimensions">
                  <TextArea
                    rows={4}
                    placeholder="请输入产品关键尺寸"
                  />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* 注塑成型工艺参数 - 合模、射胶、保压 */}
          <Card title="注塑成型工艺参数（合模、射胶、保压）" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={[8, 8]}>
              {/* 合模参数 */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>合模参数</div>
              </Col>
              {[
                { key: 'clamp1', label: '合模1' },
                { key: 'clamp2', label: '合模2' },
                { key: 'moldProtection', label: '模保' },
                { key: 'highPressure', label: '高压' },
              ].map((item) => (
                <Col span={6} key={item.key}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>{item.label}</div>
                  <Form.Item name={`${item.key}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
              
              {/* 进芯参数 */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>进芯参数</div>
              </Col>
              {[
                { key: 'corePull1In', label: '进芯一' },
                { key: 'corePull2In', label: '进芯二' },
              ].map((item) => (
                <Col span={6} key={item.key}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>{item.label}</div>
                  <Form.Item name={`${item.key}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
              
              {/* 射胶参数（6段） */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>射胶参数</div>
              </Col>
              {[1, 2, 3, 4, 5, 6].map((num) => (
                <Col span={4} key={`injection${num}`}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>射胶{num}段</div>
                  <Form.Item name={`injection${num}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`injection${num}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`injection${num}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
              
              {/* 保压参数（3段） */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>保压参数</div>
              </Col>
              {[1, 2, 3].map((num) => (
                <Col span={8} key={`holding${num}`}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>保压{num}段</div>
                  <Form.Item name={`holding${num}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`holding${num}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`holding${num}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
            </Row>
          </Card>

          {/* 注塑成型工艺参数 - 开模、抽芯、熔胶、顶出 */}
          <Card title="注塑成型工艺参数（开模、抽芯、熔胶、顶出）" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={[8, 8]}>
              {/* 开模参数 */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>开模参数</div>
              </Col>
              {[1, 2, 3, 4].map((num) => (
                <Col span={6} key={`openMold${num}`}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>开模{num}</div>
                  <Form.Item name={`openMold${num}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`openMold${num}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`openMold${num}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
              
              {/* 抽芯参数 */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>抽芯参数</div>
              </Col>
              {[
                { key: 'corePull1Out', label: '抽芯一' },
                { key: 'corePull2Out', label: '抽芯二' },
              ].map((item) => (
                <Col span={6} key={item.key}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>{item.label}</div>
                  <Form.Item name={`${item.key}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
              
              {/* 熔胶参数 */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>熔胶参数</div>
              </Col>
              {[
                { key: 'melt1', label: '熔胶1' },
                { key: 'decompressionBeforeMelt', label: '熔前松退' },
                { key: 'decompressionAfterMelt', label: '熔后松退' },
              ].map((item) => (
                <Col span={8} key={item.key}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>{item.label}</div>
                  <Form.Item name={`${item.key}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
              
              {/* 顶出参数 */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>顶出参数</div>
              </Col>
              {[
                { key: 'eject1Speed', label: '顶出一速' },
                { key: 'eject2Speed', label: '顶出二速' },
                { key: 'ejectRetract1Speed', label: '顶退一速' },
                { key: 'ejectRetract2Speed', label: '顶退二速' },
              ].map((item) => (
                <Col span={6} key={item.key}>
                  <div style={{ marginBottom: 8, fontWeight: 'bold' }}>{item.label}</div>
                  <Form.Item name={`${item.key}Pressure`} label="压力(bar)">
                    <InputNumber
                      placeholder="压力"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Flow`} label="流量(%)">
                    <InputNumber
                      placeholder="流量"
                      min={0}
                      max={100}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                  <Form.Item name={`${item.key}Position`} label="位置(mm)">
                    <InputNumber
                      placeholder="位置"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±10"
                    />
                  </Form.Item>
                </Col>
              ))}
              
              {/* 特殊模式参数 */}
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>特殊模式参数</div>
              </Col>
              <Col span={6}>
                <Form.Item label="注射模式" name="injectionMode">
                  <Input placeholder="请输入注射模式" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="进芯方式" name="corePullInMethod">
                  <Input placeholder="请输入进芯方式" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="抽芯方式" name="corePullOutMethod">
                  <Input placeholder="请输入抽芯方式" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="座台方式" name="nozzleContactMethod">
                  <Input placeholder="请输入座台方式" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="顶针模式" name="ejectionMode">
                  <Input placeholder="请输入顶针模式" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="顶针次数" name="ejectionCount">
                  <InputNumber
                    placeholder="请输入顶针次数"
                    min={0}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="螺杆转速" name="screwSpeed">
                  <InputNumber
                    placeholder="请输入螺杆转速"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item label="抽芯行程方式" name="corePullStrokeMethod">
                  <Input placeholder="请输入抽芯行程方式" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* 温度和时间参数 */}
          <Card title="温度和时间参数" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>料筒温度(℃)</div>
              </Col>
              {[1, 2, 3, 4].map((num) => (
                <Col span={6} key={`barrelTemp${num}`}>
                  <Form.Item label={`第${num}段`} name={`barrelTemp${num}`}>
                    <InputNumber
                      placeholder="温度"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                      addonAfter="±5"
                    />
                  </Form.Item>
                </Col>
              ))}
              <Col span={6}>
                <Form.Item label="模具温度(℃)" name="moldTemp">
                  <InputNumber
                    placeholder="温度"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    addonAfter="±5"
                  />
                </Form.Item>
              </Col>
              
              <Col span={24}>
                <div style={{ fontWeight: 'bold', marginTop: 16, marginBottom: 8 }}>注塑成型时间(s)</div>
              </Col>
              {[
                { key: 'clampingTime', label: '合模' },
                { key: 'moldProtectionTime', label: '模保' },
                { key: 'corePull1InTime', label: '进芯1' },
                { key: 'corePull2InTime', label: '进芯2' },
                { key: 'injectionTime', label: '注射' },
                { key: 'holdingTime', label: '保压' },
                { key: 'coolingTime', label: '冷却' },
                { key: 'corePull1OutTime', label: '抽芯1' },
                { key: 'corePull2OutTime', label: '抽芯2' },
                { key: 'moldOpeningTime', label: '开模' },
                { key: 'partEjectionTime', label: '取件时间' },
                { key: 'totalTime', label: '总时间' },
              ].map((item) => (
                <Col span={4} key={item.key}>
                  <Form.Item label={item.label} name={item.key}>
                    <InputNumber
                      placeholder="时间"
                      min={0}
                      precision={2}
                      style={{ width: '100%' }}
                    />
                  </Form.Item>
                </Col>
              ))}
            </Row>
          </Card>

          {/* 原材料干燥处理和零件后处理 */}
          <Card title="原材料干燥处理和零件后处理" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>原材料干燥处理</div>
                <Form.Item label="使用设备" name="dryingEquipment">
                  <Input placeholder="请输入使用设备" />
                </Form.Item>
                <Form.Item label="盛料高度" name="materialFillHeight">
                  <InputNumber
                    placeholder="请输入盛料高度"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="翻料时间" name="materialTurningTime">
                  <InputNumber
                    placeholder="请输入翻料时间"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="干燥温度" name="dryingTemp">
                  <InputNumber
                    placeholder="请输入干燥温度"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="前模冷却" name="frontMoldCooling">
                  <Input placeholder="请输入前模冷却" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>零件后处理</div>
                <Form.Item label="零件后处理" name="partPostTreatment">
                  <TextArea rows={2} placeholder="请输入零件后处理" />
                </Form.Item>
                <Form.Item label="产品后处理" name="productPostTreatment">
                  <TextArea rows={2} placeholder="请输入产品后处理" />
                </Form.Item>
                <Form.Item label="加热温度" name="heatingTemp">
                  <InputNumber
                    placeholder="请输入加热温度"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="保温温度" name="holdingTemp">
                  <InputNumber
                    placeholder="请输入保温温度"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="干燥时间" name="dryingTime">
                  <InputNumber
                    placeholder="请输入干燥时间"
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="后模冷却" name="rearMoldCooling">
                  <Input placeholder="请输入后模冷却" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* 工序内容和品质检查 */}
          <Card title="工序内容和品质检查" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="工序内容" name="processContent">
                  <TextArea
                    rows={6}
                    placeholder="请输入工序内容"
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="品质检查" name="qualityInspection">
                  <TextArea
                    rows={6}
                    placeholder="请输入品质检查"
                  />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item label="模具及注塑工艺综合评估" name="comprehensiveAssessment">
                  <TextArea
                    rows={3}
                    placeholder="请输入综合评估"
                  />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* 备注 */}
          <Card title="备注" size="small">
            <Form.Item name="remark">
              <TextArea
                rows={3}
                placeholder="请输入备注"
              />
            </Form.Item>
          </Card>
        </Form>
      </Card>
    </div>
  );
};

export default ProcessFileForm;
