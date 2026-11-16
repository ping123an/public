# 个人健康管理系统（Health Manager）

轻量级个人健康管理系统，采用前后端分离架构。**十个阶段已全部完成**：项目初始化、用户登录与权限管理、个人健康档案管理、健康记录管理、体重管理、运动管理、饮水管理、健康数据统计与可视化、个人中心与系统完善、整体测试与项目整理。系统已通过全量接口测试与数据隔离验证，具备完整演示能力。

## 功能一览

| 模块 | 页面 | 核心功能 |
| ---- | ---- | ---- |
| 用户认证 | 登录页 / 注册页 | 注册（账号唯一 + BCrypt 加密）、登录（JWT）、退出、路由守卫 |
| 首页概览 | `/dashboard` | 今日体重/运动/饮水数据、目标完成进度、统计中心入口 |
| 健康档案 | `/profile` | 基本信息（性别/生日/身高）+ 健康目标（目标体重/饮水目标/运动目标） |
| 体重管理 | `/weight` | 记录 CRUD + 分页 + 日期筛选 + 趋势折线图（目标参考线）+ 变化统计 |
| 运动管理 | `/sport` | 记录 CRUD（类型/时长/热量）+ 双轴柱状图 + 类型分布饼图 + 汇总统计 |
| 饮水管理 | `/water` | 记录 CRUD + 目标设置 + 完成进度环 + 每日饮水量柱状图（达标高亮） |
| 统计中心 | `/statistics` | 今日/近7天/近30天汇总对比、三大图表、目标完成进度（7/30/90 天切换） |
| 个人中心 | `/user-center` | 头像上传（canvas 压缩）/移除、昵称邮箱修改、修改密码、退出登录 |

**安全特性**：JWT 认证拦截（无 Token 返回 401）、用户数据强制隔离（userId 一律取自 JWT，越权返回 404）、BCrypt 密码加密、参数校验与全局异常处理。

## 项目组成

| 目录 | 说明 | 技术栈 |
| ---- | ---- | ------ |
| `health-management-frontend/` | 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios + ECharts |
| `health-management-backend/` | 后端 | Java 17 + Spring Boot 3 + MyBatis-Plus + MySQL 8 + JWT + SpringDoc |

## 已完成内容

### 第一阶段：项目初始化

1. ✅ 初始化前端项目（Vue 3 + TS + Vite）并安装依赖
2. ✅ 初始化后端 Maven 项目（Spring Boot 3）
3. ✅ 创建前后端目录结构（controller/service/mapper/entity/dto/vo/config 等）
4. ✅ 配置开发环境（Vite 代理、路径别名、环境变量、CORS、Swagger）
5. ✅ 创建基础路由和页面框架
6. ✅ 编写数据库初始化脚本（5 张核心表）并完成数据库初始化
7. ✅ 统一响应格式 Result、全局异常处理、JWT 工具类、健康检查接口
8. ✅ 前后端通信联调验证通过

### 第二阶段：用户登录与权限管理

1. ✅ 用户注册（账号唯一校验、BCrypt 密码加密存储）
2. ✅ 用户登录（密码校验、签发 JWT Token）
3. ✅ JWT 身份认证拦截器（白名单放行、Token 解析注入用户上下文）
4. ✅ 登录状态管理（Pinia + localStorage 持久化）
5. ✅ 退出登录（前端清除 Token + 后端无状态接口）
6. ✅ 路由权限控制（未登录跳转登录页、已登录访问登录/注册页跳首页）
7. ✅ 用户数据隔离（用户身份从 JWT 获取，禁止前端传入 userId 决定数据归属）
8. ✅ 健康档案接口作为数据隔离验证（每个用户仅可读写自己的档案）
9. ✅ Token 过期处理（HTTP 401 + 前端自动跳转登录）

### 第三阶段：个人健康档案管理

