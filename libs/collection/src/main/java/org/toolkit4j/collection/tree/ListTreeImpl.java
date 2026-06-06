package org.toolkit4j.collection.tree;

import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

record ListTreeImpl<T>(List<TreeNode<T>> roots) implements ListTree<T> {

  @Override
  public Stream<TreeNode<T>> stream() {
    return roots.stream().flatMap(TreeNode::stream);
  }

  @Override
  public @NotNull Stream<TreeNode<T>> breadthFirst() {
    return TreeStreams.breadthFirst(roots);
  }
}
