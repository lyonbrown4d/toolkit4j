package org.toolkit4j.data.model.measure;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@RecordBuilder
public record DataRate(BigDecimal bytesPerSecond) implements Comparable<DataRate> {
  private static final BigDecimal NANOS_PER_SECOND = BigDecimal.valueOf(1_000_000_000L);

  public DataRate {
    bytesPerSecond = normalize(Objects.requireNonNull(bytesPerSecond, "bytesPerSecond"));
    if (bytesPerSecond.signum() < 0) {
      throw new IllegalArgumentException("bytesPerSecond must be greater than or equal to 0");
    }
  }

  public static @NotNull DataRate bytesPerSecond(long bytesPerSecond) {
    return new DataRate(BigDecimal.valueOf(bytesPerSecond));
  }

  public static @NotNull DataRate of(
      long amount, @NotNull DataUnit unit, @NotNull Duration duration) {
    return of(DataSize.of(amount, unit), duration);
  }

  public static @NotNull DataRate of(@NotNull DataSize dataSize, @NotNull Duration duration) {
    Objects.requireNonNull(dataSize, "dataSize");
    var seconds = seconds(duration);
    return new DataRate(
        BigDecimal.valueOf(dataSize.bytes()).divide(seconds, MathContext.DECIMAL128));
  }

  public @NotNull BigDecimal to(@NotNull DataUnit unit) {
    return bytesPerSecond.divide(
        BigDecimal.valueOf(Objects.requireNonNull(unit, "unit").bytes()), MathContext.DECIMAL128);
  }

  @Override
  public int compareTo(@NotNull DataRate other) {
    return bytesPerSecond.compareTo(Objects.requireNonNull(other, "other").bytesPerSecond);
  }

  @Override
  public @NotNull String toString() {
    return bytesPerSecond.toPlainString() + "B/s";
  }

  private static BigDecimal seconds(Duration duration) {
    Objects.requireNonNull(duration, "duration");
    if (!duration.isPositive()) {
      throw new IllegalArgumentException("duration must be positive");
    }
    return BigDecimal.valueOf(duration.getSeconds())
        .add(
            BigDecimal.valueOf(duration.getNano())
                .divide(NANOS_PER_SECOND, MathContext.DECIMAL128));
  }

  private static BigDecimal normalize(BigDecimal value) {
    var normalized = value.stripTrailingZeros();
    return normalized.scale() < 0 ? normalized.setScale(0, RoundingMode.HALF_DOWN) : normalized;
  }
}
