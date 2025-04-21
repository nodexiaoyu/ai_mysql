# AI MySQL Docker 部署指南

## 项目介绍
这是一个基于Spring Boot和MySQL的应用，已配置好Docker容器化部署。

## 前提条件
- 安装 [Docker](https://www.docker.com/get-started)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

## 构建与运行

### 1. 构建项目
首先使用Maven构建项目：
```bash
./mvnw clean package -DskipTests
```

### 2. 启动Docker容器
```bash
docker-compose up -d
```

### 3. 查看容器状态
```bash
docker-compose ps
```

### 4. 访问应用
应用将在以下地址运行：
- 应用地址: http://localhost:8090/api

## 数据持久化
- MySQL数据存储在名为`ai-mysql-volume`的Docker卷中
- MySQL数据库初始化脚本位于`init.sql`

## 停止容器
```bash
docker-compose down
```

## 完全删除容器和卷
```bash
docker-compose down -v
``` 