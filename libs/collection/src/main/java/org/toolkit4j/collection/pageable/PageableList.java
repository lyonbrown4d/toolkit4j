package org.toolkit4j.collection.pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Data
public class PageableList<T> implements PageableCollection<T, List<T>> {
  private int pageNo;
  private int pageSize;
  private final List<T> data;

  @Override
  public List<T> page(int pageNo, int pageSize) {
    checkPageArgument(pageNo, pageSize);
    val fromIndex = (pageNo - 1) * pageSize;
    if (fromIndex >= data.size()) {
      return List.of();
    }
    val toIndex = Math.min(fromIndex + pageSize, data.size());
    return copyRange(fromIndex, toIndex);
  }

  @Override
  public int totalPage(int pageSize) {
    return (int) Math.ceil((double) data.size() / pageSize);
  }

  @Override
  public int current() {
    return pageNo;
  }

  @Override
  public int totalSize() {
    return data.size();
  }

  @Override
  public boolean hasNextPage() {
    return pageNo * pageSize < data.size();
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
    return data.stream();
  }

  @Override
  public List<T> slice(int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex > data.size() || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException(
          "fromIndex=%d, toIndex=%d, size=%d".formatted(fromIndex, toIndex, data.size()));
    }
    return copyRange(fromIndex, toIndex);
  }

  private List<T> copyRange(int fromIndex, int toIndex) {
    return data.stream()
        .skip(fromIndex)
        .limit(toIndex - fromIndex)
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
