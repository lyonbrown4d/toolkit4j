package org.toolkit4j.collection.diff;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;

@RecordBuilder
public record DiffPair<K, L, R>(K key, L left, R right) {
  public DiffPair {
    Objects.requireNonNull(key, "key");
  }
}
