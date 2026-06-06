package org.toolkit4j.quartz.task;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.toolkit4j.quartz.task.internal.DefaultTaskBuilder;
import org.toolkit4j.quartz.task.internal.TaskRegistration;
import org.toolkit4j.quartz.task.internal.TaskSchedule;

/**
 * Default implementation of {@link TaskScheduler}.
 *
 * <p>The wrapped Quartz {@link Scheduler} is owned, started, and stopped by the caller.
 */
@Slf4j
public class DefaultTaskScheduler implements TaskScheduler {

  private static final String DEFAULT_GROUP = "toolkit4j-tasks";

  private final Scheduler scheduler;
  private final String jobGroup;
  private final String triggerGroup;

  public DefaultTaskScheduler(Scheduler scheduler) {
    this(scheduler, DEFAULT_GROUP);
  }

  public DefaultTaskScheduler(Scheduler scheduler, String sharedGroup) {
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    this.jobGroup = requireNonBlank(sharedGroup, "sharedGroup");
    this.triggerGroup = jobGroup;
    log.debug(
        "Initialized quartz-task wrapper with external scheduler type={}",
        scheduler.getClass().getName());
  }

  @Override
  public void register(Class<? extends Job> jobClass, Consumer<TaskOptions> options) {
    Objects.requireNonNull(jobClass, "jobClass");
    Objects.requireNonNull(options, "options");

    val builder = new DefaultTaskBuilder();
    options.accept(builder);
    val registration = builder.build(jobClass);
    log.debug(
        "Registering Quartz task id={}, jobClass={}, scheduleType={}, conflictPolicy={}, durable={}, requestRecovery={}",
        registration.taskId(),
        registration.jobClass().getName(),
        registration.schedule().type(),
        registration.conflictPolicy(),
        registration.durable(),
        registration.requestRecovery());

    val jobKey = jobKey(registration.taskId());
    JobBuilder jobBuilder =
        JobBuilder.newJob(registration.jobClass())
            .withIdentity(jobKey)
            .storeDurably(registration.durable())
            .requestRecovery(registration.requestRecovery());
    if (registration.description() != null && !registration.description().isBlank()) {
      jobBuilder = jobBuilder.withDescription(registration.description());
    }
    val jobDetail = jobBuilder.usingJobData(new JobDataMap(registration.jobData())).build();

    val trigger = buildTrigger(jobKey, registration);

    try {
      scheduler.scheduleJob(jobDetail, trigger);
      log.debug(
          "Registered Quartz task id={} with triggerKey={}, jobGroup={}, triggerGroup={}",
          registration.taskId(),
          trigger.getKey(),
          jobGroup,
          triggerGroup);
    } catch (ObjectAlreadyExistsException e) {
      if (registration.conflictPolicy() == TaskRegistrationConflictPolicy.IGNORE_IF_EXISTS) {
        log.debug(
            "Skipping Quartz task registration for id={} because it already exists",
            registration.taskId());
        return;
      }
      if (registration.conflictPolicy() == TaskRegistrationConflictPolicy.RECREATE) {
        log.debug("Recreating existing Quartz task id={}", registration.taskId());
        recreateExistingTask(registration, jobKey, jobDetail, trigger);
        return;
      }
      throw new TaskRegistrationException(
          "task id already exists: "
              + registration.taskId()
              + ". use ifExistsRecreate(true) to replace it or ifExistsIgnore(true) to skip.",
          e);
    } catch (SchedulerException e) {
      throw new TaskSchedulingException("failed to schedule job: " + registration.taskId(), e);
    }
  }

  private void recreateExistingTask(
      TaskRegistration registration, JobKey jobKey, JobDetail jobDetail, Trigger trigger) {
    try {
      log.debug("Deleting existing Quartz task id={} before recreation", registration.taskId());
      scheduler.deleteJob(jobKey);
      scheduler.scheduleJob(jobDetail, trigger);
      log.debug(
          "Recreated Quartz task id={} with triggerKey={}",
          registration.taskId(),
          trigger.getKey());
    } catch (SchedulerException e) {
      throw new TaskSchedulingException("failed to recreate task: " + registration.taskId(), e);
    }
  }

  @Override
  public void pause(@NonNull String taskId) {
    if (!exists(taskId)) {
      throw new TaskNotFoundException("task not found: " + taskId);
    }
    val jobKey = jobKey(taskId);
    log.debug("Pausing Quartz task id={}", taskId);
    withScheduler("pause task: " + taskId, () -> scheduler.pauseJob(jobKey));
    log.debug("Paused Quartz task id={}", taskId);
  }

