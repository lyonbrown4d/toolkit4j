package org.toolkit4j.collection.diff;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class CollectionDiff {
  public static <K, V> @NotNull DiffResult<K, V, V> compare(
      @NotNull Iterable<? extends V> previous,
      @NotNull Iterable<? extends V> current,
      @NotNull Function<? super V, ? extends K> keyExtractor) {
    return compare(previous, current, keyExtractor, Objects::equals);
  }

  public static <K, V> @NotNull DiffResult<K, V, V> compare(
      @NotNull Iterable<? extends V> previous,
      @NotNull Iterable<? extends V> current,
      @NotNull Function<? super V, ? extends K> keyExtractor,
      @NotNull BiPredicate<? super V, ? super V> sameValue) {
    return compareByKey(previous, current, keyExtractor, keyExtractor, sameValue);
  }

  public static <K, L, R> @NotNull DiffResult<K, L, R> compareByKey(
      @NotNull Iterable<? extends L> previous,
      @NotNull Iterable<? extends R> current,
      @NotNull Function<? super L, ? extends K> previousKeyExtractor,
      @NotNull Function<? super R, ? extends K> currentKeyExtractor,
      @NotNull BiPredicate<? super L, ? super R> sameValue) {
    Objects.requireNonNull(sameValue, "sameValue");

    var previousByKey = index(previous, previousKeyExtractor, "previous");
    var currentByKey = index(current, currentKeyExtractor, "current");

    var removed =
        previousByKey.entrySet().stream()
            .filter(entry -> !currentByKey.containsKey(entry.getKey()))
            .map(entry -> new DiffItem<>(entry.getKey(), entry.getValue()))
            .toList();
    var added =
        currentByKey.entrySet().stream()
            .filter(entry -> !previousByKey.containsKey(entry.getKey()))
            .map(entry -> new DiffItem<>(entry.getKey(), entry.getValue()))
            .toList();
    var pairs =
        previousByKey.entrySet().stream()
            .filter(entry -> currentByKey.containsKey(entry.getKey()))
            .map(
                entry -> {
                  var key = entry.getKey();
                  var previousValue = entry.getValue();
                  var currentValue = currentByKey.get(key);
                  var pair = new DiffPair<>(key, previousValue, currentValue);
                  return new ClassifiedDiffPair<>(
                      pair, sameValue.test(previousValue, currentValue));
                })
            .toList();
    var unchanged =
        pairs.stream()
            .filter(ClassifiedDiffPair::unchanged)
            .map(ClassifiedDiffPair::pair)
            .toList();
    var changed =
        pairs.stream()
            .filter(pair -> !pair.unchanged())
            .map(ClassifiedDiffPair::pair)
            .toList();

    return new DiffResult<>(added, removed, unchanged, changed);
  }

  private static <K, V> LinkedHashMap<K, V> index(
      Iterable<? extends V> values,
      Function<? super V, ? extends K> keyExtractor,
      String sideName) {
    Objects.requireNonNull(values, sideName);
    Objects.requireNonNull(keyExtractor, sideName + "KeyExtractor");

    return StreamSupport.stream(values.spliterator(), false)
        .collect(
            Collector.of(
                () ->
                    values instanceof Collection<?> collection
                        ? LinkedHashMap.<K, V>newLinkedHashMap(collection.size())
                        : new LinkedHashMap<K, V>(),
                (indexed, value) -> putIndexed(indexed, value, keyExtractor, sideName),
                (left, right) -> mergeIndexed(left, right, sideName)));
  }

  private static <K, V> void putIndexed(
      LinkedHashMap<K, V> indexed,
      V value,
      Function<? super V, ? extends K> keyExtractor,
      String sideName) {
    var key = Objects.requireNonNull(keyExtractor.apply(value), sideName + " key");
    putUnique(indexed, key, value, sideName);
  }

  private static <K, V> LinkedHashMap<K, V> mergeIndexed(
      LinkedHashMap<K, V> left, LinkedHashMap<K, V> right, String sideName) {
    right.forEach((key, value) -> putUnique(left, key, value, sideName));
    return left;
  }

  private static <K, V> void putUnique(
      LinkedHashMap<K, V> indexed, K key, V value, String sideName) {
    if (indexed.containsKey(key)) {
      throw new IllegalArgumentException("Duplicate %s key: %s".formatted(sideName, key));
    }
    indexed.put(key, value);
  }

  private record ClassifiedDiffPair<K, L, R>(DiffPair<K, L, R> pair, boolean unchanged) {}
}
