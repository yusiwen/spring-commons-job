# spring-commons-job

A Spring Boot scheduled task management library with database persistence, REST API, and extensible execution modes.

## Features

- **Persistent scheduling** — Task definitions stored in database, loaded on startup
- **Three registration methods** — REST API, `TaskRegistrar` (programmatic), `@ScheduledTask` (declarative)
- **Cron-based** — Uses Spring `TaskScheduler` + `CronTrigger`
- **Concurrent control** — Per-task allow/disallow concurrent execution
- **Execution history** — Every execution logged to `task_log` table with duration, trigger type, params snapshot
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

## Creating a Task Programmatically

Inject `TaskRegistrar` and register tasks in code:

```java
@Component
public class MyService {

    @Autowired
    private TaskRegistrar taskRegistrar;

    @PostConstruct
    public void init() {
        // Reference an existing bean method
        taskRegistrar.schedule("日报生成", "0 12 3 * * ?", this, "generate")
                .params("[\"2026-06-30\"]")
                .concurrent(false)
                .register();
    }

    // Direct Runnable (auto-wrapped as a Spring bean)
    public void registerCleanup() {
        taskRegistrar.schedule("清理任务", "0 0 2 * * ?", () -> {
            tempCleaner.clean();
        });
    }

    public void generate(String date) { ... }
}
```

### TaskRegistrar API

| Method | Description |
|---|---|
| `schedule(name, cron, bean, method)` | Reference existing bean instance |
| `schedule(name, cron, beanName, method)` | Reference bean by name |
| `schedule(name, cron, Runnable)` | Register a Runnable (auto wrapped) |
| `cancel(taskId)` | Disable by task ID |
| `cancelByName(name)` | Disable by task name |

## Creating a Task Declaratively

Annotate any `@Component` with `@ScheduledTask` — it auto-registers on startup:

```java
@ScheduledTask(name = "每日统计", cron = "0 0 3 * * ?")
@Component("statsTask")
public class StatisticsTask {
    // default method name is "execute", can be overridden via method attribute
    public void execute() {
        // runs every day at 03:00
    }
}
```

> Duplicate names are skipped automatically (idempotent registration).

## Architecture

```
Registration paths:
  REST API (TaskController) ──┐
  TaskRegistrar (programmatic) ─┤──► TaskService ──► TaskSchedulerManager ──► ThreadPoolTaskScheduler
  @ScheduledTask (declarative) ─┘         │                     │
                                          │                     ▼
                                          │              ScheduledFuture
                                          │                (per task)
                                          ▼
                                    TaskExecutor (interface)
                                      ├── LocalTaskExecutor    (JVM reflection)
                                      └── RemoteTaskExecutor   (HTTP callback, future)
```

## Requirements

- Java 8+
- Spring Boot 2.7.x

## License

MIT