  @Override
  public void resume(@NonNull String taskId) {
    if (!exists(taskId)) {
      throw new TaskNotFoundException("task not found: " + taskId);
    }
    val jobKey = jobKey(taskId);
    log.debug("Resuming Quartz task id={}", taskId);
    withScheduler("resume task: " + taskId, () -> scheduler.resumeJob(jobKey));
    log.debug("Resumed Quartz task id={}", taskId);
  }

  @Override
  public void triggerNow(@NonNull String taskId) {
    val jobKey = jobKey(taskId);
    try {
      log.debug("Triggering Quartz task immediately id={}", taskId);
      scheduler.triggerJob(jobKey);
      log.debug("Triggered Quartz task immediately id={}", taskId);
    } catch (SchedulerException e) {
      if (!exists(taskId)) {
        throw new TaskNotFoundException("task not found: " + taskId);
      }
      throw new TaskSchedulingException("failed to trigger now: " + taskId, e);
    }
  }

  @Override
  public void unschedule(@NonNull String taskId) {
    val jobKey = jobKey(taskId);
    log.debug("Unscheduling Quartz task id={}", taskId);
    val deleted = withScheduler("unschedule task: " + taskId, () -> scheduler.deleteJob(jobKey));
    if (!deleted) {
      throw new TaskNotFoundException("task not found: " + taskId);
    }
    log.debug("Unscheduled Quartz task id={}", taskId);
  }

  @Override
  public boolean exists(@NonNull String taskId) {
    return withScheduler(
        "check task existence: " + taskId, () -> scheduler.checkExists(jobKey(taskId)));
  }

  @Override
  public Optional<TaskInfo> getTask(@NonNull String taskId) {
    val jobKey = jobKey(taskId);
    val detail = withScheduler("query task: " + taskId, () -> scheduler.getJobDetail(jobKey));
    return Optional.ofNullable(detail).map(this::toTaskInfo);
  }

