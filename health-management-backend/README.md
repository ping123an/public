# 健康管理系统 - 后端

基于 Spring Boot 3 + MyBatis-Plus + MySQL 8 的轻量级个人健康管理系统后端。

## 技术栈

- Java 17 + Spring Boot 3.3.5
- MyBatis-Plus 3.5.7（含分页插件）
- MySQL 8.0
- JWT (jjwt 0.12.6)
- BCrypt 密码加密（spring-security-crypto）
- SpringDoc + Swagger UI
- Maven

## 项目结构

```
src/main/java/com/health/
├── controller/     # 接口控制层（Auth / HealthProfile / Health / WeightRecord / SportRecord / WaterRecord Controller）
├── service/impl/   # 业务逻辑层
├── mapper/         # MyBatis-Plus 数据访问层
├── entity/         # 数据库实体类
├── dto/            # 请求参数对象
├── vo/             # 响应数据对象
├── config/         # 配置类（CORS、Swagger、WebMvc、BCrypt、MybatisPlus 分页插件）
├── common/         # 统一返回结果 Result、分页 PageVO、用户上下文 UserContext
├── exception/      # 全局异常处理
├── interceptor/    # JWT 认证拦截器
├── utils/          # 工具类（JWT）
└── HealthApplication.java
```

## 认证与数据隔离设计

- **JWT 认证**：`/auth/login` 签发 Token，其余接口需携带 `Authorization: Bearer <token>`；为便于 Swagger UI 调试，拦截器同时兼容直接传 token 值（不带 Bearer 前缀）。
- **拦截器白名单**：注册、登录、健康检查、Swagger 文档无需 Token；其余接口由 `JwtInterceptor` 校验。
- **Swagger 调试**：打开 Swagger UI 后先调用 `/auth/login` 获取 token（演示账号 `carol / 123456`），点击右上角 Authorize 按钮粘贴 token 即可调试全部受保护接口。
- **用户上下文**：校验通过后把 userId 写入 `UserContext`（ThreadLocal），请求结束清理。
- **数据隔离**：业务层一律通过 `UserContext.getUserId()` 获取身份，忽略前端传入的 userId；401/403 返回对应 HTTP 状态码，越权访问他人数据返回 404。
- **密码安全**：注册/修改密码时用 BCrypt 加密，数据库不存储明文。
- **头像**：`PUT /auth/avatar` 接收 base64 DataURL（png/jpeg/webp/gif，最大 200000 字符），存于 `sys_user.avatar`；`DELETE /auth/avatar` 用 `LambdaUpdateWrapper.set(null)` 显式置空（`updateById` 默认忽略 null 字段）。

### 全局异常处理

`GlobalExceptionHandler` 统一返回 `Result` 格式，并对常见异常给出明确提示：参数校验失败（400）、JSON 格式错误（400）、参数类型不匹配（400）、缺少必填参数（400）、数据唯一约束冲突（400）、业务异常（按 code 映射 HTTP 状态）、兜底 500。前端请求拦截器统一弹窗提示，避免页面重复提示。

## 健康档案与健康目标

- `health_profile` 表存储基本信息（性别、出生日期、身高）和健康目标（目标体重、每日饮水目标、每周运动目标）。
- `GET /profile` 与 `PUT /profile` 分别查看、保存当前用户档案；身份取自 JWT，用户之间数据隔离。
- 饮水/运动目标未提供时默认 2000ml / 150 分钟。

## 健康记录（体重/运动/饮水）

- 三张表 `weight_record`、`sport_record`、`water_record` 分别存储体重、运动、饮水记录，均带 `user_id` 与 `record_date` 索引。
- 统一接口前缀 `/records/weight`、`/records/sport`、`/records/water`，各自提供：
  - `POST /records/{type}` 新增、`PUT /records/{type}/{id}` 修改、`DELETE /records/{type}/{id}` 删除、`GET /records/{type}/{id}` 详情
  - `GET /records/{type}` 分页列表，参数 `current`、`size`、`startDate`、`endDate`（日期可选，格式 `yyyy-MM-dd`）
- 分页由 `MybatisPlusConfig` 中的 `PaginationInnerInterceptor` 实现，每页最多 100 条，结果按 `record_date`、`id` 倒序。
- 所有操作强制以 `UserContext` 中的 userId 为条件：VO 不暴露 userId，越权访问（读取/修改/删除他人记录）返回 404。
- 参数校验：体重 1~500kg、运动时长 1~1440 分钟、单次饮水 1~10000ml；日期区间颠倒返回 code 400。

### 体重趋势分析

