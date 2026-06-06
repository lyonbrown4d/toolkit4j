package org.toolkit4j.collection.tree;

import java.util.SortedSet;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

record SortedTreeImpl<T>(SortedSet<TreeNode<T>> roots) implements SortedTree<T> {

  @Override
  public Stream<TreeNode<T>> stream() {
    return roots.stream().flatMap(TreeNode::stream);
  }

  @Override
  public @NotNull Stream<TreeNode<T>> breadthFirst() {
    return TreeStreams.breadthFirst(roots);
  }
}
