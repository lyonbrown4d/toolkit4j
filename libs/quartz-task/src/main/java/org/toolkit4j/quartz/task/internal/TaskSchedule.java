package org.toolkit4j.quartz.task.internal;

import io.soabase.recordbuilder.core.RecordBuilder;
import lombok.NonNull;
import lombok.val;
import org.toolkit4j.quartz.task.TaskRegistrationException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

@RecordBuilder
public record TaskSchedule(
    TaskScheduleType type,
    String cronExpression,
    ZoneId cronZoneId,
    Instant onceFireAt,
    Duration fixedInterval,
    Instant fixedStartAt) {
  public static TaskSchedule cron(@NonNull String expression, ZoneId zoneId) {
    val resolvedZoneId = zoneId == null ? ZoneId.systemDefault() : zoneId;
    return TaskScheduleBuilder.builder()
        .type(TaskScheduleType.CRON)
        .cronExpression(expression)
        .cronZoneId(resolvedZoneId)
        .build();
  }

  public static TaskSchedule once(@NonNull Instant fireAt) {
    return TaskScheduleBuilder.builder().type(TaskScheduleType.ONCE).onceFireAt(fireAt).build();
  }

  public static TaskSchedule fixedInterval(@NonNull Duration interval,@NonNull Instant startAt) {
    if (!interval.isPositive()) {
      throw new TaskRegistrationException("interval must be positive.");
    }
    return TaskScheduleBuilder.builder()
        .type(TaskScheduleType.FIXED_INTERVAL)
        .fixedInterval(interval)
        .fixedStartAt(startAt)
        .build();
  }
}
