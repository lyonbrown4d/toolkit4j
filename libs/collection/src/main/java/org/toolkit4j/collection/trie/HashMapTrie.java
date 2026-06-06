package org.toolkit4j.collection.trie;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public class HashMapTrie<K, V> implements Trie<K, V> {
  private final TrieNode<K, V> root;
  private final Supplier<TrieNode<K, V>> nodeSupplier;

  public HashMapTrie() {
    this(HashTrieNode::new);
  }

  public HashMapTrie(@NotNull Supplier<TrieNode<K, V>> nodeSupplier) {
    this.nodeSupplier = nodeSupplier;
    this.root = nodeSupplier.get();
  }

  @Override
  public void insert(@NotNull Iterable<K> keySequence, V value) {
    var node = root;
    for (K key : keySequence) {
      var child = node.getChild(key);
      if (child == null) {
        child = nodeSupplier.get();
        node.setChild(key, child);
      }
      node = child;
    }
    node.setEnd(true);
    node.setValue(value);
  }

  @Override
  public V search(Iterable<K> keySequence) {
    val node = findNode(keySequence);
    return node != null && node.isEnd() ? node.getValue() : null;
  }

  @Override
  public boolean startsWith(Iterable<K> prefixSequence) {
    return findNode(prefixSequence) != null;
  }

  @Override
  public boolean delete(@NotNull Iterable<K> keySequence) {
    val found = new boolean[1];
    delete(root, keySequence.iterator(), found);
    return found[0];
  }

  private boolean delete(TrieNode<K, V> current, @NotNull Iterator<K> iterator, boolean[] found) {
    if (!iterator.hasNext()) {
      if (!current.isEnd()) return false;
      current.setEnd(false);
      current.setValue(null);
      found[0] = true;
      return current.getChildren().isEmpty();
    }
    K key = iterator.next();
    val child = current.getChild(key);
    if (child == null) return false;

    boolean shouldDeleteChild = delete(child, iterator, found);

    if (shouldDeleteChild) {
      current.getChildren().remove(key);
      return !current.isEnd() && current.getChildren().isEmpty();
    }
    return false;
  }

  @Override
  public Set<List<K>> keysWithPrefix(@NotNull Iterable<K> prefixSequence) {
    val prefixList = StreamSupport.stream(prefixSequence.spliterator(), false).toList();
    val node = findNode(prefixList);
    if (node == null) {
      return Collections.emptySet();
    }
    return keysFrom(node, prefixList).collect(Collectors.toSet());
  }

  private @NotNull Stream<List<K>> keysFrom(@NotNull TrieNode<K, V> node, @NotNull List<K> path) {
    val current = node.isEnd() ? Stream.of(List.copyOf(path)) : Stream.<List<K>>empty();
    val descendants =
        node.getChildren().entrySet().stream()
            .flatMap(
                entry -> {
                  val childPath = new ArrayList<>(path);
                  childPath.add(entry.getKey());
                  return keysFrom(entry.getValue(), childPath);
                });
    return Stream.concat(current, descendants);
  }

  @Override
  public void clear() {
    root.getChildren().clear();
    root.setEnd(false);
    root.setValue(null);
  }

  @Override
  public boolean isEmpty() {
    return root.getChildren().isEmpty();
  }

  /** Walks from root following {@code sequence}. Returns {@code null} if any step is missing. */
  private @Nullable TrieNode<K, V> findNode(@NotNull Iterable<K> sequence) {
    var node = root;
    for (K key : sequence) {
      node = node.getChild(key);
      if (node == null) {
        return null;
      }
    }
    return node;
  }
}
