package org.toolkit4j.net;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** Port Range */
@EqualsAndHashCode
@Getter
public final class PortRange {
  private final int start;
  private final int end;

  private PortRange(int start, int end) {
    if (start < 0 || end < 0 || end > 65535 || start > end) {
      throw new IllegalArgumentException("Invalid port range: " + start + "-" + end);
    }
    this.start = start;
    this.end = end;
  }

  @Contract(value = "_, _ -> new", pure = true)
  public static @NotNull PortRange of(int start, int end) {
    return new PortRange(start, end);
  }

  public boolean contains(int port) {
    return port >= start && port <= end;
  }

  @Override
  public String toString() {
    return start == end ? String.valueOf(start) : start + "-" + end;
  }
}
