package org.toolkit4j.collection.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;
import lombok.val;

import static java.util.Collections.emptySet;

/**
 * 集合运算工具类。
 *
 * <p>职责限定为：多集合之间的组合与关系运算，
 * 如拼接、并集、交集、差集、对称差集、子集判断等。
 *
 * <p>设计约定：
 * <ul>
 *   <li>对 {@code null} 集合作为空集合处理</li>
 *   <li>返回结果优先保持遍历顺序</li>
 *   <li>返回类型统一为 {@link List}，便于调用方直接消费</li>
 * </ul>
 */
@UtilityClass
public class CollectionOperations {

  /**
   * 拼接多个集合，不去重，保留原始遍历顺序。
   *
   * @param collections 多个集合
   * @param <T>         元素类型
   * @return 拼接后的列表；若入参为空则返回空列表
   */
  @SafeVarargs
  public <T> List<T> concat(Collection<? extends T>... collections) {
    return streamOf(collections)
      .toList();
  }

  /**
   * 多集合并集，去重，并保留元素首次出现顺序。
   *
   * @param collections 多个集合
   * @param <T>         元素类型
   * @return 并集结果；若入参为空则返回空列表
   */
  @SafeVarargs
  public <T> List<T> union(Collection<? extends T>... collections) {
    return streamOf(collections)
      .distinct()
      .toList();
  }

  /**
   * 多集合交集。
   *
   * <p>语义：
   * 结果中只保留所有集合都包含的元素，且结果去重。
   * 返回顺序以第一个非空集合中的遍历顺序为准。
   *
   * @param collections 多个集合
   * @param <T>         元素类型
   * @return 交集结果；若入参为空或任一集合为空，则返回空列表
   */
  @SafeVarargs
  public <T> List<T> intersection(Collection<? extends T>... collections) {
    if (collections == null || collections.length == 0) {
      return Collections.emptyList();
    }

    val nonNullCollections = Stream.of(collections)
      .filter(Objects::nonNull)
      .toList();

    if (nonNullCollections.isEmpty()) {
      return Collections.emptyList();
    }

    if (nonNullCollections.stream().anyMatch(Collection::isEmpty)) {
      return Collections.emptyList();
    }

    val first = nonNullCollections.getFirst();
    val remainingSets = nonNullCollections.stream()
      .skip(1)
      .map(LinkedHashSet::new)
      .toList();

    return first.stream()
      .<T>map(element -> element)
      .distinct()
      .filter(element -> remainingSets.stream().allMatch(set -> set.contains(element)))
      .toList();
  }

  /**
   * 差集：返回仅存在于左集合、但不存在于右集合的元素。
   *
   * <p>结果去重，顺序以左集合中的遍历顺序为准。
   *
   * @param left  左集合
   * @param right 右集合
   * @param <T>   元素类型
   * @return 差集结果
   */
  public <T> List<T> difference(
    Collection<? extends T> left,
    Collection<? extends T> right
  ) {
    if (left == null || left.isEmpty()) {
      return Collections.emptyList();
    }

    val rightSet = right == null
      ? emptySet()
      : new LinkedHashSet<>(right);

    return left.stream()
      .<T>map(element -> element)
      .distinct()
      .filter(element -> !rightSet.contains(element))
      .toList();
  }

  /**
   * 对称差集：返回只存在于其中一个集合中的元素。
   *
   * <p>等价于：
   * <pre>
   * union(difference(left, right), difference(right, left))
   * </pre>
   *
   * <p>结果去重，前半部分顺序遵循 left，后半部分顺序遵循 right。
   *
   * @param left  左集合
   * @param right 右集合
   * @param <T>   元素类型
   * @return 对称差集结果
   */
  public <T> List<T> symmetricDifference(
    Collection<? extends T> left,
    Collection<? extends T> right
  ) {
    return Stream.concat(
        difference(left, right).stream(),
        difference(right, left).stream()
      )
      .distinct()
      .toList();
  }

  /**
   * 判断 subset 是否为 superset 的子集。
   *
   * <p>约定：
   * <ul>
   *   <li>{@code null} subset 视为空集</li>
   *   <li>{@code null} superset 视为空集</li>
   * </ul>
   *
   * @param subset   候选子集
   * @param superset 候选父集
   * @return 若 subset 中所有元素都包含于 superset，则返回 true
   */
  public boolean isSubset(
    Collection<?> subset,
    Collection<?> superset
  ) {
    if (subset == null || subset.isEmpty()) {
      return true;
    }

    if (superset == null || superset.isEmpty()) {
      return false;
    }

    val supersetSet = new LinkedHashSet<>(superset);
    return supersetSet.containsAll(subset);
  }

  /**
   * 判断 subset 是否为 superset 的真子集。
   *
   * <p>真子集要求：
   * <ul>
   *   <li>subset 是 superset 的子集</li>
   *   <li>subset 与 superset 不相等</li>
   * </ul>
   *
   * @param subset   候选子集
   * @param superset 候选父集
   * @return 若 subset 是 superset 的真子集，则返回 true
   */
  public boolean isProperSubset(
    Collection<?> subset,
    Collection<?> superset
  ) {
    if (!isSubset(subset, superset)) {
      return false;
    }

    val subsetSet = subset == null
      ? emptySet()
      : new LinkedHashSet<>(subset);
    val supersetSet = superset == null
      ? emptySet()
      : new LinkedHashSet<>(superset);

    return !subsetSet.equals(supersetSet);
  }

  @SafeVarargs
  private <T> Stream<T> streamOf(Collection<? extends T>... collections) {
    if (collections == null || collections.length == 0) {
      return Stream.empty();
    }

    return Stream.of(collections)
      .filter(Objects::nonNull)
      .flatMap(Collection::stream);
  }
}
