package org.toolkit4j.collection.tree;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 树形结构接口。Tree interface.
 *
 * <p>子类可细化 roots 的集合类型，如 ListTree、SetTree。 Sub-interfaces may narrow roots to List or Set.
 *
 * @param <T> 节点数据类型
 */
public interface Tree<T> {

  /** 根节点集合。Root nodes. 实现可为 List 或 Set 等。 */
  Collection<TreeNode<T>> roots();

  /** DFS 遍历。Depth-first stream. */
  Stream<TreeNode<T>> stream();

  /** BFS 遍历。Breadth-first stream. */
  Stream<TreeNode<T>> breadthFirst();

  /** 查找第一个匹配节点。Find first matching node. */
  default Optional<TreeNode<T>> find(Predicate<T> predicate) {
    return stream().filter(n -> predicate.test(n.data())).findFirst();
  }

  /** 从根到第一个匹配节点的路径。Path from root to first matching node. */
  default List<TreeNode<T>> pathTo(Predicate<T> predicate) {
    return roots().stream()
        .map(root -> pathFrom(root, predicate))
        .filter(path -> !path.isEmpty())
        .findFirst()
        .orElseGet(List::of);
  }

  private static <T> List<TreeNode<T>> pathFrom(TreeNode<T> node, Predicate<T> predicate) {
    if (predicate.test(node.data())) return List.of(node);
    return node.children().stream()
        .map(child -> pathFrom(child, predicate))
        .filter(path -> !path.isEmpty())
        .findFirst()
        .map(path -> Stream.concat(Stream.of(node), path.stream()).toList())
        .orElseGet(List::of);
  }
}