- `GET /records/weight/trend`：返回 `{ points, stats }`。
  - 参数：`days`（近 N 天，7/30/90）或 `startDate`/`endDate` 自定义区间，均不传则查全部；days 优先。
  - `points`：按日期升序的 `{ date, weight }`，同一天多条记录取最新一条。
  - `stats`：最新/上次体重、变化值与变化率、区间最高/最低/平均、累计变化（最新-最早）、目标体重及距目标差值，目标来自当前用户健康档案；无数据时各值为 null、`count=0`。
  - 所有统计仅基于当前 JWT 用户的记录，用户之间完全隔离。

### 运动管理与统计

- `sport_record` 含 `calories`（消耗热量，千卡，默认 0），由 V3 迁移脚本 `migrate_v3_sport_calories.sql` 添加。
- `GET /records/sport/stats`：返回 `{ daily, summary }`。
  - 参数：`days`（近 N 天）或 `startDate`/`endDate` 自定义区间，均不传查全部；days 优先。
  - `daily`：按日期升序的每日统计，含当天总时长、总热量、记录条数、去重后的运动类型列表（同日多条记录合并）。
  - `summary`：区间总时长、总热量、总次数、活跃天数、平均每日时长（保留 1 位小数）、最常运动类型。
  - 热量取值范围 0~10000 千卡，负数校验失败返回 code 400。

### 饮水目标与统计

- `PUT /records/water/goal`：请求体 `{ dailyWaterGoal }`（200~10000ml），只更新当前用户 `health_profile.daily_water_goal`，不影响性别/身高/其他目标；无档案时自动创建。返回设置后的目标值。
- `GET /records/water/stats`：返回 `{ daily, summary }`。
  - 参数：`days`（近 N 天）或 `startDate`/`endDate` 自定义区间，均不传查全部；days 优先。
  - `daily`：按日期升序的每日统计，含当天总量、次数、目标完成率（%、0 位）、是否达标（总量 >= 目标）。
  - `summary`：区间总量、有记录天数、日均量（0 位）、达标天数、当前每日目标。
  - 目标来自当前用户健康档案（默认 2000ml）；所有数据按 JWT 用户隔离。

### 统计中心聚合

- `GET /statistics/overview?days=7`：一次返回完整统计概览，减少前端请求次数。
  - `today`：今日体重（取最近一次）、运动时长/热量/次数、饮水量/目标/完成率/是否达标。
  - `week` / `month`：近 7 / 30 天汇总（体重变化、运动时长热量次数、饮水总量达标天数、有记录天数）。
  - `goals`：体重/饮水/每周运动三个目标的当前值、目标值、完成率、是否达成。
  - `charts`：指定 `days` 范围（默认 7）的体重趋势、运动统计、饮水统计原始数据，供前端绘图。
  - 聚合层复用 `WeightRecordService.trend`、`SportRecordService.stats`、`WaterRecordService.stats`，口径与各模块单独查询一致；所有数据由各 Service 按 JWT 用户隔离。

## 启动步骤

### 1. 初始化数据库

MySQL 服务需处于运行状态，执行初始化脚本（会创建 `health_manager` 数据库及 5 张表）：

```bash
mysql -u root -p < src/main/resources/sql/init.sql
```

已从旧版本升级（已存在 `health_manager` 库）时，执行增量迁移脚本为 `health_profile` 增加健康目标字段（幂等、保留数据）：

```bash
mysql -u root -p < src/main/resources/sql/migrate_v2_health_goal.sql
```

升级到含运动热量的版本时，执行 V3 迁移脚本（幂等、保留数据）：

```bash
mysql -u root -p < src/main/resources/sql/migrate_v3_sport_calories.sql
```

升级到含用户头像的版本时，执行 V4 迁移脚本（幂等、保留数据）：

```bash
mysql -u root -p < src/main/resources/sql/migrate_v4_user_avatar.sql
```

### 2. 配置数据库连接

修改 `src/main/resources/application.yml`，或通过环境变量覆盖：

- `DB_USERNAME`：数据库账号（默认 root）
- `DB_PASSWORD`：数据库密码（默认 123456）
- `JWT_SECRET`：JWT 密钥

### 3. 启动服务

推荐方式（打包运行，兼容性最好）：

```bash
mvn clean package -DskipTests
java -jar target/health-management-backend-0.0.1-SNAPSHOT.jar
```

备选方式（Maven 插件，部分环境下可能出现类加载问题）：

```bash
mvn spring-boot:run
```

服务启动后访问：

- 应用端口：`http://localhost:8080`（context-path 为 `/api`）
- 健康检查：`http://localhost:8080/api/health/ping`
- Swagger UI：`http://localhost:8080/api/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8080/api/v3/api-docs`

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```
