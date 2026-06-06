package org.toolkit4j.data.model.page;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 通用分页结果模型。
 *
 * <p>用于统一后端分页数据的返回格式，适配 Repository 层查询结果与 Controller 层 API 输出。
 * 支持任意类型的内容列表（content），并包含分页信息：当前页码、每页大小、 总元素数量以及总页数。
 *
 * <p>本结构通常与 {@code PageRequest} 配合使用。
 *
 * @param <T> 内容的数据类型
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class PageResult<T> implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private static final int DEFAULT_PAGE = 1;

  private Collection<T> content;
  private Integer page;
  private Integer size;
  private Long totalElements;
  private Long totalPages;

  @Contract(pure = true)
  public boolean isEmpty() {
    return Objects.isNull(content) || content.isEmpty();
  }

  @Contract(pure = true)
  public boolean hasContent() {
    return !isEmpty();
  }

  @Contract(pure = true)
  public boolean hasPage() {
    return this.page != null;
  }

  @Contract(pure = true)
  public boolean hasSize() {
    return this.size != null;
  }

  @Contract(pure = true)
  public boolean hasTotalElements() {
    return this.totalElements != null;
  }

  @Contract(pure = true)
  public boolean hasTotalPages() {
    return this.totalPages != null;
  }

  @Contract(pure = true)
  public boolean isFirstPage() {
    return Objects.requireNonNullElse(this.page, DEFAULT_PAGE) <= DEFAULT_PAGE;
  }

  @Contract(pure = true)
  public boolean isLastPage() {
    Long currentTotalPages = this.totalPages;
    Integer currentPage = this.page;
    if (currentTotalPages == null || currentPage == null) {
      return false;
    }
    return currentTotalPages <= 0 || currentPage >= currentTotalPages;
  }

  @Contract(pure = true)
  public boolean hasNextPage() {
    Long currentTotalPages = this.totalPages;
    Integer currentPage = this.page;
    if (currentTotalPages == null || currentPage == null) {
      return false;
    }
    return currentPage < currentTotalPages;
  }

  @Contract(pure = true)
  public boolean hasPreviousPage() {
    Integer currentPage = this.page;
    if (currentPage == null) {
      return false;
    }
    return currentPage > DEFAULT_PAGE;
  }

  @Contract(pure = true)
  public @NotNull Collection<T> getContentOrEmpty() {
    return Objects.requireNonNullElseGet(this.content, Collections::emptyList);
  }

  @Contract(pure = true)
  public @NotNull List<T> getContentAsList() {
    val current = this.content;
    if (current == null || current.isEmpty()) {
      return Collections.emptyList();
    }
    if (current instanceof List<T> list) {
      return list;
    }
    return List.copyOf(current);
  }

  @Contract(pure = true)
  public @Nullable T firstOrNull() {
    if (this.content == null || this.content.isEmpty()) {
      return null;
    }
    return this.content.iterator().next();
  }

  @Contract(pure = true)
  public T firstOrDefault(@Nullable T defaultValue) {
    T first = firstOrNull();
    return first != null ? first : defaultValue;
  }

  @Contract(pure = true)
  public T firstOrElse(@NotNull Supplier<? extends T> supplier) {
    T first = firstOrNull();
    return first != null ? first : supplier.get();
  }

  @Contract(pure = true)
  public @NotNull Optional<T> firstOptional() {
    return Optional.ofNullable(firstOrNull());
  }

  @Contract(pure = true)
  public int contentSize() {
    return this.content == null ? 0 : this.content.size();
  }

  @Contract(" -> new")
  public @NotNull PageResult<T> copy() {
    return new PageResult<>(
        this.content, this.page, this.size, this.totalElements, this.totalPages);
  }

  @Contract(" -> new")
  public @NotNull PageResult<T> normalized() {
    return new PageResult<>(
        Objects.requireNonNullElseGet(content, ArrayList::new),
        Objects.requireNonNullElse(page, DEFAULT_PAGE),
        Objects.requireNonNullElse(size, 0),
        Objects.requireNonNullElse(totalElements, 0L),
        Objects.requireNonNullElse(totalPages, 0L));
  }

  @Contract("_ -> new")
  public @NotNull PageResult<T> withContent(@Nullable Collection<T> newContent) {
    return new PageResult<>(newContent, this.page, this.size, this.totalElements, this.totalPages);
  }

  @Contract("_ -> new")
  public @NotNull PageResult<T> withPage(@Nullable Integer newPage) {
    return new PageResult<>(this.content, newPage, this.size, this.totalElements, this.totalPages);
  }

  @Contract("_ -> new")
  public @NotNull PageResult<T> withSize(@Nullable Integer newSize) {
    return new PageResult<>(this.content, this.page, newSize, this.totalElements, this.totalPages);
  }

  @Contract("_ -> new")
  public @NotNull PageResult<T> withTotalElements(@Nullable Long newTotalElements) {
    return new PageResult<>(this.content, this.page, this.size, newTotalElements, this.totalPages);
  }

  @Contract("_ -> new")
  public @NotNull PageResult<T> withTotalPages(@Nullable Long newTotalPages) {
    return new PageResult<>(this.content, this.page, this.size, this.totalElements, newTotalPages);
  }

  @Contract(" -> new")
  public @NotNull PageResult<T> withoutContent() {
    return new PageResult<>(
        Collections.emptyList(), this.page, this.size, this.totalElements, this.totalPages);
  }

  @Contract("_ -> new")
  public <R> @NotNull PageResult<R> mapContent(@NotNull Function<? super T, ? extends R> mapper) {
    List<R> mapped = this.getContentAsList().stream().<R>map(mapper::apply).toList();

    return new PageResult<>(mapped, this.page, this.size, this.totalElements, this.totalPages);
  }

  /**
   * 创建一个空的分页结果。
   *
   * <p>用于无数据场景（例如搜索结果为空），返回空列表和零分页信息。
   *
   * @param <T> 数据类型
   * @return 一个空的 {@link PageResult}
   */
  @Contract(" -> new")
  public static <T> @NotNull PageResult<T> empty() {
    return new PageResult<>(Collections.emptyList(), DEFAULT_PAGE, 0, 0L, 0L);
  }

  @Contract("_ -> new")
  public static <T> @NotNull PageResult<T> ofContent(@Nullable Collection<T> content) {
    return new PageResult<>(content, DEFAULT_PAGE, 0, 0L, 0L);
  }

  @Contract("_, _, _, _ -> new")
  public static <T> @NotNull PageResult<T> of(
      @Nullable Collection<T> content,
      @Nullable Integer page,
      @Nullable Integer size,
      @Nullable Long totalElements) {
    long safeTotalElements = totalElements == null ? 0L : totalElements;
    int safeSize = size == null ? 0 : size;
    long calculatedTotalPages = safeSize <= 0 ? 0L : (safeTotalElements + safeSize - 1) / safeSize;

    return new PageResult<>(content, page, size, totalElements, calculatedTotalPages);
  }

  @Contract("_, _, _, _, _ -> new")
  public static <T> @NotNull PageResult<T> of(
      @Nullable Collection<T> content,
      @Nullable Integer page,
      @Nullable Integer size,
      @Nullable Long totalElements,
      @Nullable Long totalPages) {
    return new PageResult<>(content, page, size, totalElements, totalPages);
  }
}
