package org.toolkit4j.collection.trie;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * 基于 Trie 的前缀匹配集合。Prefix set backed by Trie.
 *
 * <p>适用于“前缀查询场景”（如路由匹配、关键字提示、标签分类）。
 */
public class PrefixSet {

  private final Trie<Character, Boolean> trie;
  private int size = 0;

  public PrefixSet() {
    this.trie = new HashMapTrie<>();
  }

  public PrefixSet(@NotNull Iterable<String> values) {
    this();
    Objects.requireNonNull(values, "values");
    values.forEach(this::add);
  }

  public boolean add(@NotNull String value) {
    Objects.requireNonNull(value, "value");
    if (contains(value)) {
      return false;
    }
    trie.insert(toKey(value), Boolean.TRUE);
    size++;
    return true;
  }

  public boolean remove(@NotNull String value) {
    Objects.requireNonNull(value, "value");
    if (trie.delete(toKey(value))) {
      size--;
      return true;
    }
    return false;
  }

  public boolean contains(@NotNull String value) {
    Objects.requireNonNull(value, "value");
    return trie.search(toKey(value)) != null;
  }

  public boolean containsPrefix(@NotNull String prefix) {
    Objects.requireNonNull(prefix, "prefix");
    if (size == 0) {
      return false;
    }
    return trie.startsWith(toKey(prefix));
  }

  public Set<String> valuesWithPrefix(@NotNull String prefix) {
    Objects.requireNonNull(prefix, "prefix");
    return trie.keysWithPrefix(toKey(prefix)).stream()
        .map(PrefixSet::toValue)
        .collect(Collectors.toSet());
  }

  public void clear() {
    trie.clear();
    size = 0;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  private static List<Character> toKey(@NotNull String value) {
    Objects.requireNonNull(value, "value");
    return value.chars().mapToObj(c -> (char) c).toList();
  }

  private static String toValue(@NotNull List<Character> key) {
    Objects.requireNonNull(key, "key");
    var builder = new StringBuilder(key.size());
    key.forEach(builder::append);
    return builder.toString();
  }
}