1. ✅ 查看个人健康档案（基本信息 + 健康目标分区展示，含空状态）
2. ✅ 修改个人基本信息（性别、出生日期、身高）
3. ✅ 健康目标设置（目标体重、每日饮水目标、每周运动目标，含默认值 2000ml/150min）
4. ✅ 个人中心：修改昵称/邮箱（`PUT /auth/profile`）、修改登录密码
5. ✅ 健康档案数据持久化到 MySQL
6. ✅ 不同用户数据相互隔离（身份取自 JWT，忽略前端传入的 userId）
7. ✅ 数据库增量迁移脚本（幂等，升级不丢数据）

### 第四阶段：健康记录管理

1. ✅ 体重/运动/饮水三类记录完整 CRUD（新增、修改、删除、详情、分页列表）
2. ✅ MyBatis-Plus 分页插件 + 通用分页响应 `PageVO` / 查询参数 `RecordQueryDTO`
3. ✅ 按日期区间筛选记录（开始/结束日期，参数校验）
4. ✅ 分页查询（页码 + 每页条数，最多 100 条/页，按记录日期倒序）
5. ✅ 数据校验与异常处理（体重/时长/饮水量的取值范围、日期区间合法性、404/400 响应）
6. ✅ 数据与当前登录用户强关联：所有读写以 JWT 中的 userId 为条件，越权访问返回 404
7. ✅ 前端三个记录页：日期筛选 + 分页表格 + 新增/编辑弹窗 + 删除确认 + 详情弹窗；运动页含类型下拉、饮水页含快捷水量
8. ✅ 操作后数据库数据正确更新（接口回读 + 数据库直查一致）

### 第五阶段：体重管理

1. ✅ 体重趋势分析接口 `GET /records/weight/trend`：按日期聚合（同日取最新）+ 变化统计
2. ✅ 体重变化计算：最新/上次变化值与变化率、区间最高/最低/平均、累计变化、距目标差值（目标来自健康档案）
3. ✅ 时间范围支持近 7/30/90 天与自定义日期区间
4. ✅ ECharts 体重变化趋势折线图（含目标体重参考线、Tooltip 显示距目标）
5. ✅ 趋势页统计卡片（最新/较上次/平均/最高最低/累计变化/距目标），CRUD 后自动刷新
6. ✅ 按需引入 ECharts 组件，减小打包体积

### 第六阶段：运动管理

1. ✅ 运动记录增加消耗热量字段（数据库幂等迁移 `migrate_v3_sport_calories.sql` + 后端 + 前端）
2. ✅ 运动统计接口 `GET /records/sport/stats`：按日期聚合（每日总时长/总热量/次数/运动类型）+ 区间汇总（总时长/总热量/总次数/活跃天数/日均时长/最常运动）
3. ✅ 时间范围支持近 7/30/90 天与自定义日期区间
4. ✅ ECharts 运动图表：每日时长与热量双轴柱状图 + 运动类型分布饼图
5. ✅ 运动页统计卡片 + 热量录入（支持按类型与时长自动估算）；CRUD 后自动刷新统计

### 第七阶段：饮水管理

1. ✅ 每日饮水目标设置接口 `PUT /records/water/goal`（只更新 `daily_water_goal`，不影响档案其他字段，无档案时自动创建）
2. ✅ 饮水统计接口 `GET /records/water/stats`：每日总量/次数/完成率/是否达标 + 汇总（总量/有记录天数/日均/达标天数/目标值）
3. ✅ 时间范围支持近 7/30/90 天与自定义日期区间
4. ✅ 饮水页目标完成进度环（ECharts 仪表盘）+ 每日饮水量柱状图（目标参考线、达标高亮）
5. ✅ 目标设置弹窗 + 统计卡片；CRUD 与目标变更后自动刷新，完成率实时重算

### 第八阶段：健康数据统计与可视化

