package org.toolkit4j.data.model.time;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@RecordBuilder
public record YearMonthValue(int year, int month) implements Comparable<YearMonthValue> {
  public YearMonthValue {
    YearMonth.of(year, month);
  }

  public static @NotNull YearMonthValue of(int year, int month) {
    return new YearMonthValue(year, month);
  }

  public static @NotNull YearMonthValue from(@NotNull YearMonth value) {
    var yearMonth = Objects.requireNonNull(value, "value");
    return new YearMonthValue(yearMonth.getYear(), yearMonth.getMonthValue());
  }

  public static @NotNull YearMonthValue parse(@NotNull CharSequence value) {
    return from(YearMonth.parse(Objects.requireNonNull(value, "value")));
  }

  public @NotNull YearMonth toYearMonth() {
    return YearMonth.of(year, month);
  }

  public @NotNull LocalDate atDay(int dayOfMonth) {
    return toYearMonth().atDay(dayOfMonth);
  }

  public @NotNull LocalDate atEndOfMonth() {
    return toYearMonth().atEndOfMonth();
  }

  public @NotNull String format(@NotNull DateTimeFormatter formatter) {
    return Objects.requireNonNull(formatter, "formatter").format(toYearMonth());
  }

  @Override
  public int compareTo(@NotNull YearMonthValue other) {
    return toYearMonth().compareTo(Objects.requireNonNull(other, "other").toYearMonth());
  }

  @Override
  public @NotNull String toString() {
    return toYearMonth().toString();
  }
}
