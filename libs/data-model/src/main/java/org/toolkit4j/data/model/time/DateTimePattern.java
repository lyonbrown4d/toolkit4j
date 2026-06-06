package org.toolkit4j.data.model.time;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.toolkit4j.data.model.enumeration.EnumValue;

public enum DateTimePattern implements EnumValue<String> {
  STANDARD_DATE_TIME("yyyy-MM-dd HH:mm:ss"),
  YEAR_MONTH("yyyy-MM"),
  DATE("yyyy-MM-dd"),
  BASIC_YEAR_MONTH("yyyyMM"),
  BASIC_DATE("yyyyMMdd"),
  TIME("HH:mm:ss"),
  TIME_MILLIS("HH:mm:ss.SSS"),
  BASIC_TIMESTAMP("yyyyMMddHHmmss");

  @Getter(onMethod_ = @NotNull)
  private final String primaryValue;

  private final DateTimeFormatter formatter;

  DateTimePattern(String primaryValue) {
    this.primaryValue = Objects.requireNonNull(primaryValue, "primaryValue");
    this.formatter = DateTimeFormatter.ofPattern(primaryValue);
  }

  public @NotNull DateTimeFormatter formatter() {
    return formatter;
  }
}