1. ✅ 统计中心聚合接口 `GET /statistics/overview?days=7`：一次返回今日汇总 + 近7天 + 近30天 + 目标完成 + 图表数据
2. ✅ 聚合层复用体重/运动/饮水各模块的 trend/stats 方法，统计口径与数据库查询完全一致
3. ✅ 统计中心页 `/statistics`：今日数据卡片、三类健康目标完成进度、近7天/近30天周期对比表
4. ✅ ECharts 三类图表：体重趋势折线图（目标线）、运动时长柱状图、饮水量柱状图（达标高亮+目标线），支持 7/30/90 天切换
5. ✅ 首页 Dashboard 接入真实今日数据（体重/运动/饮水）与目标完成进度，移除占位组件，提供统计中心入口

### 第九阶段：个人中心与系统完善

1. ✅ 头像管理：`PUT /auth/avatar`（base64 DataURL，格式/大小校验）、`DELETE /auth/avatar`，`sys_user` 增加 `avatar` 字段（幂等迁移 `migrate_v4_user_avatar.sql`）
2. ✅ 个人中心头像上传（canvas 压缩为 160×160 JPEG）、预览、移除；顶栏与主布局展示头像
3. ✅ 个人资料管理、修改密码、登录退出流程完善
4. ✅ 页面加载状态（首页/个人中心/统计中心）、空数据提示、404 兜底页
5. ✅ 全局异常处理增强：JSON 格式错误、参数类型错误、缺少参数、数据冲突均返回明确提示（code 400）
6. ✅ 统一操作反馈：请求拦截器统一错误提示，避免页面重复弹窗

### 第十阶段：整体测试与项目整理

1. ✅ 全量接口测试：20 个接口（认证 8 + 档案 2 + 三类记录 16 项操作 + 统计 1 + 健康检查）逐一验证通过
2. ✅ 用户权限测试：无 Token 访问返回 401；伪造 Token 返回 401；错误密码/重复注册返回 400 明确提示
3. ✅ 数据隔离测试：carol 无法读取/修改/删除 bob 的记录（均返回 404），列表数据互不可见
4. ✅ 核心业务流程测试：注册 → 登录 → 档案与目标设置 → 三类记录录入 → 趋势/统计 → 改密码 → 登出，全链路通过
5. ✅ 前后端构建验证：后端 `mvn clean package` 成功；前端 `npm run build` 成功无错误
6. ✅ 数据库 SQL 整理：`init.sql` 与线上库结构逐字段核对一致，临时库全新安装验证一次成功（5 张表）
7. ✅ Swagger 接口文档完善：全局 JWT 认证支持（右上角 Authorize 按钮可直接填 Token 调试）、文档说明与演示账号
8. ✅ 项目启动说明与功能说明整理（本 README）

## 快速启动

### 前置条件

- Node.js 18+、npm
- JDK 17、Maven 3.6+
- MySQL 8.0（服务运行中，root 密码 123456，与后端配置默认值一致）

### 1. 初始化数据库

首次安装执行初始化脚本（含全部字段，可一次安装成功）：

```bash
mysql -u root -p < health-management-backend/src/main/resources/sql/init.sql
```

已从旧版本升级、已有 `health_manager` 库时，执行增量迁移脚本（幂等、保留数据）：

```bash
# V2：健康目标字段（第三阶段）
mysql -u root -p < health-management-backend/src/main/resources/sql/migrate_v2_health_goal.sql
# V3：运动消耗热量字段（第六阶段）
mysql -u root -p < health-management-backend/src/main/resources/sql/migrate_v3_sport_calories.sql
# V4：用户头像字段（第九阶段）
mysql -u root -p < health-management-backend/src/main/resources/sql/migrate_v4_user_avatar.sql
```

### 2. 启动后端

```bash
cd health-management-backend
mvn clean package -DskipTests
java -jar target/health-management-backend-0.0.1-SNAPSHOT.jar
```

