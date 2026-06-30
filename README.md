# spring-commons-job

A Spring Boot scheduled task management library with database persistence, REST API, and extensible execution modes.

## Features

- **Persistent scheduling** — Task definitions stored in database, loaded on startup
- **REST API** — Full CRUD + trigger + enable/disable + refresh endpoints
- **Cron-based** — Uses Spring `TaskScheduler` + `CronTrigger`
- **Concurrent control** — Per-task allow/disallow concurrent execution
- **Execution history** — Every execution logged to `task_log` table
- **Database agnostic** — Portable SQL + UUID PK, works with H2/MySQL/PostgreSQL/Oracle
- **Dual-channel schema init** — Flyway (with isolated history table) or `DataSourceInitializer` fallback
- **Distributed ready** — `TaskExecutor` interface with `LOCAL`/`CALLBACK` mode, `callback_url` field pre-embedded

## Modules

| Module | Description |
|---|---|
| `spring-commons-job-core` | Core implementation: entities, MyBatis mappers, scheduler, REST controller |
| `spring-commons-job-starter` | Spring Boot auto-configuration, drop-in dependency |
| `spring-commons-job-test` | Integration tests |

## Quick Start

```xml
<dependency>
    <groupId>cn.yusiwen.spring</groupId>
    <artifactId>spring-commons-job-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Just add the dependency — the starter auto-configures everything:

- Creates `task_info` and `task_log` tables (via Flyway or fallback)
- Scans for `@EnableJobManagement` (optional, works without it)
- Registers all REST endpoints under `/api/tasks`

### Configuration

```yaml
job:
  management:
    enabled: true        # default: true
    pool-size: 5         # scheduler thread pool size, default: 5
```

## REST API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/tasks` | Create task |
| `PUT` | `/api/tasks/{id}` | Update task (auto reschedule) |
| `DELETE` | `/api/tasks/{id}` | Delete task |
| `GET` | `/api/tasks/{id}` | Get task detail |
| `GET` | `/api/tasks?page=1&size=20&status=` | List tasks (paginated) |
| `POST` | `/api/tasks/{id}/trigger` | Manually trigger once |
| `PUT` | `/api/tasks/{id}/enable` | Enable scheduling |
| `PUT` | `/api/tasks/{id}/disable` | Disable scheduling |
| `POST` | `/api/tasks/refresh` | Reload all tasks from DB |
| `GET` | `/api/tasks/{id}/logs?page=1&size=20` | Execution history |

## Creating a Task

Define a Spring bean with a method to be scheduled:

```java
@Component("myTask")
public class MyTask {
    public void run() {
        // your logic here
    }
}
```

Then create a task via API:

```json
POST /api/tasks
{
  "name": "My Scheduled Task",
  "cronExpression": "0 */5 * * * ?",
  "beanName": "myTask",
  "methodName": "run",
  "concurrent": false
}
```

## Architecture

```
REST API (TaskController)
       │
       ▼
TaskService ──► TaskSchedulerManager ──► ThreadPoolTaskScheduler
       │               │                         │
       │               ▼                         │
       │        ScheduledFuture                    │
       │          (per task)                       │
       │                                          │
       ▼                                          ▼
TaskExecutor (interface)
  ├── LocalTaskExecutor    (JVM reflection call)
  └── RemoteTaskExecutor   (HTTP callback, future)
```

## Requirements

- Java 8+
- Spring Boot 2.7.x

## License

MIT
