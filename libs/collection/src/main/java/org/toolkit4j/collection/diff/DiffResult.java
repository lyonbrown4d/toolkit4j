package org.toolkit4j.collection.diff;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import java.util.Objects;

@RecordBuilder
public record DiffResult<K, L, R>(
    List<DiffItem<K, R>> added,
    List<DiffItem<K, L>> removed,
    List<DiffPair<K, L, R>> unchanged,
    List<DiffPair<K, L, R>> changed) {

  public DiffResult {
    added = List.copyOf(Objects.requireNonNull(added, "added"));
    removed = List.copyOf(Objects.requireNonNull(removed, "removed"));
    unchanged = List.copyOf(Objects.requireNonNull(unchanged, "unchanged"));
    changed = List.copyOf(Objects.requireNonNull(changed, "changed"));
  }

  public boolean hasChanges() {
    return !added.isEmpty() || !removed.isEmpty() || !changed.isEmpty();
  }

  public int totalCompared() {
    return added.size() + removed.size() + unchanged.size() + changed.size();
  }
}
