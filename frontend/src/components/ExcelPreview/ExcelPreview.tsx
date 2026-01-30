import React, { useState, useEffect } from 'react';
import { Modal, Spin, Tabs, Table, Empty, message } from 'antd';
import * as XLSX from 'xlsx';
import './ExcelPreview.less';

interface ExcelPreviewProps {
  visible: boolean;
  onClose: () => void;
  fileBlob?: Blob | null;
  fileName?: string;
}

interface SheetData {
  name: string;
  data: any[][];
  columns: { title: string; dataIndex: string; key: string; width?: number }[];
}

/**
 * Excel 文件预览组件
 * 支持多 Sheet 页显示
 */
const ExcelPreview: React.FC<ExcelPreviewProps> = ({
  visible,
  onClose,
  fileBlob,
  fileName = 'Excel文件',
}) => {
  const [loading, setLoading] = useState(false);
  const [sheets, setSheets] = useState<SheetData[]>([]);
  const [activeSheet, setActiveSheet] = useState<string>('');

  useEffect(() => {
    if (visible && fileBlob) {
      parseExcel(fileBlob);
    }
  }, [visible, fileBlob]);

  const parseExcel = async (blob: Blob) => {
    setLoading(true);
    try {
      const arrayBuffer = await blob.arrayBuffer();
      const workbook = XLSX.read(arrayBuffer, { type: 'array' });

      const parsedSheets: SheetData[] = [];

      workbook.SheetNames.forEach((sheetName) => {
        const worksheet = workbook.Sheets[sheetName];
        // 转换为二维数组，保留空单元格
        const jsonData: any[][] = XLSX.utils.sheet_to_json(worksheet, {
          header: 1,
          defval: '',
          blankrows: true,
        });

        if (jsonData.length === 0) {
          parsedSheets.push({
            name: sheetName,
            data: [],
            columns: [],
          });
          return;
        }

        // 获取最大列数
        const maxCols = Math.max(...jsonData.map((row) => row.length));

        // 生成列配置（使用 Excel 的 A, B, C... 列名）
        const columns = Array.from({ length: maxCols }, (_, index) => {
          const colLetter = getExcelColumnName(index);
          return {
            title: colLetter,
            dataIndex: `col_${index}`,
            key: `col_${index}`,
            width: 120,
            ellipsis: true,
          };
        });

        // 转换数据格式
        const tableData = jsonData.map((row, rowIndex) => {
          const rowData: any = { key: rowIndex };
          for (let i = 0; i < maxCols; i++) {
            rowData[`col_${i}`] = row[i] !== undefined ? String(row[i]) : '';
          }
          return rowData;
        });

        parsedSheets.push({
          name: sheetName,
          data: tableData,
          columns,
        });
      });

      setSheets(parsedSheets);
      if (parsedSheets.length > 0) {
        setActiveSheet(parsedSheets[0].name);
      }
    } catch (error: any) {
      console.error('解析Excel失败:', error);
      message.error('解析Excel文件失败: ' + (error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  };

  // 获取 Excel 列名（A, B, C, ..., Z, AA, AB, ...）
  const getExcelColumnName = (index: number): string => {
    let name = '';
    let num = index;
    while (num >= 0) {
      name = String.fromCharCode((num % 26) + 65) + name;
      num = Math.floor(num / 26) - 1;
    }
    return name;
  };

  const renderSheetContent = (sheet: SheetData) => {
    if (sheet.data.length === 0) {
      return <Empty description="该工作表为空" />;
    }

    return (
      <div className="excel-table-container">
        <Table
          columns={sheet.columns}
          dataSource={sheet.data}
          pagination={false}
          scroll={{ x: 'max-content', y: 500 }}
          size="small"
          bordered
          rowClassName={(_, index) => (index === 0 ? 'excel-header-row' : '')}
        />
      </div>
    );
  };

  return (
    <Modal
      title={`预览: ${fileName}`}
      open={visible}
      onCancel={onClose}
      footer={null}
      width="90%"
      style={{ top: 20 }}
      styles={{ body: { padding: '12px 24px', maxHeight: 'calc(100vh - 120px)', overflow: 'auto' } }}
      destroyOnClose
    >
      <Spin spinning={loading} tip="正在解析Excel文件...">
        {sheets.length === 0 && !loading ? (
          <Empty description="无法解析Excel文件内容" />
        ) : sheets.length === 1 ? (
          // 只有一个 Sheet 时直接显示内容
          renderSheetContent(sheets[0])
        ) : (
          // 多个 Sheet 时使用 Tabs
          <Tabs
            activeKey={activeSheet}
            onChange={setActiveSheet}
            items={sheets.map((sheet) => ({
              key: sheet.name,
              label: sheet.name,
              children: renderSheetContent(sheet),
            }))}
          />
        )}
      </Spin>
    </Modal>
  );
};

export default ExcelPreview;
