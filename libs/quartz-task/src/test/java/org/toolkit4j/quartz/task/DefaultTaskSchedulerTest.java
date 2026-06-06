package org.toolkit4j.quartz.task;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

@Execution(ExecutionMode.SAME_THREAD)
class DefaultTaskSchedulerTest {

  private Scheduler scheduler;
  private DefaultTaskScheduler taskScheduler;

  private static final AtomicReference<CountDownLatch> LATCH_REF = new AtomicReference<>();
  private static final AtomicReference<String> TENANT_ID_REF = new AtomicReference<>();
  private static final AtomicReference<String> SOURCE_REF = new AtomicReference<>();

  @BeforeEach
  void resetState() {
    LATCH_REF.set(null);
    TENANT_ID_REF.set(null);
    SOURCE_REF.set(null);
  }

  private static Scheduler createTestScheduler() throws SchedulerException {
    Properties props = new Properties();
    props.setProperty(
        "org.quartz.scheduler.instanceName", "toolkit4j-test-scheduler-" + System.nanoTime());
    props.setProperty("org.quartz.threadPool.threadCount", "2");
    props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
    return new StdSchedulerFactory(props).getScheduler();
  }

  @AfterEach
  void tearDown() throws SchedulerException {
    if (scheduler != null) {
      scheduler.shutdown(true);
    }
  }

