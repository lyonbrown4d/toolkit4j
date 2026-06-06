package org.toolkit4j.collection.table;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractTable<R, C, V> implements Table<R, C, V> {
  private final AtomicInteger size = new AtomicInteger(0);

  protected abstract Map<R, Map<C, V>> getBackingMap();

  /** 创建新的列 map，子类可重写以使用不同的 map 实现，比如 concurrent 或 fastutil */
  protected Map<C, V> createColumnMap() {
    return new HashMap<>();
  }

  @Override
  public V get(R rowKey, C columnKey) {
    return Optional.ofNullable(getBackingMap().get(rowKey))
        .map(row -> row.get(columnKey))
        .orElse(null);
  }

  @Override
  public void put(R rowKey, C columnKey, V value) {
    Map<C, V> row = getBackingMap().computeIfAbsent(rowKey, k -> createColumnMap());
    V old = row.put(columnKey, value);
    if (old == null) {
      size.incrementAndGet();
    }
  }

  @Override
  public V remove(R rowKey, C columnKey) {
    Map<C, V> row = getBackingMap().get(rowKey);
    if (row == null) return null;
    V removed = row.remove(columnKey);
    if (removed != null) {
      size.decrementAndGet();
      if (row.isEmpty()) {
        getBackingMap().remove(rowKey);
      }
    }
    return removed;
  }

  @Override
  public boolean contains(R rowKey, C columnKey) {
    return Optional.ofNullable(getBackingMap().get(rowKey))
        .map(row -> row.containsKey(columnKey))
        .orElse(false);
  }

  @Override
  public boolean containsRow(R rowKey) {
    return getBackingMap().containsKey(rowKey);
  }

  @Override
  public boolean containsColumn(C columnKey) {
    return getBackingMap().values().stream().anyMatch(row -> row.containsKey(columnKey));
  }

  @Override
  public Map<C, V> row(R rowKey) {
    return Optional.ofNullable(getBackingMap().get(rowKey))
        .map(Collections::unmodifiableMap)
        .orElseGet(Collections::emptyMap);
  }

  @Override
  public Map<R, V> column(C columnKey) {
    return getBackingMap().entrySet().stream()
        .filter(e -> e.getValue().containsKey(columnKey))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().get(columnKey), (a, b) -> b, HashMap::new));
  }

  @Override
  public Map<R, Map<C, V>> rowMap() {
    return Collections.unmodifiableMap(getBackingMap());
  }

  @Override
  public Set<Cell<R, C, V>> cellSet() {
    return stream().collect(Collectors.toSet());
  }

  @Override
  public Stream<Cell<R, C, V>> stream() {
    return getBackingMap().entrySet().stream()
        .flatMap(
            rowEntry ->
                rowEntry.getValue().entrySet().stream()
                    .map(
                        cellEntry ->
                            new RecordCell<>(
                                rowEntry.getKey(), cellEntry.getKey(), cellEntry.getValue())));
  }

  @Override
  public void clear() {
    getBackingMap().clear();
    size.set(0);
  }

  @Override
  public boolean isEmpty() {
    return getBackingMap().isEmpty();
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public Table<R, C, V> filter(BiPredicate<R, C> predicate) {
    AbstractTable<R, C, V> filtered = createInstance();
    stream()
        .filter(cell -> predicate.test(cell.getRowKey(), cell.getColumnKey()))
        .forEach(cell -> filtered.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue()));
    return filtered;
  }

  @Override
  public <V2> Table<R, C, V2> mapValues(Function<? super V, ? extends V2> mapper) {
    AbstractTable<R, C, V2> mapped = createInstance();
    stream()
        .forEach(
            cell ->
                mapped.put(cell.getRowKey(), cell.getColumnKey(), mapper.apply(cell.getValue())));
    return mapped;
  }

  /** 子类必须实现此方法，用于返回新的实例，方便 filter、mapValues 返回相同实现类 */
  protected abstract <V2> AbstractTable<R, C, V2> createInstance();
}
