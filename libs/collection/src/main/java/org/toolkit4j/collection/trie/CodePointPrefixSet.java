package org.toolkit4j.collection.trie;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * 基于代码点的前缀匹配集合。Prefix set backed by code-point based Trie.
 *
 * <p>对非 BMP 字符（如 emoji）更友好，避免以 UTF-16 code unit 划分前缀。
 */
public class CodePointPrefixSet {

  private final Trie<Integer, Boolean> trie;
  private int size = 0;

  public CodePointPrefixSet() {
    this.trie = new HashMapTrie<>();
  }

  public CodePointPrefixSet(@NotNull Iterable<String> values) {
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
        .map(CodePointPrefixSet::toValue)
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

  private static List<Integer> toKey(@NotNull String value) {
    Objects.requireNonNull(value, "value");
    return value.codePoints().boxed().toList();
  }

  private static String toValue(@NotNull List<Integer> key) {
    Objects.requireNonNull(key, "key");
    return key.stream().map(Character::toString).collect(Collectors.joining());
  }
}
