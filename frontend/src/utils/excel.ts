import axios from 'axios';
import { message } from 'antd';
import { getToken } from './auth';

/**
 * 导出Excel文件
 * @param url 导出接口URL
 * @param params 查询参数
 * @param filename 文件名（不含扩展名）
 */
export const exportExcel = async (url: string, params: any, filename: string) => {
  try {
    const token = getToken();
    
    // 处理数组参数：将数组转换为Spring Boot可以识别的格式
    const processedParams: any = { ...params };
    if (params.dateList && Array.isArray(params.dateList)) {
      // Spring Boot GET请求中，数组参数格式为：dateList=value1&dateList=value2
      processedParams.dateList = params.dateList;
    }
    
    const response = await axios.get(url, {
      params: processedParams,
      paramsSerializer: {
        indexes: null, // 使用重复的键名格式：dateList=value1&dateList=value2
      },
      responseType: 'blob',
      headers: {
        Authorization: token ? `Bearer ${token}` : undefined,
      },
    });
    
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = `${filename}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
    
    message.success('导出成功');
  } catch (error: any) {
    message.error('导出失败：' + (error.message || '未知错误'));
  }
};
