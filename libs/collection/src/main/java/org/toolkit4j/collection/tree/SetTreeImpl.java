package org.toolkit4j.collection.tree;

import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

record SetTreeImpl<T>(Set<TreeNode<T>> roots) implements SetTree<T> {

  @Override
  public Stream<TreeNode<T>> stream() {
    return roots.stream().flatMap(TreeNode::stream);
  }

  @Override
  public @NotNull Stream<TreeNode<T>> breadthFirst() {
    return TreeStreams.breadthFirst(roots);
  }
}
