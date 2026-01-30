import { useState, useEffect, useCallback } from 'react';

// 响应式断点定义
export const BREAKPOINTS = {
  mobile: 768,
  tablet: 1024,
} as const;

export interface ResponsiveState {
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
  width: number;
}

/**
 * 响应式 Hook
 * 监听窗口大小变化，返回当前设备类型
 */
export const useResponsive = (): ResponsiveState => {
  const getResponsiveState = useCallback((): ResponsiveState => {
    const width = window.innerWidth;
    return {
      isMobile: width <= BREAKPOINTS.mobile,
      isTablet: width > BREAKPOINTS.mobile && width <= BREAKPOINTS.tablet,
      isDesktop: width > BREAKPOINTS.tablet,
      width,
    };
  }, []);

  const [state, setState] = useState<ResponsiveState>(getResponsiveState);

  useEffect(() => {
    let timeoutId: ReturnType<typeof setTimeout>;

    const handleResize = () => {
      // 防抖处理，避免频繁更新
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        setState(getResponsiveState());
      }, 100);
    };

    window.addEventListener('resize', handleResize);

    // 初始化时也调用一次
    setState(getResponsiveState());

    return () => {
      window.removeEventListener('resize', handleResize);
      clearTimeout(timeoutId);
    };
  }, [getResponsiveState]);

  return state;
};

/**
 * 简化版本，只返回是否为移动端
 */
export const useIsMobile = (): boolean => {
  const { isMobile } = useResponsive();
  return isMobile;
};

export default useResponsive;
