# CowHealth Backend

Java 17 + Spring Boot 3.x + Maven + MyBatis-Plus + MySQL 8 + Redis backend demo.

## Features

- Auth demo: `POST /api/auth/login` with `admin/admin`, token stored in Redis.
- Cow archive CRUD: `/api/cows`.
- Observation ingest:
  - batch JSON: `POST /api/ingest/observations`
  - CSV upload: `POST /api/ingest/csv`
  - query: `GET /api/observations/query`
- Data quality flags: `GOOD|SUSPECT|BAD`.
- Rule CRUD and enable/disable: `/api/rules`.
- Alarm lifecycle: `/api/alarms`, detail, ack/resolve/false_positive.
- Baseline + deviation score.
- Schedulers:
  - every minute: rule evaluation (threshold/duration/composite/offline + silence window)
  - daily at 00:00: baseline rebuild
- Dashboard summary: `GET /api/dashboard/summary`
- OpenAPI: `/swagger-ui/index.html`
- Health: `/actuator/health`

## Quick Start

1. Start infrastructure:

```bash
cd backend
docker compose up -d
```

2. Run backend:

```bash
cd backend
mvn -q -DskipTests spring-boot:run
```

3. Login:

```http
POST /api/auth/login
Content-Type: application/json

{"username":"admin","password":"admin"}
```

Use returned token:

`Authorization: Bearer <token>`

## Environment

- Dev config: `src/main/resources/application-dev.yml`
- Prod config: `src/main/resources/application-prod.yml`
- Base config: `src/main/resources/application.yml`

## Seed Data

- `db/seed.sql` inserts 30 cows, 2 barns (`B1`,`B2`), and default rules.
- `DataSeeder` generates 7-day aggregated observation demo data at startup.
- Demo data pattern can trigger:
  - respiratory abnormal alarms
  - heat stress composite alarms
  - offline alarms


## .\mvnw -DskipTests spring-boot:run
## http://localhost:8080/swagger-ui/index.html

# 提示词
你在 workspace-write 模式，必须直接创建/修改工作区文件而不是只输出代码。目标：生成一个可运行的后端工程 backend/（Java 17, Spring Boot 3.x, Maven, MyBatis-Plus, MySQL 8, Redis, SpringDoc OpenAPI, Actuator, Validation）。要求：代码完整可编译运行。

# A. 工程结构
- backend/pom.xml
- backend/src/main/java/...（按包名 com.cowhealth）
- backend/src/main/resources/application.yml + application-dev.yml + application-prod.yml
- backend/src/main/resources/db/schema.sql + seed.sql
- backend/README.md

# B. 必须实现的业务模块（MVP+V1）
1) Auth（演示登录）
- POST /api/auth/login  (admin/admin)，返回 token（可用简单 JWT 或自定义 token，存 Redis）
- 前端会用 Authorization: Bearer <token>
- 提供拦截器/过滤器校验

2) 牛只档案 Cow
- 表 cow：cow_id, name, ear_tag, barn_id, pen, age_days, parity, lactation_day, status, created_at, updated_at
- CRUD：/api/cows (GET分页/POST/PUT/DELETE)
- 支持筛选 barn_id、status、关键词（cow_id/name/ear_tag）

3) 观测数据接入 Observation
- POST /api/ingest/observations 批量上报数组，幂等键(cow_id,barn_id,metric_name,timestamp)
- CSV 上传导入：POST /api/ingest/csv （返回导入条数与错误行）
- 保存到 observation_raw（可保留）并写入 minute 聚合表 observation_minute_agg（分钟粒度 ts_minute）
- 提供查询：GET /api/observations/query?cowId=&barnId=&metric=&from=&to=&granularity=minute|hour|day
  - minute：读 observation_minute_agg
  - hour/day：在 SQL 里二次聚合（avg/min/max/count/bad_count）

4) 数据质量 Quality
- quality_flag: GOOD|SUSPECT|BAD
- 入库时执行简单规则：
  - value 为 NaN/inf 或明显越界 -> BAD
  - 相邻点突变超过阈值 -> SUSPECT（用 Redis 缓存每个 cow+metric 上一个值）
- BAD 不参与聚合 avg，但 bad_count 计数

5) 规则配置 Rule
- 表 rule_config：rule_id, name, enabled, severity(P1/P2/P3), type(THRESHOLD/DURATION/COMPOSITE/OFFLINE), metric_name, upper, lower, duration_minutes, silence_minutes, composite_json, barn_scope(optional), created_at, updated_at
- CRUD：/api/rules
- enable/disable

6) 告警 Alarm + 处置闭环
- 表 alarm：alarm_id, cow_id, barn_id, rule_id, severity, status(OPEN/ACKED/RESOLVED/FALSE_POSITIVE), start_time, end_time, metric_name, trigger_value, threshold_json, deviation_score, evidence_json, created_at, updated_at
- 表 alarm_action_log：id, alarm_id, action(ACK/RESOLVE/FALSE_POSITIVE), note, operator, created_at
- API：
  - GET /api/alarms (分页筛选：severity/status/cowId/barnId/metric/from/to)
  - GET /api/alarms/{id}
  - POST /api/alarms/{id}/ack
  - POST /api/alarms/{id}/resolve
  - POST /api/alarms/{id}/false_positive

7) 个体基线 Baseline + 偏离度 deviation_score（V1关键）
- 表 baseline_hourly：cow_id, metric_name, hour_of_day(0-23), mean, std, updated_at
- 每天凌晨跑任务：基于过去 7 天 observation_minute_agg 统计（按小时分桶）
- deviation_score = (current_value - mean) / std（std过小做保护）
- 告警触发时写入 deviation_score 并展示在 evidence_json 里

8) 定时任务（必须）
- 每 1 分钟运行：
  - 对启用规则进行检查，读取最近窗口分钟聚合数据
  - 处理静默期：Redis key silence:{cowId}:{ruleId}，存在则不重复报警
  - 对持续型规则：连续N分钟满足条件才触发（可用最近N条聚合记录判断）
  - 对组合规则：例如 “resp_rate 偏高 且 (temp_c>阈值 AND humidity>阈值)”（环境按 barn_id）
  - 对离线规则：某 cow 的某 metric 超过 N 分钟无数据 -> 生成 P3 告警
- 每天凌晨跑 baseline 更新

9) Dashboard
- GET /api/dashboard/summary
  - 今日告警数按等级
  - 观察/治疗牛只数
  - 24h 告警趋势（按小时）
  - Top 异常指标排行（按告警数量）

10) 统一返回结构 + 全局异常 + traceId
- 返回：{code,message,data,traceId}
- 全局异常处理
- 请求日志中带 traceId

11) Swagger + Actuator
- /swagger-ui/index.html
- /actuator/health

# C. 数据库与演示数据
- schema.sql + seed.sql：
  - 30头牛、2个barn
  - 生成过去7天的分钟聚合 demo 数据（可用后端启动时插入脚本或 seed.sql 直接插入较粗粒度也行，但必须能看到曲线）
  - 至少能触发3类告警：呼吸异常、热应激、数据离线

# D. 运行方式
- 提供 docker-compose 只启动 mysql+redis（后端本地跑）
- backend/README.md 写清楚启动命令与配置

# 输出要求
1) 直接在工作区创建所有文件
2) 输出最终文件树
3) 给出 mvn -q -DskipTests spring-boot:run 的命令
4) 不要省略任何关键代码文件
现在开始创建 backend 项目。