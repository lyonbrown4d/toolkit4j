package org.toolkit4j.collection.tree;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;

/**
 * 树构建入口。Single entry point for building trees.
 *
 * <p>中文：从扁平列表构建树，参数简单。
 *
 * <p>English: Build a tree from a flat list with minimal parameters.
 */
@UtilityClass
public class Trees {

  /**
   * 从扁平列表构建树。Build tree from flat list.
   *
   * @param flat 扁平节点列表，需实现 FlatNode
   */
  public <T extends FlatNode<ID>, ID> ListTree<T> build(@NonNull Collection<T> flat) {
    return build(flat, null);
  }

  /**
   * 从扁平列表构建树，使用提取器（无需实现 FlatNode）。 Build tree using extractors when FlatNode is not implemented.
   *
   * @param flat 扁平列表
   * @param id id 提取器
   * @param parentId parentId 提取器
   */
  public <T, ID> ListTree<T> build(
      @NonNull Collection<T> flat, @NonNull Function<T, ID> id, @NonNull Function<T, ID> parentId) {
    return build(flat, id, parentId, null);
  }

  /** 从扁平列表构建树，使用提取器并可指定兄弟排序。 */
  public <T, ID> ListTree<T> build(
      @NonNull Collection<T> flat,
      @NonNull Function<T, ID> id,
      @NonNull Function<T, ID> parentId,
      Comparator<? super T> siblingSort) {
    return buildList(flat, id, parentId, siblingSort);
  }

  /** 构建 ListTree（根与子节点有序）。Build ListTree with ordered roots and children. */
  public <T, ID> ListTree<T> buildList(
      @NonNull Collection<T> flat,
      @NonNull Function<T, ID> id,
      @NonNull Function<T, ID> parentId,
      Comparator<? super T> siblingSort) {
    if (flat.isEmpty()) return new ListTreeImpl<>(List.of());

    val nodeMap = flat.stream().collect(Collectors.toMap(id, n -> n));
    Map<ID, List<T>> childrenMap = groupChildren(flat, parentId, nodeMap, ArrayList::new);

    val rootsStream =
        flat.stream()
            .filter(
                n -> {
                  val pid = parentId.apply(n);
                  return pid == null || !nodeMap.containsKey(pid);
                })
            .map(n -> buildNode(n, id, childrenMap, siblingSort));
    val roots =
        siblingSort == null
            ? rootsStream.toList()
            : rootsStream.sorted(comparing(TreeNode::data, siblingSort)).toList();

    return new ListTreeImpl<>(roots);
  }

  /** 构建 SetTree（根与子节点为 Set，无顺序）。 Build SetTree with Set roots and children. */
  public <T extends FlatNode<ID>, ID> SetTree<T> buildSet(@NonNull Collection<T> flat) {
    return buildSet(flat, FlatNode::id, FlatNode::parentId);
  }

  /** 构建 SetTree，使用提取器。 */
  public <T, ID> SetTree<T> buildSet(
      @NonNull Collection<T> flat, @NonNull Function<T, ID> id, @NonNull Function<T, ID> parentId) {
    if (flat.isEmpty()) return new SetTreeImpl<>(Set.of());

    val nodeMap = flat.stream().collect(Collectors.toMap(id, n -> n));
    Map<ID, Set<T>> childrenMap = groupChildren(flat, parentId, nodeMap, HashSet::new);

    val roots =
        flat.stream()
            .filter(
                n -> {
                  val pid = parentId.apply(n);
                  return pid == null || !nodeMap.containsKey(pid);
                })
            .map(n -> buildSetNode(n, id, childrenMap))
            .collect(toSet());

    return new SetTreeImpl<>(roots);
  }

  private <T, ID> @NotNull TreeNode<T> buildSetNode(
      T data, @NotNull Function<T, ID> id, @NotNull Map<ID, Set<T>> childrenMap) {
    val childSet = childrenMap.getOrDefault(id.apply(data), Set.of());
    val children = childSet.stream().map(c -> buildSetNode(c, id, childrenMap)).collect(toSet());
    return new SetTreeNodeImpl<>(data, children);
  }

  /**
   * 构建 LinkedTree（根与子节点为 LinkedHashSet，保持插入顺序）。 Build LinkedTree with LinkedHashSet, preserves
   * insertion order.
   */
  public <T extends FlatNode<ID>, ID> LinkedTree<T> buildLinked(@NonNull Collection<T> flat) {
    return buildLinked(flat, FlatNode::id, FlatNode::parentId);
  }

  public <T, ID> LinkedTree<T> buildLinked(
      @NonNull Collection<T> flat, @NonNull Function<T, ID> id, @NonNull Function<T, ID> parentId) {
    if (flat.isEmpty()) return new LinkedTreeImpl<>(new LinkedHashSet<>());

    val nodeMap = flat.stream().collect(Collectors.toMap(id, n -> n));
    Map<ID, LinkedHashSet<T>> childrenMap =
        groupChildren(flat, parentId, nodeMap, LinkedHashSet::new);

    val roots =
        flat.stream()
            .filter(
                n -> {
                  val pid = parentId.apply(n);
                  return pid == null || !nodeMap.containsKey(pid);
                })
            .map(n -> buildLinkedNode(n, id, childrenMap))
            .collect(toCollection(LinkedHashSet::new));

    return new LinkedTreeImpl<>(roots);
  }