  @Test
  void testOnceSchedule_executesAndRecordsStatus() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);
    scheduler.start();

    CountDownLatch latch = new CountDownLatch(1);
    LATCH_REF.set(latch);

    taskScheduler.register(
        OneTimeJob.class,
        options ->
            options
                .id("t1")
                .description("once-test")
                .startAt(Instant.now().plusMillis(200))
                .jobData("tenantId", "tenant-1")
                .jobData("source", "manual-test")
                .durable(true));

    Optional<TaskInfo> infoOpt = taskScheduler.getTask("t1");
    assertTrue(infoOpt.isPresent());
    TaskInfo info = infoOpt.get();
    assertEquals("t1", info.taskId());
    assertEquals(OneTimeJob.class, info.jobClass());
    assertEquals(TaskScheduleKind.ONCE, info.scheduleType());
    assertEquals("tenant-1", info.jobData().get("tenantId"));

    assertTrue(latch.await(5, TimeUnit.SECONDS), "task should execute");
    assertEquals("tenant-1", TENANT_ID_REF.get());
    assertEquals("manual-test", SOURCE_REF.get());
    assertTrue(taskScheduler.exists("t1"));
  }

  @Test
  void testTriggerNow_executesWithManualTriggerSource() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);
    scheduler.start();

    CountDownLatch latch = new CountDownLatch(1);
    LATCH_REF.set(latch);

    taskScheduler.register(
        ManualTriggerJob.class,
        options ->
            options
                .id("t2")
                .description("manual-trigger-test")
                .startAt(Instant.now().plusSeconds(30)));

    taskScheduler.triggerNow("t2");
    assertTrue(latch.await(5, TimeUnit.SECONDS), "task should execute by triggerNow");
  }

  @Test
  void testUnschedule_removesTask() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    taskScheduler.register(
        ManualTriggerJob.class,
        options ->
            options.id("t3").description("unschedule-test").startAt(Instant.now().plusSeconds(30)));

    assertTrue(taskScheduler.getTask("t3").isPresent());
    taskScheduler.unschedule("t3");
    assertTrue(taskScheduler.getTask("t3").isEmpty());
    assertFalse(taskScheduler.exists("t3"));
  }

  @Test
  void testPauseAndResume_updatesPausedState() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    taskScheduler.register(
        ManualTriggerJob.class,
        options ->
            options
                .id("t4")
                .description("pause-resume-test")
                .startAt(Instant.now().plusSeconds(30)));

    taskScheduler.pause("t4");
    TaskInfo pausedInfo = taskScheduler.getTask("t4").orElseThrow();
    assertTrue(pausedInfo.paused());

    taskScheduler.resume("t4");
    TaskInfo resumedInfo = taskScheduler.getTask("t4").orElseThrow();
    assertFalse(resumedInfo.paused());
  }

  @Test
  void testListTasks_sortedByTaskId() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    taskScheduler.register(
        ManualTriggerJob.class, options -> options.id("task-b").interval(Duration.ofSeconds(10)));
    taskScheduler.register(
        ManualTriggerJob.class,
        options -> options.id("task-a").startAt(Instant.now().plusSeconds(30)));

    List<TaskInfo> tasks = taskScheduler.listTasks();
    assertEquals(2, tasks.size());
    assertEquals("task-a", tasks.get(0).taskId());
    assertEquals("task-b", tasks.get(1).taskId());
  }

  @Test
  void testCronSchedule_exposesCronMetadata() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    taskScheduler.register(
        ManualTriggerJob.class, options -> options.id("cron-task").cron("0/30 * * * * ?"));

    TaskInfo taskInfo = taskScheduler.getTask("cron-task").orElseThrow();
    assertEquals(TaskScheduleKind.CRON, taskInfo.scheduleType());
    assertEquals("0/30 * * * * ?", taskInfo.cronExpression());
    assertNotNull(taskInfo.cronZoneId());
  }

  @Test
  void testInvalidCron_throwsTaskRegistrationException() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    assertThrows(
        TaskRegistrationException.class,
        () ->
            taskScheduler.register(
                ManualTriggerJob.class, options -> options.id("bad-cron").cron("invalid cron")));
  }

  @Test
  void testDuplicateTaskId_defaultPolicyFails() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    taskScheduler.register(
        ManualTriggerJob.class, options -> options.id("dup-task").interval(Duration.ofSeconds(10)));

    assertThrows(
        TaskRegistrationException.class,
        () ->
            taskScheduler.register(
                ManualTriggerJob.class,
                options -> options.id("dup-task").interval(Duration.ofSeconds(20))));
  }

  @Test
  void testDuplicateTaskId_recreatePolicyReplacesTask() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    taskScheduler.register(
        ManualTriggerJob.class,
        options ->
            options
                .id("replace-task")
                .description("old-version")
                .interval(Duration.ofSeconds(10))
                .jobData("version", "v1"));

    taskScheduler.register(
        OneTimeJob.class,
        options ->
            options
                .id("replace-task")
                .description("new-version")
                .startAt(Instant.now().plusSeconds(60))
                .jobData("version", "v2")
                .ifExistsRecreate(true));

    TaskInfo taskInfo = taskScheduler.getTask("replace-task").orElseThrow();
    assertEquals(OneTimeJob.class, taskInfo.jobClass());
    assertEquals("new-version", taskInfo.description());
    assertEquals(TaskScheduleKind.ONCE, taskInfo.scheduleType());
    assertEquals("v2", taskInfo.jobData().get("version"));
  }

  @Test
  void testDuplicateTaskId_ignorePolicyLeavesExistingTask() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    taskScheduler.register(
        ManualTriggerJob.class,
        options ->
            options
                .id("ignore-dup")
                .description("first")
                .interval(Duration.ofSeconds(10))
                .jobData("version", "v1"));

    taskScheduler.register(
        OneTimeJob.class,
        options ->
            options
                .id("ignore-dup")
                .description("second-should-not-apply")
                .startAt(Instant.now().plusSeconds(60))
                .jobData("version", "v2")
                .ifExistsIgnore(true));

    TaskInfo taskInfo = taskScheduler.getTask("ignore-dup").orElseThrow();
    assertEquals(ManualTriggerJob.class, taskInfo.jobClass());
    assertEquals("first", taskInfo.description());
    assertEquals(TaskScheduleKind.INTERVAL, taskInfo.scheduleType());
    assertEquals("v1", taskInfo.jobData().get("version"));
  }

  @Test
  void testCustomGroupAllowsSameTaskIdInSameScheduler() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler, "toolkit4j-group-a");
    var aliasScheduler = new DefaultTaskScheduler(scheduler, "toolkit4j-group-b");
    scheduler.start();

    taskScheduler.register(
        ManualTriggerJob.class,
        options -> options.id("shared-id").interval(Duration.ofSeconds(10)));

    aliasScheduler.register(
        OneTimeJob.class,
        options ->
            options.id("shared-id").startAt(Instant.now().plusSeconds(30)).ifExistsIgnore(true));

    assertEquals(1, taskScheduler.listTasks().size());
    assertEquals(1, aliasScheduler.listTasks().size());
    assertEquals(
        TaskScheduleKind.INTERVAL, taskScheduler.getTask("shared-id").orElseThrow().scheduleType());
    assertEquals(
        TaskScheduleKind.ONCE, aliasScheduler.getTask("shared-id").orElseThrow().scheduleType());
  }

  @Test
  void testPauseUnknownTask_throwsTaskNotFound() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    assertThrows(TaskNotFoundException.class, () -> taskScheduler.pause("missing-id"));
    assertThrows(TaskNotFoundException.class, () -> taskScheduler.resume("missing-id"));
  }

  @Test
  void testNegativeInterval_rejectedAtRegistration() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    assertThrows(
        TaskRegistrationException.class,
        () ->
            taskScheduler.register(
                ManualTriggerJob.class,
                options -> options.id("bad-interval").interval(Duration.ofMillis(-1))));
  }

  @Test
  void testNullJobDataValue_throwsTaskRegistrationException() throws Exception {
    scheduler = createTestScheduler();
    taskScheduler = new DefaultTaskScheduler(scheduler);

    assertThrows(
        TaskRegistrationException.class,
        () ->
            taskScheduler.register(
                ManualTriggerJob.class,
                options ->
                    options
                        .id("null-job-data")
                        .startAt(Instant.now().plusSeconds(30))
                        .jobData("tenantId", (String) null)));
  }

  @Test
  void testWrapperDoesNotStartExternalScheduler() throws Exception {
    scheduler = createTestScheduler();
    assertFalse(scheduler.isStarted());

    taskScheduler = new DefaultTaskScheduler(scheduler);

    assertFalse(scheduler.isStarted());
  }

  public static class OneTimeJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
      TENANT_ID_REF.set(context.getMergedJobDataMap().getString("tenantId"));
      SOURCE_REF.set(context.getMergedJobDataMap().getString("source"));
      CountDownLatch latch = LATCH_REF.get();
      if (latch != null) {
        latch.countDown();
      }
    }
  }

  public static class ManualTriggerJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
      CountDownLatch latch = LATCH_REF.get();
      if (latch != null) {
        latch.countDown();
      }
    }
  }
}
