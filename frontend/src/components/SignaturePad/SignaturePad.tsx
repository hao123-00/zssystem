import React, { useRef, useEffect, useState } from 'react';
import { Button, Space, message } from 'antd';
import { ReloadOutlined, CheckOutlined } from '@ant-design/icons';
import './SignaturePad.less';

interface SignaturePadProps {
  onConfirm: (signatureData: string) => void;
  onCancel?: () => void;
  width?: number;
  height?: number;
}

/**
 * 电子签名画板组件
 */
const SignaturePad: React.FC<SignaturePadProps> = ({
  onConfirm,
  onCancel,
  width = 600,
  height = 300,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [hasSignature, setHasSignature] = useState(false);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // 设置画布样式
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    // 清空画布，设置白色背景
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
  }, []);

  const startDrawing = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    setIsDrawing(true);
    const rect = canvas.getBoundingClientRect();
    
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;
    
    ctx.beginPath();
    ctx.moveTo(clientX - rect.left, clientY - rect.top);
  };

  const draw = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
    if (!isDrawing) return;

    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const rect = canvas.getBoundingClientRect();
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;
    
    ctx.lineTo(clientX - rect.left, clientY - rect.top);
    ctx.stroke();
    setHasSignature(true);
  };

  const stopDrawing = () => {
    setIsDrawing(false);
  };

  const clearSignature = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    setHasSignature(false);
  };

  const confirmSignature = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    if (!hasSignature) {
      message.warning('请先进行签名');
      return;
    }

    // 将画布内容转换为Base64图片
    const signatureData = canvas.toDataURL('image/png');
    onConfirm(signatureData);
  };

  return (
    <div className="signature-pad-container">
      <div className="signature-pad-header">
        <span>请在下方区域进行签名</span>
      </div>
      <div className="signature-pad-wrapper">
        <canvas
          ref={canvasRef}
          width={width}
          height={height}
          className="signature-canvas"
          onMouseDown={startDrawing}
          onMouseMove={draw}
          onMouseUp={stopDrawing}
          onMouseLeave={stopDrawing}
          onTouchStart={startDrawing}
          onTouchMove={draw}
          onTouchEnd={stopDrawing}
        />
      </div>
      <div className="signature-pad-actions">
        <Space>
          <Button icon={<ReloadOutlined />} onClick={clearSignature}>
            清除重签
          </Button>
          {onCancel && (
            <Button onClick={onCancel}>
              取消
            </Button>
          )}
          <Button type="primary" icon={<CheckOutlined />} onClick={confirmSignature}>
            确认签名
          </Button>
        </Space>
      </div>
    </div>
  );
};

export default SignaturePad;
