# Quartz 任务删除 API 文档

## 🗑️ 删除任务相关接口

### 1. 删除单个任务
**接口**: `DELETE /api/quartz/delete-job`

**参数**:
- `jobName` (必填): 任务名称
- `jobGroup` (可选): 任务组名称，默认为 `defaultGroup`

**示例**:
```bash
# 删除默认组中的任务
curl -X DELETE "http://localhost:8080/api/quartz/delete-job?jobName=cronJob_daily"

# 删除指定组中的任务
curl -X DELETE "http://localhost:8080/api/quartz/delete-job?jobName=testJob&jobGroup=testGroup"
```

**响应示例**:
```json
{
  "success": true,
  "message": "任务删除成功",
  "jobName": "cronJob_daily",
  "jobGroup": "defaultGroup",
  "deletedAt": "2024-07-10T10:30:00.000+00:00"
}
```

### 2. 批量删除任务
**接口**: `DELETE /api/quartz/delete-jobs`

**参数**:
- `jobNames` (必填): 任务标识符列表，格式为 `jobGroup.jobName` 或 `jobName`

**示例**:
```bash
# 批量删除任务
curl -X DELETE "http://localhost:8080/api/quartz/delete-jobs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "jobNames=cronJob_daily&jobNames=testGroup.testJob&jobNames=cronJob_hourly"
```

**响应示例**:
```json
{
  "success": true,
  "message": "批量删除完成：成功 3 个，失败 0 个",
  "totalCount": 3,
  "successCount": 3,
  "failCount": 0,
  "results": [
    {
      "jobName": "cronJob_daily",
      "jobGroup": "defaultGroup",
      "identifier": "cronJob_daily",
      "success": true,
      "message": "删除成功"
    }
  ],
  "deletedAt": "2024-07-10T10:30:00.000+00:00"
}
```

### 3. 删除任务组
**接口**: `DELETE /api/quartz/delete-group`

**参数**:
- `jobGroup` (必填): 任务组名称

**示例**:
```bash
# 删除整个任务组
curl -X DELETE "http://localhost:8080/api/quartz/delete-group?jobGroup=testGroup"
```

**响应示例**:
```json
{
  "success": true,
  "message": "删除任务组完成：成功 5 个，失败 0 个",
  "jobGroup": "testGroup",
  "totalCount": 5,
  "successCount": 5,
  "failCount": 0,
  "deletedJobs": ["job1", "job2", "job3", "job4", "job5"],
  "failedJobs": [],
  "deletedAt": "2024-07-10T10:30:00.000+00:00"
}
```

## 📋 查询任务相关接口

### 4. 查询所有任务
**接口**: `GET /api/quartz/list-jobs`

**示例**:
```bash
curl "http://localhost:8080/api/quartz/list-jobs"
```

**响应示例**:
```json
{
  "success": true,
  "totalGroups": 2,
  "totalJobs": 8,
  "jobsByGroup": {
    "defaultGroup": [
      {
        "jobName": "cronJob_daily",
        "jobGroup": "defaultGroup",
        "identifier": "defaultGroup.cronJob_daily",
        "exists": true,
        "nextFireTime": "2024-07-11T09:00:00.000+00:00",
        "jobClass": "CronJob",
        "description": "每天上午9点执行的Cron任务",
        "durable": false,
        "requestsRecovery": false
      }
    ]
  },
  "groupNames": ["defaultGroup", "testGroup"],
  "queriedAt": "2024-07-10T10:30:00.000+00:00"
}
```

### 5. 查询指定组的任务
**接口**: `GET /api/quartz/list-jobs/{jobGroup}`

**示例**:
```bash
curl "http://localhost:8080/api/quartz/list-jobs/defaultGroup"
```

**响应示例**:
```json
{
  "success": true,
  "jobGroup": "defaultGroup",
  "jobCount": 3,
  "jobs": [
    {
      "jobName": "cronJob_daily",
      "jobGroup": "defaultGroup",
      "identifier": "defaultGroup.cronJob_daily",
      "exists": true,
      "nextFireTime": "2024-07-11T09:00:00.000+00:00",
      "jobClass": "CronJob",
      "description": "每天上午9点执行的Cron任务",
      "durable": false,
      "requestsRecovery": false
    }
  ],
  "queriedAt": "2024-07-10T10:30:00.000+00:00"
}
```

## 🔄 完整的任务管理流程

### 1. 查看现有任务
```bash
# 查看所有任务
curl "http://localhost:8080/api/quartz/list-jobs"
```

### 2. 创建测试任务
```bash
# 创建几个测试任务
curl -X POST "http://localhost:8080/api/quartz/create-cron?type=daily"
curl -X POST "http://localhost:8080/api/quartz/create-cron?type=hourly"
curl -X POST "http://localhost:8080/api/quartz/create-cron?type=weekly"
```

### 3. 删除任务
```bash
# 删除单个任务
curl -X DELETE "http://localhost:8080/api/quartz/delete-job?jobName=cronJob_daily"

# 批量删除
curl -X DELETE "http://localhost:8080/api/quartz/delete-jobs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "jobNames=cronJob_hourly&jobNames=cronJob_weekly"

# 删除整个组
curl -X DELETE "http://localhost:8080/api/quartz/delete-group?jobGroup=defaultGroup"
```

### 4. 验证删除结果
```bash
# 再次查看任务列表，确认删除成功
curl "http://localhost:8080/api/quartz/list-jobs"
```

## ⚠️ 注意事项

1. **任务标识符格式**: 
   - 完整格式: `jobGroup.jobName`
   - 简化格式: `jobName` (使用默认组)

2. **删除顺序**: 删除任务时会同时删除相关的触发器

3. **错误处理**: 如果任务不存在，会返回相应的错误信息

4. **批量操作**: 批量删除时，即使部分任务删除失败，其他任务仍会继续删除

5. **日志记录**: 所有删除操作都会记录详细的日志信息
