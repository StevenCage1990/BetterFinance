# VibeCoding - 家庭财务管理系统

一个全栈的家庭财务管理应用，用于追踪资产、负债、收入、支出和预算执行情况。

## 功能特性

### 年度计划
- 设定年度资产目标（流动资金、固定资产、金融投资、其他资产）
- 设定年度负债目标
- 规划年度收入来源
- 设定预算分类及金额（支持月度/年度预算）

### 月度记录
- 记录每月资产明细（分组、名称、金额、收益率）
- 记录每月负债明细（名称、金额、利率）
- 记录每月收入明细
- 记录每月支出明细（可关联预算分类）
- 自动从上月复制结构和数据

### 数据看板
- 年度目标追踪：资产增值进度、负债降低进度
- 月度趋势：收入支出变化趋势图
- 预算管理：预算消耗饼图、分类消耗率明细

### 预算执行
- 预算分类执行情况追踪
- 消耗率计算与预警
- 支出明细追溯（按分类查看每笔支出）

## 技术栈

### 后端
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 Database（可切换为 MySQL/PostgreSQL）

### 前端
- React 18
- TypeScript
- Ant Design 5.x
- ECharts 5.x
- Vite

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Maven 3.6+

### 本地运行

1. **克隆项目**
```bash
git clone https://github.com/StevenCage1990/VibeCoding.git
cd VibeCoding
```

2. **启动后端**
```bash
cd finance-backend
mvn spring-boot:run
```

3. **启动前端（开发模式）**
```bash
cd finance-frontend
npm install
npm run dev
```

4. **访问应用**
- 开发模式：http://localhost:5173
- 生产模式：http://localhost:8080

### 生产构建

```bash
# 构建前端
cd finance-frontend
npm run build

# 构建后端（包含前端静态资源）
cd finance-backend
mvn clean package

# 运行
java -jar target/finance-backend-0.0.1-SNAPSHOT.jar
```

### Docker 部署

```bash
# 使用 docker-compose
docker-compose up -d

# 或手动构建
./build.sh
docker build -t finance-app ./finance-backend
docker run -p 8080:8080 -v finance-data:/app/data finance-app
```

## 项目结构

```
VibeCoding/
├── finance-backend/          # Spring Boot 后端
│   ├── src/main/java/com/finance/
│   │   ├── controller/       # REST API 控制器
│   │   ├── service/          # 业务逻辑层
│   │   ├── repository/       # 数据访问层
│   │   ├── entity/           # JPA 实体
│   │   ├── dto/              # 数据传输对象
│   │   └── enums/            # 枚举类型
│   └── src/main/resources/
│       ├── application.yml   # 应用配置
│       └── static/           # 前端构建产物
│
├── finance-frontend/         # React 前端
│   ├── src/
│   │   ├── pages/            # 页面组件
│   │   ├── components/       # 公共组件
│   │   ├── services/         # API 服务
│   │   └── types/            # TypeScript 类型定义
│   └── vite.config.ts        # Vite 配置
│
├── docker-compose.yml        # Docker 编排配置
└── build.sh                  # 构建脚本
```

## API 接口

### 年度计划
- `GET /api/annual-plan/{year}` - 获取年度计划
- `POST /api/annual-plan` - 创建/更新年度计划
- `GET /api/annual-plan/{year}/summary` - 获取年度汇总

### 月度记录
- `GET /api/monthly-record/list` - 获取月度记录列表
- `GET /api/monthly-record/{year}/{month}` - 获取指定月度记录
- `POST /api/monthly-record` - 创建月度记录
- `GET /api/monthly-record/{year}/{month}/previous` - 获取上月模板

### 数据看板
- `GET /api/dashboard/overview` - 获取总览数据
- `GET /api/dashboard/annual-target-trend/{year}` - 年度目标趋势
- `GET /api/dashboard/income-expense-trend/{year}` - 收支趋势
- `GET /api/dashboard/budget-pie/{year}` - 预算饼图数据
- `GET /api/dashboard/annual-progress/{year}` - 年度预算执行进度

### 数据导入导出
- `GET /api/data/export/annual-plan/{year}` - 导出年度计划
- `GET /api/data/export/monthly-records/{year}` - 导出月度记录
- `POST /api/data/import/annual-plan/{year}` - 导入年度计划

## 配置说明

### 数据库配置
默认使用 H2 文件数据库，数据存储在 `finance-backend/data/` 目录。

切换到 MySQL：
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/finance
    username: root
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
```

### 端口配置
```yaml
server:
  port: 8080  # 默认端口
```

## 开发指南

### 添加新的预算分类
1. 修改 `ExpenseCategory` 枚举添加新分类
2. 在年度计划页面添加对应预算项

### 添加新的资产类型
1. 修改 `AssetGroup` 枚举添加新类型
2. 更新前端 `AssetGroupLabels` 映射

## License

MIT License