  @Override
  public List<TaskInfo> listTasks() {
    return withScheduler(
            "list tasks", () -> scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup)))
        .stream()
        .map(this::toTaskInfo)
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(TaskInfo::taskId))
        .toList();
  }

  private @org.jetbrains.annotations.Nullable TaskInfo toTaskInfo(JobKey jobKey) {
    val detail = withScheduler("query job detail: " + jobKey, () -> scheduler.getJobDetail(jobKey));
    return detail == null ? null : toTaskInfo(detail);
  }

  private @NotNull TaskInfo toTaskInfo(@NotNull JobDetail jobDetail) {
    val triggers = getTriggers(jobDetail.getKey());
    val primaryTrigger = resolvePrimaryTrigger(triggers);
    val paused = isPaused(triggers, jobDetail.getKey().getName());
    val nextFireAt = nextFireAt(triggers);
    val metadata = scheduleMetadata(primaryTrigger);

    return new TaskInfo(
        jobDetail.getKey().getName(),
        jobDetail.getJobClass(),
        jobDetail.getDescription(),
        jobDetail.isDurable(),
        jobDetail.requestsRecovery(),
        metadata.kind(),
        metadata.cronExpression(),
        metadata.cronZoneId(),
        metadata.interval(),
        metadata.startAt(),
        jobDataOf(jobDetail.getJobDataMap()),
        nextFireAt,
        paused);
  }

  private List<? extends Trigger> getTriggers(JobKey jobKey) {
    return withScheduler("query triggers of: " + jobKey, () -> scheduler.getTriggersOfJob(jobKey));
  }

  @Contract(pure = true)
  private @NotNull @Unmodifiable Map<String, Object> jobDataOf(JobDataMap jobDataMap) {
    return Map.copyOf(jobDataMap);
  }

  private Trigger resolvePrimaryTrigger(@NotNull List<? extends Trigger> triggers) {
    return triggers.stream()
        .min(Comparator.comparing(Trigger::getNextFireTime, Comparator.nullsLast(Date::compareTo)))
        .orElse(null);
  }

  private Instant nextFireAt(@NotNull List<? extends Trigger> triggers) {
    return triggers.stream()
        .map(Trigger::getNextFireTime)
        .filter(Objects::nonNull)
        .map(Date::toInstant)
        .min(Comparator.naturalOrder())
        .orElse(null);
  }

  private boolean isPaused(@NotNull List<? extends Trigger> triggers, String taskId) {
    if (triggers.isEmpty()) {
      return false;
    }
    return triggers.stream()
        .allMatch(
            trigger -> {
              val state =
                  withScheduler(
                      "query trigger state for: " + taskId,
                      () -> scheduler.getTriggerState(trigger.getKey()));
              return state == Trigger.TriggerState.PAUSED;
            });
  }

  private Trigger buildTrigger(JobKey jobKey, @NotNull TaskRegistration registration) {
    val schedule = registration.schedule();
    Objects.requireNonNull(schedule, "schedule");

    val identity = registration.taskId() + "-" + schedule.type().name().toLowerCase();

    return switch (schedule.type()) {
      case CRON -> buildCronTrigger(jobKey, identity, schedule);
      case ONCE -> buildOnceTrigger(jobKey, identity, schedule);
      case FIXED_INTERVAL -> buildIntervalTrigger(jobKey, identity, schedule);
    };
  }

  private Trigger buildCronTrigger(JobKey jobKey, String identity, @NotNull TaskSchedule schedule) {
    val timezone =
        schedule.cronZoneId() == null
            ? TimeZone.getDefault()
            : TimeZone.getTimeZone(schedule.cronZoneId());
    final CronScheduleBuilder cronSchedule;
    try {
      cronSchedule =
          CronScheduleBuilder.cronSchedule(schedule.cronExpression()).inTimeZone(timezone);
    } catch (RuntimeException e) {
      throw new TaskRegistrationException(
          "invalid cron expression: " + schedule.cronExpression(), e);
    }
    return TriggerBuilder.newTrigger()
        .withIdentity(identity, triggerGroup)
        .forJob(jobKey)
        .withSchedule(cronSchedule)
        .build();
  }

  private Trigger buildOnceTrigger(JobKey jobKey, String identity, @NotNull TaskSchedule schedule) {
    val startAt = Date.from(schedule.onceFireAt());
    return TriggerBuilder.newTrigger()
        .withIdentity(identity, triggerGroup)
        .forJob(jobKey)
        .startAt(startAt)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
        .build();
  }

  private Trigger buildIntervalTrigger(
      JobKey jobKey, String identity, @NotNull TaskSchedule schedule) {
    val interval = schedule.fixedInterval();
    if (!interval.isPositive()) {
      throw new TaskRegistrationException("fixedInterval must be positive.");
    }
    val startAt = Date.from(schedule.fixedStartAt());
    return TriggerBuilder.newTrigger()
        .withIdentity(identity, triggerGroup)
        .forJob(jobKey)
        .startAt(startAt)
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(interval.toMillis())
                .repeatForever())
        .build();
  }

  private ScheduleMetadata scheduleMetadata(Trigger primaryTrigger) {
    if (primaryTrigger == null) {
      return ScheduleMetadata.unknown();
    }
    val startAt =
        primaryTrigger.getStartTime() == null ? null : primaryTrigger.getStartTime().toInstant();
    if (primaryTrigger instanceof CronTrigger cronTrigger) {
      return ScheduleMetadataBuilder.builder()
          .kind(TaskScheduleKind.CRON)
          .cronExpression(cronTrigger.getCronExpression())
          .cronZoneId(cronTrigger.getTimeZone().toZoneId())
          .startAt(startAt)
          .build();
    }
    if (primaryTrigger instanceof SimpleTrigger simpleTrigger) {
      if (simpleTrigger.getRepeatInterval() > 0L) {
        return ScheduleMetadataBuilder.builder()
            .kind(TaskScheduleKind.INTERVAL)
            .interval(Duration.ofMillis(simpleTrigger.getRepeatInterval()))
            .startAt(startAt)
            .build();
      }
      return ScheduleMetadataBuilder.builder().kind(TaskScheduleKind.ONCE).startAt(startAt).build();
    }
    return ScheduleMetadataBuilder.builder()
        .kind(TaskScheduleKind.UNKNOWN)
        .startAt(startAt)
        .build();
  }

  private void withScheduler(String operation, @NotNull QuartzRunnable runnable) {
    try {
      runnable.run();
    } catch (SchedulerException e) {
      throw new TaskSchedulingException("failed to " + operation, e);
    }
  }

  private <T> T withScheduler(String operation, @NotNull QuartzSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (SchedulerException e) {
      throw new TaskSchedulingException("failed to " + operation, e);
    }
  }

  @FunctionalInterface
  private interface QuartzRunnable {
    void run() throws SchedulerException;
  }

  @FunctionalInterface
  private interface QuartzSupplier<T> {
    T get() throws SchedulerException;
  }

  @Contract("_ -> new")
  private @NotNull JobKey jobKey(String taskId) {
    return JobKey.jobKey(taskId, jobGroup);
  }

  private static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new TaskSchedulingException(
          "task scheduler " + fieldName + " must not be null or blank");
    }
    return value;
  }
}
