package org.toolkit4j.collection.pageable;

import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

/**
 * Paging over a {@link Set} using the backing collection's iterator order. For {@link
 * java.util.HashSet}, that order is unspecified but stable until the set is modified. Use {@link
 * java.util.LinkedHashSet} if you need a predictable order.
 */
@Data
@RequiredArgsConstructor
public class PageableSet<T> implements PageableCollection<T, Set<T>> {

  private final Set<T> set;
  private int pageNo = 1;
  private int pageSize = 10;

  @Override
  public Set<T> page(int pageNo, int pageSize) {
    checkPageArgument(pageNo, pageSize);
    val fromIndex = (pageNo - 1) * pageSize;
    val total = set.size();
    if (fromIndex >= total) {
      return emptySet();
    }
    val toIndex = Math.min(fromIndex + pageSize, total);
    return copyIteratorRange(fromIndex, toIndex - fromIndex);
  }

  @Override
  public int totalPage(int pageSize) {
    return (int) Math.ceil((double) set.size() / pageSize);
  }

  @Override
  public int current() {
    return pageNo;
  }

  @Override
  public int totalSize() {
    return set.size();
  }

  @Override
  public boolean hasNextPage() {
    return pageNo < totalPage(pageSize);
  }

  @Override
  public boolean hasPreviousPage() {
    return pageNo > 1;
  }

  @Override
  public int getNextPage() {
    return hasNextPage() ? pageNo + 1 : pageNo;
  }

  @Override
  public int getPreviousPage() {
    return hasPreviousPage() ? pageNo - 1 : pageNo;
  }

  @Override
  public Stream<T> stream() {
    return set.stream();
  }

  @Override
  public Set<T> slice(int fromIndex, int toIndex) {
    val size = set.size();
    if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException(
          "fromIndex=%d, toIndex=%d, size=%d".formatted(fromIndex, toIndex, size));
    }
    if (fromIndex == toIndex) {
      return emptySet();
    }
    return copyIteratorRange(fromIndex, toIndex - fromIndex);
  }

  /**
   * Copies {@code length} elements starting after skipping {@code skip} iterator steps. Uses the
   * same order as {@link Set#iterator()}.
   */
  private @NotNull Set<T> copyIteratorRange(int skip, int length) {
    if (length <= 0) {
      return emptySet();
    }
    val out =
        set.stream()
            .skip(skip)
            .limit(length)
            .collect(Collectors.toCollection(() -> HashSet.<T>newHashSet(length)));
    if (out.size() < length) {
      throw new IndexOutOfBoundsException(
          "expected %d elements from offset %d but iterator ended early (size=%d)"
              .formatted(length, skip, set.size()));
    }
    return out;
  }
}
