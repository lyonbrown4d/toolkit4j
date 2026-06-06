package org.toolkit4j.data.model.range;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@RecordBuilder
public record Bound<T extends Comparable<? super T>>(T value, BoundType type) {
  public Bound {
    Objects.requireNonNull(value, "value");
    Objects.requireNonNull(type, "type");
  }

  public static <T extends Comparable<? super T>> @NotNull Bound<T> open(@NotNull T value) {
    return new Bound<>(value, BoundType.OPEN);
  }

  public static <T extends Comparable<? super T>> @NotNull Bound<T> closed(@NotNull T value) {
    return new Bound<>(value, BoundType.CLOSED);
  }
}