后端启动后：`http://localhost:8080/api`
- 健康检查：`http://localhost:8080/api/health/ping`
- Swagger 接口文档：`http://localhost:8080/api/swagger-ui/index.html`

**Swagger 调试方法**：打开 Swagger 页面 → 先调用 `POST /auth/login`（演示账号 `carol / 123456`）→ 复制返回的 `token` → 点击页面右上角 **Authorize** 按钮 → 粘贴 token（无需 Bearer 前缀）→ 即可直接调试全部受保护接口。

### 3. 启动前端

```bash
cd health-management-frontend
npm install
npm run dev
```

前端启动后：`http://localhost:5173`

开发环境下前端通过 Vite 代理将 `/api` 请求转发到后端 `http://localhost:8080`，无需额外配置跨域。

### 演示流程（5 分钟）

1. 访问 `http://localhost:5173`，未登录自动跳转登录页
2. 注册新账号（或直接用演示账号 `carol / 123456` 登录）
3. 健康档案页：设置身高、目标体重、饮水/运动目标
4. 依次在体重/运动/饮水页录入几条记录，观察趋势图、统计图、进度环实时更新
5. 统计中心查看今日/近7天/近30天汇总与三大图表
6. 个人中心上传头像、修改昵称、修改密码
7. 退出登录，换 `bob / 123456` 登录，验证看不到 carol 的任何数据（数据隔离）

## 主要接口

| 方法 | 路径 | 说明 | 需认证 |
| ---- | ---- | ---- | ---- |
| POST | `/auth/register` | 用户注册 | 否 |
| POST | `/auth/login` | 用户登录，返回 Token | 否 |
| GET | `/auth/me` | 获取当前用户信息 | 是 |
| PUT | `/auth/profile` | 修改个人资料（昵称、邮箱） | 是 |
| PUT | `/auth/avatar` | 设置头像（base64 DataURL） | 是 |
| DELETE | `/auth/avatar` | 清除头像 | 是 |
| PUT | `/auth/password` | 修改密码 | 是 |
| POST | `/auth/logout` | 退出登录 | 是 |
| GET | `/profile` | 获取当前用户健康档案（含健康目标） | 是 |
| PUT | `/profile` | 保存当前用户健康档案（含健康目标） | 是 |
| POST | `/records/weight` | 新增体重记录 | 是 |
| PUT | `/records/weight/{id}` | 修改体重记录 | 是 |
| DELETE | `/records/weight/{id}` | 删除体重记录 | 是 |
| GET | `/records/weight/{id}` | 体重记录详情 | 是 |
| GET | `/records/weight` | 体重记录分页列表（`current/size/startDate/endDate`） | 是 |
| GET | `/records/weight/trend` | 体重趋势分析（`days` 或 `startDate/endDate`） | 是 |
| POST | `/records/sport` | 新增运动记录 | 是 |
| PUT | `/records/sport/{id}` | 修改运动记录 | 是 |
| DELETE | `/records/sport/{id}` | 删除运动记录 | 是 |
| GET | `/records/sport/{id}` | 运动记录详情 | 是 |
| GET | `/records/sport` | 运动记录分页列表（支持日期区间） | 是 |
| GET | `/records/sport/stats` | 运动统计（每日明细 + 区间汇总，`days` 或日期区间） | 是 |
| POST | `/records/water` | 新增饮水记录 | 是 |
| PUT | `/records/water/{id}` | 修改饮水记录 | 是 |
| DELETE | `/records/water/{id}` | 删除饮水记录 | 是 |
| GET | `/records/water/{id}` | 饮水记录详情 | 是 |
| GET | `/records/water` | 饮水记录分页列表（支持日期区间） | 是 |
| PUT | `/records/water/goal` | 设置每日饮水目标 | 是 |
| GET | `/records/water/stats` | 饮水统计（每日明细 + 完成率 + 汇总） | 是 |
| GET | `/statistics/overview` | 统计中心聚合概览（`days` 图表范围，默认 7） | 是 |
| GET | `/health/ping` | 健康检查 | 否 |

