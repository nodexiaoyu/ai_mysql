# AI MySQL 应用远程部署指南

## 前提条件
- 安装 [Docker](https://www.docker.com/get-started)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

## 部署步骤

### 1. 准备文件
在服务器上创建一个目录，并将以下文件放入该目录：
- `docker-compose-pull.yml` - Docker Compose 配置文件
- `init.sql` - 数据库初始化脚本

### 2. 启动容器
在包含上述文件的目录中运行：
```bash
docker-compose -f docker-compose-pull.yml up -d
```

这会自动从 Docker Hub 拉取镜像并启动容器。

### 3. 检查容器状态
```bash
docker-compose -f docker-compose-pull.yml ps
```

### 4. 访问应用
应用将在以下地址运行：
- 应用地址: http://[服务器IP]:8090/api

### 数据库连接信息
- 主机: localhost (或服务器IP)
- 端口: 3307
- 用户名: root
- 密码: root
- 数据库: ai_mysql

## 数据持久化
MySQL 数据存储在名为 `ai-mysql-volume` 的 Docker 卷中，即使容器被删除，数据也不会丢失。

## 停止服务
```bash
docker-compose -f docker-compose-pull.yml down
```

## 完全删除服务及数据
```bash
docker-compose -f docker-compose-pull.yml down -v
``` 