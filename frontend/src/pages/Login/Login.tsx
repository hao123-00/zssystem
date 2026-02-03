import React, { useEffect, useState } from 'react';
import { Form, Input, Button, Checkbox, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { login, LoginParams } from '@/api/auth';
import { setToken, setRefreshToken, setUserInfo, isAuthenticated } from '@/utils/auth';
import bg1 from '@/assets/login/bg1.png';
import bg2 from '@/assets/login/bg2.png';
import './Login.less';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [currentBg, setCurrentBg] = useState(0);
  const backgrounds = [bg1, bg2];

  useEffect(() => {
    // 如果已登录，跳转到首页
    if (isAuthenticated()) {
      navigate('/');
    }
  }, [navigate]);

  useEffect(() => {
    // 背景图片轮播
    const timer = setInterval(() => {
      setCurrentBg((prev) => (prev + 1) % backgrounds.length);
    }, 3000);
    return () => clearInterval(timer);
  }, []);

  const onFinish = async (values: LoginParams) => {
    try {
      const result = await login(values);
      setToken(result.token);
      setRefreshToken(result.refreshToken);
      setUserInfo({
        userId: result.userId,
        username: result.username,
        realName: result.realName,
      });
      message.success('登录成功');
      navigate('/');
    } catch (error: any) {
      message.error(error.message || '登录失败');
    }
  };

  return (
    <div className="login-container">
      {backgrounds.map((bg, index) => (
        <div
          key={index}
          className={`login-bg-slide ${index === currentBg ? 'active' : ''}`}
          style={{ backgroundImage: `url(${bg})` }}
        />
      ))}
      <div className="login-box">
        <h1 className="login-title">注塑部管理系统</h1>
        <Form
          form={form}
          name="login"
          onFinish={onFinish}
          autoComplete="off"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              size="large"
            />
          </Form.Item>

          <Form.Item name="rememberMe" valuePropName="checked">
            <Checkbox>记住我</Checkbox>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">
              登录
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
};

export default Login;
