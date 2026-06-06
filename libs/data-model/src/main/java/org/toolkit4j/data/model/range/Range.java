package org.toolkit4j.data.model.range;

import io.soabase.recordbuilder.core.RecordBuilder;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RecordBuilder
public record Range<T extends Comparable<? super T>>(Bound<T> lower, Bound<T> upper) {
  public Range {
    if (lower != null && upper != null && lower.value().compareTo(upper.value()) > 0) {
      throw new IllegalArgumentException("lower bound must be less than or equal to upper bound");
    }
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> unbounded() {
    return new Range<>(null, null);
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> closed(
      @NotNull T lower, @NotNull T upper) {
    return new Range<>(Bound.closed(lower), Bound.closed(upper));
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> open(
      @NotNull T lower, @NotNull T upper) {
    return new Range<>(Bound.open(lower), Bound.open(upper));
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> closedOpen(
      @NotNull T lower, @NotNull T upper) {
    return new Range<>(Bound.closed(lower), Bound.open(upper));
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> openClosed(
      @NotNull T lower, @NotNull T upper) {
    return new Range<>(Bound.open(lower), Bound.closed(upper));
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> atLeast(@NotNull T lower) {
    return new Range<>(Bound.closed(lower), null);
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> greaterThan(@NotNull T lower) {
    return new Range<>(Bound.open(lower), null);
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> atMost(@NotNull T upper) {
    return new Range<>(null, Bound.closed(upper));
  }

  public static <T extends Comparable<? super T>> @NotNull Range<T> lessThan(@NotNull T upper) {
    return new Range<>(null, Bound.open(upper));
  }

  public boolean hasLowerBound() {
    return lower != null;
  }

  public boolean hasUpperBound() {
    return upper != null;
  }

  public boolean isEmpty() {
    if (lower == null || upper == null) {
      return false;
    }
    val comparison = lower.value().compareTo(upper.value());
    if (comparison < 0) {
      return false;
    }
    return lower.type() == BoundType.OPEN || upper.type() == BoundType.OPEN;
  }

  public boolean contains(@Nullable T value) {
    if (value == null || isEmpty()) {
      return false;
    }
    return matchesLowerBound(value) && matchesUpperBound(value);
  }

  private boolean matchesLowerBound(T value) {
    if (lower == null) {
      return true;
    }
    val comparison = value.compareTo(lower.value());
    return lower.type() == BoundType.CLOSED ? comparison >= 0 : comparison > 0;
  }

  private boolean matchesUpperBound(T value) {
    if (upper == null) {
      return true;
    }
    val comparison = value.compareTo(upper.value());
    return upper.type() == BoundType.CLOSED ? comparison <= 0 : comparison < 0;
  }
}