三类记录接口结构一致（`weight` / `sport` / `water`），分页查询参数：`current`（页码，从 1 开始）、`size`（每页条数，1~100）、`startDate`、`endDate`（均可选，格式 `yyyy-MM-dd`）。认证方式：请求头携带 `Authorization: Bearer <token>`。

## 验收检查

- 前端首页：`http://localhost:5173/` （未登录自动跳转登录页）
- 登录页 `/login`、注册页 `/register`、健康档案 `/profile`、个人中心 `/user-center`
- 记录页 `/weight`、`/sport`、`/water`（CRUD + 分页 + 日期筛选）
- 后端健康检查：`http://localhost:8080/api/health/ping` （返回 `code:200`，无需 Token）
- 认证拦截：不带 Token 访问 `/auth/me`、`/records/*` 返回 HTTP 401
- 档案查看/修改/目标设置：保存后重新查询及数据库直查均一致
- 记录 CRUD：新增/修改/删除后接口回读与数据库直查一致；分页 total/pages 正确、按日期倒序
- 日期筛选：区间查询返回正确条数；起止日期颠倒返回 code 400
- 数据隔离：用户 A 无法读取/修改/删除用户 B 的记录（返回 404），列表互不可见
- Token 过期：过期 Token 返回 HTTP 401，前端自动跳转登录
- 体重趋势：`/records/weight/trend` 按日期聚合（同日取最新），变化值/变化率/最高最低/平均/累计/距目标计算正确
- 趋势时间范围：days 与自定义日期区间均生效；日期颠倒返回 code 400；不同用户趋势数据隔离
- 运动统计：`/records/sport/stats` 每日时长/热量/次数/类型聚合正确，汇总（总时长/总热量/次数/活跃天数/日均/最常运动）计算正确
- 热量字段：录入与修改后统计联动更新；热量为负返回 code 400；不同用户统计数据隔离
- 饮水目标：`PUT /records/water/goal` 只改目标不影响档案其他字段；目标 200~10000ml 校验；不同用户目标独立
- 饮水统计：每日总量/完成率/达标判定准确，目标变化时完成率实时重算；汇总计算正确；数据隔离
- 统计中心：`/statistics/overview` 今日/7天/30天/目标完成各指标与数据库直查一致
- 图表数据源正确、7/30/90 天切换生效；不同用户统计数据完全隔离（bob 聚合为空）；未登录 401
- 头像：上传后回读一致、非法格式返回 400、清除后为 null；不同用户头像独立
- 异常处理：JSON 格式错误/参数类型错误/缺少参数均返回 code 400 明确提示（非 500）；404 兜底页正常

## 第十阶段测试结果（2026-11-16）

- **接口测试**：20 个接口全部通过（含正常流程与异常分支：错误密码、重复注册、参数缺失、非法值、日期格式错误、伪造 Token）
- **权限测试**：无 Token 访问受保护接口 → HTTP 401；伪造 Token → HTTP 401；旧密码改密 → 400
- **数据隔离**：carol 读取/修改/删除 bob 的记录均返回 404，列表互不可见，bob 自身访问正常
- **业务流程**：注册 → 登录 → 档案 → 三类记录 → 趋势/统计 → 改密码 → 登出，全链路一次通过
- **数据库**：init.sql 全新安装在临时库一次成功（5 张表），与线上库逐字段一致
- **构建**：后端 jar 打包成功；前端 production build 成功
- **测试数据**：测试用户已清理；carol 保留少量演示数据（3 条体重、1 条运动、1 条饮水）

## 后续可扩展方向

十个阶段已全部完成，系统功能完整、测试通过、具备完整演示能力。后续可考虑：单元测试与集成测试覆盖、数据导出（Excel/CSV）、 BMI 自动计算、饮食记录模块、部署上线（Nginx + Docker）等。
