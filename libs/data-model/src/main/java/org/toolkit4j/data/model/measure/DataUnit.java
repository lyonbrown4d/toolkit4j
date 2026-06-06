package org.toolkit4j.data.model.measure;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public enum DataUnit {
  BYTES("B", 1L),
  KILOBYTES("KB", 1_000L),
  MEGABYTES("MB", 1_000_000L),
  GIGABYTES("GB", 1_000_000_000L),
  TERABYTES("TB", 1_000_000_000_000L),
  KIBIBYTES("KiB", 1_024L),
  MEBIBYTES("MiB", 1_048_576L),
  GIBIBYTES("GiB", 1_073_741_824L),
  TEBIBYTES("TiB", 1_099_511_627_776L);

  private final String symbol;
  private final long bytes;

  DataUnit(String symbol, long bytes) {
    this.symbol = symbol;
    this.bytes = bytes;
  }

  public @NotNull String symbol() {
    return symbol;
  }

  public long bytes() {
    return bytes;
  }

  public long toBytes(long amount) {
    return Math.multiplyExact(amount, bytes);
  }

  BigDecimal toBytes(BigDecimal amount) {
    return Objects.requireNonNull(amount, "amount").multiply(BigDecimal.valueOf(bytes));
  }

  public static @NotNull DataUnit parse(@NotNull String text) {
    var normalized = Objects.requireNonNull(text, "text").trim();
    if (normalized.isEmpty()) {
      return BYTES;
    }

    var upper = normalized.toUpperCase(Locale.ROOT);
    return switch (upper) {
      case "B", "BYTE", "BYTES" -> BYTES;
      case "KB", "K" -> KILOBYTES;
      case "MB", "M" -> MEGABYTES;
      case "GB", "G" -> GIGABYTES;
      case "TB", "T" -> TERABYTES;
      case "KIB", "KI" -> KIBIBYTES;
      case "MIB", "MI" -> MEBIBYTES;
      case "GIB", "GI" -> GIBIBYTES;
      case "TIB", "TI" -> TEBIBYTES;
      default -> throw new IllegalArgumentException("Unsupported data unit: " + text);
    };
  }
}
