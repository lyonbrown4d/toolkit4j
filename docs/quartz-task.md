# Quartz Task

Artifact: `io.github.lyonbrown4d:quartz-task:0.0.6`

## What it provides

- High-level scheduler API: `TaskScheduler`
- Default implementation: `DefaultTaskScheduler`
- Builder-style registration options via `TaskOptions`
- Conflict policies for existing task IDs

## Minimal examples

```java
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.toolkit4j.quartz.task.DefaultTaskScheduler;
import org.toolkit4j.quartz.task.TaskScheduler;

public class DemoJob implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    System.out.println("run");
  }
}

Scheduler quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
quartzScheduler.start();

TaskScheduler scheduler = new DefaultTaskScheduler(quartzScheduler);
scheduler.register(DemoJob.class, opts -> opts
  .id("demo-job")
  .cron("0/30 * * * * ?")
  .description("run every 30 seconds")
);
```

## Notes

- This module keeps Quartz as the execution engine and wraps common scheduling flows with a simpler API.
- The caller owns the Quartz `Scheduler` lifecycle and is responsible for starting and shutting it down.
- Operation-level debug logs are emitted through SLF4J under `org.toolkit4j.quartz.task`.
