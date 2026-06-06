package org.toolkit4j.data.model.measure;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

@RecordBuilder
public record DataSize(long bytes) implements Comparable<DataSize> {
  private static final Pattern PARSE_PATTERN =
      Pattern.compile("^([+]?\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)?$");

  public DataSize {
    if (bytes < 0) {
      throw new IllegalArgumentException("bytes must be greater than or equal to 0");
    }
  }

  public static @NotNull DataSize of(long amount, @NotNull DataUnit unit) {
    return new DataSize(Objects.requireNonNull(unit, "unit").toBytes(amount));
  }

  public static @NotNull DataSize bytes(long bytes) {
    return new DataSize(bytes);
  }

  public static @NotNull DataSize kilobytes(long amount) {
    return of(amount, DataUnit.KILOBYTES);
  }

  public static @NotNull DataSize megabytes(long amount) {
    return of(amount, DataUnit.MEGABYTES);
  }

  public static @NotNull DataSize kibibytes(long amount) {
    return of(amount, DataUnit.KIBIBYTES);
  }

  public static @NotNull DataSize mebibytes(long amount) {
    return of(amount, DataUnit.MEBIBYTES);
  }

  public static @NotNull DataSize parse(@NotNull String text) {
    var matcher = PARSE_PATTERN.matcher(Objects.requireNonNull(text, "text").trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid data size: " + text);
    }

    var amount = new BigDecimal(matcher.group(1));
    var unit = DataUnit.parse(matcher.group(2) == null ? "" : matcher.group(2));
    return new DataSize(exactBytes(unit.toBytes(amount)));
  }

  public long to(@NotNull DataUnit unit) {
    return bytes / Objects.requireNonNull(unit, "unit").bytes();
  }

  public @NotNull BigDecimal toDecimal(@NotNull DataUnit unit) {
    return BigDecimal.valueOf(bytes)
        .divide(
            BigDecimal.valueOf(Objects.requireNonNull(unit, "unit").bytes()),
            MathContext.DECIMAL128);
  }

  public @NotNull DataSize plus(@NotNull DataSize other) {
    return new DataSize(Math.addExact(bytes, Objects.requireNonNull(other, "other").bytes));
  }

  public @NotNull DataSize minus(@NotNull DataSize other) {
    return new DataSize(Math.subtractExact(bytes, Objects.requireNonNull(other, "other").bytes));
  }

  public @NotNull DataSize multiply(long multiplier) {
    return new DataSize(Math.multiplyExact(bytes, multiplier));
  }

  @Override
  public int compareTo(@NotNull DataSize other) {
    return Long.compare(bytes, Objects.requireNonNull(other, "other").bytes);
  }

  @Override
  public @NotNull String toString() {
    return bytes + "B";
  }

  private static long exactBytes(BigDecimal value) {
    try {
      return value.stripTrailingZeros().toBigIntegerExact().longValueExact();
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("Data size must resolve to a whole byte count", e);
    }
  }
}
