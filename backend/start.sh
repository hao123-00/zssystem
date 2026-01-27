#!/bin/bash

# 后端服务启动脚本
# 自动处理端口冲突问题

echo "正在检查端口8080..."

# 查找并停止占用8080端口的进程
PORT_PID=$(lsof -ti:8080 2>/dev/null)
if [ ! -z "$PORT_PID" ]; then
    echo "发现端口8080被进程 $PORT_PID 占用，正在停止..."
    kill $PORT_PID 2>/dev/null
    sleep 2
    
    # 如果进程仍在运行，强制终止
    if lsof -ti:8080 >/dev/null 2>&1; then
        echo "强制终止进程..."
        kill -9 $PORT_PID 2>/dev/null
        sleep 1
    fi
fi

# 停止所有相关的Maven进程
MAVEN_PIDS=$(ps aux | grep "spring-boot:run" | grep -v grep | awk '{print $2}')
if [ ! -z "$MAVEN_PIDS" ]; then
    echo "发现Maven进程，正在停止..."
    echo $MAVEN_PIDS | xargs kill 2>/dev/null
    sleep 1
fi

# 确认端口已释放
if lsof -ti:8080 >/dev/null 2>&1; then
    echo "警告: 端口8080仍被占用，请手动检查"
    exit 1
fi

echo "端口8080已释放，正在启动后端服务..."
cd "$(dirname "$0")"
mvn spring-boot:run -DskipTests
