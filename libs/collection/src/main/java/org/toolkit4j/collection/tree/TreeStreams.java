package org.toolkit4j.collection.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

final class TreeStreams {
  private TreeStreams() {}

  static <T> @NotNull Stream<TreeNode<T>> breadthFirst(@NotNull Collection<TreeNode<T>> roots) {
    var queue = new ArrayDeque<>(roots);
    return Stream.generate(
            () -> {
              if (queue.isEmpty()) {
                return null;
              }
              var node = queue.remove();
              queue.addAll(node.children());
              return node;
            })
        .takeWhile(Objects::nonNull);
  }
}
