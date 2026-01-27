/**
 * 文件处理工具函数
 */

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
};

/**
 * 格式化版本号
 */
export const formatVersion = (version: number): string => {
  return `V${version}.0`;
};

/**
 * 验证Excel文件
 */
export const validateExcelFile = (file: File): { valid: boolean; message?: string } => {
  const isExcel = file.name.endsWith('.xls') || file.name.endsWith('.xlsx');
  if (!isExcel) {
    return { valid: false, message: '只支持Excel文件格式（.xls, .xlsx）' };
  }

  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    return { valid: false, message: '文件大小不能超过10MB' };
  }

  return { valid: true };
};

/**
 * 下载文件
 */
export const downloadFile = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
};

/**
 * 获取文件扩展名
 */
export const getFileExtension = (filename: string): string => {
  if (!filename || filename.lastIndexOf('.') === -1) {
    return '';
  }
  return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
};

/**
 * 检查是否为Excel文件
 */
export const isExcelFile = (filename: string): boolean => {
  const ext = getFileExtension(filename);
  return ext === 'xls' || ext === 'xlsx';
};