  private <T, ID> @NotNull TreeNode<T> buildLinkedNode(
      T data, @NotNull Function<T, ID> id, @NotNull Map<ID, LinkedHashSet<T>> childrenMap) {
    val childSet = childrenMap.getOrDefault(id.apply(data), new LinkedHashSet<>());
    val children =
        childSet.stream()
            .map(c -> buildLinkedNode(c, id, childrenMap))
            .collect(toCollection(LinkedHashSet::new));
    return new SetTreeNodeImpl<>(data, children);
  }

  /**
   * 构建 SortedTree（根与子节点按 comparator 排序）。 Build SortedTree with roots and children sorted by
   * comparator.
   */
  public <T extends FlatNode<ID>, ID> SortedTree<T> buildSorted(
      @NonNull Collection<T> flat, @NonNull Comparator<? super T> comparator) {
    return buildSorted(flat, FlatNode::id, FlatNode::parentId, comparator);
  }

  public <T, ID> SortedTree<T> buildSorted(
      @NonNull Collection<T> flat,
      @NonNull Function<T, ID> id,
      @NonNull Function<T, ID> parentId,
      @NonNull Comparator<? super T> comparator) {
    if (flat.isEmpty())
      return new SortedTreeImpl<>(
          new TreeSet<>(comparing((TreeNode<T> n) -> n.data(), comparator)));

    val nodeMap = flat.stream().collect(Collectors.toMap(id, n -> n));
    Map<ID, List<T>> childrenMap = groupChildren(flat, parentId, nodeMap, ArrayList::new);

    val nodeComparator = Comparator.<TreeNode<T>, T>comparing(TreeNode::data, comparator);
    val roots =
        flat.stream()
            .filter(
                n -> {
                  val pid = parentId.apply(n);
                  return pid == null || !nodeMap.containsKey(pid);
                })
            .map(n -> buildSortedNode(n, id, childrenMap, comparator))
            .collect(toCollection(() -> new TreeSet<>(nodeComparator)));

    return new SortedTreeImpl<>(roots);
  }

  private <T, ID> @NotNull TreeNode<T> buildSortedNode(
      T data,
      @NotNull Function<T, ID> id,
      @NotNull Map<ID, List<T>> childrenMap,
      Comparator<? super T> comparator) {
    val childList =
        childrenMap.getOrDefault(id.apply(data), List.<T>of()).stream().sorted(comparator).toList();
    val nodeComparator = Comparator.<TreeNode<T>, T>comparing(TreeNode::data, comparator);
    val children =
        childList.stream()
            .map(c -> buildSortedNode(c, id, childrenMap, comparator))
            .collect(toCollection(() -> new TreeSet<>(nodeComparator)));
    return new SortedTreeNodeImpl<>(data, children);
  }

  /**
   * 从扁平列表构建树，兄弟节点按 comparator 排序。 Build tree with siblings sorted by comparator.
   *
   * @param flat 扁平节点列表
   * @param siblingSort 兄弟排序，可为 null
   */
  public <T extends FlatNode<ID>, ID> ListTree<T> build(
      @NonNull Collection<T> flat, Comparator<? super T> siblingSort) {
    return build(flat, FlatNode::id, FlatNode::parentId, siblingSort);
  }

  private <T, ID> @NotNull TreeNode<T> buildNode(
      T data,
      @NotNull Function<T, ID> id,
      @NotNull Map<ID, List<T>> childrenMap,
      Comparator<? super T> siblingSort) {
    val childList = childrenMap.getOrDefault(id.apply(data), List.<T>of());
    val sorted = siblingSort == null ? childList : childList.stream().sorted(siblingSort).toList();
    val children = sorted.stream().map(c -> buildNode(c, id, childrenMap, siblingSort)).toList();
    return new ListTreeNodeImpl<>(data, children);
  }

  private <T, ID, C extends Collection<T>> @NotNull Map<ID, C> groupChildren(
      @NotNull Collection<T> flat,
      @NotNull Function<T, ID> parentId,
      @NotNull Map<ID, T> nodeMap,
      @NotNull Supplier<C> childrenFactory) {
    return flat.stream()
        .map(node -> new ChildCandidate<>(parentId.apply(node), node))
        .filter(
            candidate ->
                candidate.parentId() != null && nodeMap.containsKey(candidate.parentId()))
        .collect(
            Collectors.groupingBy(
                ChildCandidate::parentId,
                HashMap::new,
                Collectors.mapping(ChildCandidate::node, toCollection(childrenFactory))));
  }

  private record ChildCandidate<ID, T>(ID parentId, T node) {}
}
