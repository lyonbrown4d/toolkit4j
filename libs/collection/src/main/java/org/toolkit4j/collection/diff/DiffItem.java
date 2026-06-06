package org.toolkit4j.collection.diff;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;

@RecordBuilder
public record DiffItem<K, V>(K key, V value) {
  public DiffItem {
    Objects.requireNonNull(key, "key");
  }
}
