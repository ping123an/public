# Tech Design

# **个人健康管理系统技术设计**

## **技术栈**

- 前端：Vue 3 + TypeScript + Vite
- UI 组件库：Element Plus
- 样式：CSS3
- 状态管理：Pinia
- 路由管理：Vue Router
- 数据可视化：ECharts
- HTTP 请求：Axios
- 后端：Java 17 + Spring Boot 3
- ORM 框架：MyBatis-Plus
- 数据存储：MySQL 8.0
- 身份认证：JWT
- 参数校验：Spring Validation
- 项目构建：Maven
- 接口文档：SpringDoc + Swagger UI
- 部署：Docker（可选）

## **项目结构**

### **前端项目**

```
health-management-frontend/
├── public/
├── src/
│   ├── api/              # 后端接口请求
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── layouts/          # 页面布局
│   ├── pages/            # 页面
│   │   ├── login/        # 登录注册
│   │   ├── dashboard/    # 健康数据首页
│   │   ├── profile/      # 个人健康档案
│   │   ├── weight/       # 体重管理
│   │   ├── sport/        # 运动管理
│   │   ├── water/        # 饮水管理
│   │   └── statistics/   # 数据统计
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── utils/            # 工具函数
│   ├── types/            # TypeScript 类型定义
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
└── vite.config.ts
```

### **后端项目**

```
health-management-backend/
├── src/main/java/com/health/
│   ├── controller/       # 接口控制层
│   ├── service/          # 业务逻辑层
│   │   └── impl/         # 业务实现
│   ├── mapper/           # MyBatis-Plus 数据访问层
│   ├── entity/           # 数据库实体类
│   ├── dto/              # 请求参数对象
│   ├── vo/               # 响应数据对象
│   ├── config/           # 配置类
│   ├── common/            # 统一返回结果、常量
│   ├── exception/        # 全局异常处理
│   ├── interceptor/      # JWT 拦截器
│   ├── utils/            # 工具类
│   └── HealthApplication.java
├── src/main/resources/
│   ├── mapper/           # XML 映射文件（需要时使用）
│   ├── application.yml   # 项目配置
│   └── sql/              # 数据库初始化脚本
├── pom.xml
└── README.md
```

## **数据模型**

### **1. User（用户）**

- id: Long
- username: String
- password: String
- nickname: String
- email: String
- createTime: LocalDateTime
- updateTime: LocalDateTime

### **2. HealthProfile（健康档案）**

- id: Long
- userId: Long
- gender: Integer
- birthday: LocalDate
- height: BigDecimal
- createTime: LocalDateTime
- updateTime: LocalDateTime

### **3. WeightRecord（体重记录）**

- id: Long
- userId: Long
- weight: BigDecimal
- recordDate: LocalDate
- remark: String
- createTime: LocalDateTime

### **4. SportRecord（运动记录）**

- id: Long
- userId: Long
- sportType: String
- duration: Integer
- recordDate: LocalDate
- remark: String
- createTime: LocalDateTime

### **5. WaterRecord（饮水记录）**

- id: Long
- userId: Long
- amount: Integer
- recordDate: LocalDate
- createTime: LocalDateTime

## **页面设计**

| 页面       | 主要功能                     |
| ---------- | ---------------------------- |
| 登录注册页 | 用户注册、登录               |
| 健康首页   | 今日健康数据概览             |
| 健康档案页 | 个人基本信息管理             |
| 体重管理页 | 体重记录增删改查             |
| 运动管理页 | 运动记录增删改查             |
| 饮水管理页 | 饮水记录及每日统计           |
| 数据统计页 | 体重趋势、运动及饮水统计     |
| 个人中心页 | 个人资料、修改密码、退出登录 |

## **关键技术点**

1. **JWT 身份认证**：用户登录成功后生成 Token，通过请求头携带 Token 实现身份验证。
2. **Spring Boot 分层架构**：采用 Controller、Service、Mapper 分层设计，降低代码耦合。
3. **MyBatis-Plus**：实现健康记录的增删改查，减少重复 SQL 代码。
4. **MySQL 数据设计**：通过 user_id 关联用户和健康记录，实现不同用户的数据隔离。
5. **ECharts 数据可视化**：使用折线图展示体重变化，使用柱状图展示运动和饮水统计。
6. **Pinia 状态管理**：统一管理用户登录状态和 Token。
7. **Axios 请求封装**：统一处理请求头、Token 和接口异常。
8. **Spring Validation**：校验用户输入，例如体重范围、运动时长和饮水量。
9. **统一异常处理**：通过全局异常处理器统一返回错误信息。
10. **BCrypt 密码加密**：对用户密码进行加密存储，避免明文保存。

## **开发建议**

- 第一阶段：完成登录注册、健康档案和数据库设计。
- 第二阶段：完成体重、运动、饮水三个核心 CRUD 模块。
- 第三阶段：完成首页数据统计和 ECharts 图表。
- 第四阶段：完成接口测试、前后端联调和部署。

**技术选型原则：** 使用成熟、常见、容易上手的技术，不引入 Redis、微服务、消息队列等暂时不需要的复杂组件。对于这个项目，Vue 3 + Spring Boot + MySQL 已经足够。