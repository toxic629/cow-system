# 提示词
##你在 workspace-write 模式，必须直接创建/修改工作区文件。目标：生成可运行前端工程 frontend/（Vite + React + TypeScript + Ant Design + ECharts + axios + zustand + dayjs）。必须与后端 API 对接（baseURL 可配置，默认 http://localhost:8080）。

# 后端鉴权（必须按此实现）
- 登录接口：POST {baseURL}/api/auth/login
- 请求体：{ "username": "admin", "password": "admin" }
- 响应结构（必须按此解析）：
  { "code": 0, "message": "ok", "data": { "token": "<string>", "tokenType": "Bearer", "expiresInSeconds": 43200 } }
- 前端保存：token = ${tokenType} ${token}
- axios 拦截器：自动带 Authorization；遇到响应体 code==401 或 HTTP 401 时清空 token 并跳转 /login

# 路由（必须）
- /login 登录页（admin/admin），登录成功跳转 /dashboard
- /dashboard 牛群概览：调用 GET /api/dashboard/summary
  - 用 AntD 卡片展示 summary 字段（按返回 key 动态渲染，避免字段不一致导致报错）
  - 如果接口返回含 time-series（不确定），再画趋势图；否则显示“暂无趋势数据”

- /cows 牛只列表：调用 GET /api/cows（支持分页参数 page/size 若存在；若不存在则前端分页）
  - 新增/编辑：POST/PUT /api/cows
  - 删除：DELETE /api/cows（若后端是 /api/cows/{id} 或 body 传参不确定，则在 api 层做兼容：优先调用 /api/cows/{cowId}，失败再调用 /api/cows 并在 query/body 传 cowId）

- /cows/:cowId 单牛详情：
  - 指标下拉：resp_rate / heart_rate / rumination_minutes / activity_index / temp_c / humidity / nh3_ppm
  - 时间范围：24h/7d（转换成 from/to 时间戳或字符串）
  - 调用 GET /api/observations/query（参数名不确定时做兼容：尝试 cowId/metricName/from/to；失败则尝试 cow_id/metric/start/end）
  - 用 ECharts 折线图画 ts-value
  - 告警高亮（可选增强）：尝试调用 GET /api/alarms 并按 cowId+metric+timeRange 过滤；若拿不到区间信息则不高亮，页面仍可用

- /alarms 告警中心：
  - 列表：GET /api/alarms（支持筛选：level/status/metric/barn/cow/time，若后端不支持则前端筛选）
  - 详情抽屉：展示 alarm 的 evidence/deviation_score/threshold（字段不存在则隐藏该行）
  - 操作：
    POST /api/alarms/{id}/ack
    POST /api/alarms/{id}/resolve
    POST /api/alarms/{id}/false_positive
    备注字段名不确定时：优先 { "note": "..." }，失败再用 { "remark": "..." }

- /rules 规则配置：
  - 列表：GET /api/rules（分页同上：支持则后端分页，不支持前端分页）
  - 启用/停用：POST /api/rules/{id}/enable  与  /api/rules/{id}/disable
  - 新增/编辑：POST/PUT /api/rules
    - UI 至少包含：metricName、minValue、maxValue、durationSeconds(可选)、enabled
    - 若后端不支持 durationSeconds，提交时不带该字段

# 工程要求（必须）
- 统一 API client：src/api/http.ts + src/api/*.ts
- zustand：src/store/auth.ts 存 token、user
- 环境变量：VITE_API_BASE_URL（默认 http://localhost:8080）
- AntD Layout：侧边栏+顶部；路由守卫：无 token 访问非 /login 自动跳 /login
- 基础可用样式，不追求美观

# 输出要求
1) 在工作区生成 frontend/ 全部文件（含 package.json、vite 配置、tsconfig 等）
2) 输出最终文件树
3) 给出 npm install / npm run dev 命令
现在开始创建 frontend 项目并落盘。

运行：npm run dev
