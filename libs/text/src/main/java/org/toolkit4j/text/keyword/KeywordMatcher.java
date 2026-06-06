package org.toolkit4j.text.keyword;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class KeywordMatcher {
  private final Node root;
  private final Set<String> keywords;
  private final boolean ignoreCase;

  private KeywordMatcher(Collection<String> keywords, boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
    this.keywords = Collections.unmodifiableSet(validateKeywords(keywords));
    this.root = buildTrie(this.keywords);
  }

  public static @NotNull KeywordMatcher of(@NotNull String... keywords) {
    return of(List.of(keywords));
  }

  public static @NotNull KeywordMatcher of(@NotNull Collection<String> keywords) {
    return new KeywordMatcher(keywords, false);
  }

  public static @NotNull KeywordMatcher ignoreCase(@NotNull String... keywords) {
    return ignoreCase(List.of(keywords));
  }

  public static @NotNull KeywordMatcher ignoreCase(@NotNull Collection<String> keywords) {
    return new KeywordMatcher(keywords, true);
  }

  public @NotNull List<KeywordMatch> findAll(@NotNull String text) {
    var matches = new ArrayList<KeywordMatch>();
    scan(text, matches::add);
    return List.copyOf(matches);
  }

  public boolean containsAny(@NotNull String text) {
    var found = new boolean[1];
    scan(
        text,
        keyword -> {
          found[0] = true;
          return false;
        });
    return found[0];
  }

  public @NotNull Set<String> keywords() {
    return keywords;
  }

  public boolean ignoreCase() {
    return ignoreCase;
  }

  private void scan(String text, MatchConsumer consumer) {
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(consumer, "consumer");
    if (keywords.isEmpty() || text.isEmpty()) {
      return;
    }

    var codePoints = text.codePoints().toArray();
    var charStarts = charStarts(text, codePoints.length);
    var current = root;

    for (var index = 0; index < codePoints.length; index++) {
      var codePoint = normalize(codePoints[index]);
      while (current != root && !current.transitions.containsKey(codePoint)) {
        current = current.failure;
      }
      current = current.transitions.getOrDefault(codePoint, root);

      if (current.outputs.isEmpty()) {
        continue;
      }

      var matchEndCodePointIndex = index;
      var matchEndIndex = charStarts[index] + Character.charCount(codePoints[index]);
      var shouldContinue =
          current.outputs.stream()
              .map(keyword -> toMatch(keyword, matchEndCodePointIndex, matchEndIndex, charStarts))
              .allMatch(consumer::accept);
      if (!shouldContinue) {
        return;
      }
    }
  }

  private Node buildTrie(Set<String> keywords) {
    var trieRoot = new Node();
    trieRoot.failure = trieRoot;

    for (var keyword : keywords) {
      var current = trieRoot;
      var codePoints = keyword.codePoints().map(this::normalize).toArray();
      for (var codePoint : codePoints) {
        current = current.transitions.computeIfAbsent(codePoint, ignored -> new Node());
      }
      current.outputs.add(new Keyword(keyword, codePoints.length));
    }

    buildFailureLinks(trieRoot);
    return trieRoot;
  }

  private void buildFailureLinks(Node trieRoot) {
    var queue = new ArrayDeque<Node>();
    trieRoot
        .transitions
        .values()
        .forEach(
            child -> {
              child.failure = trieRoot;
              queue.add(child);
            });

    while (!queue.isEmpty()) {
      var current = queue.remove();
      current.transitions.forEach(
          (codePoint, target) -> {
            var fallback = current.failure;
            while (fallback != trieRoot && !fallback.transitions.containsKey(codePoint)) {
              fallback = fallback.failure;
            }
            target.failure = fallback.transitions.getOrDefault(codePoint, trieRoot);
            target.outputs.addAll(target.failure.outputs);
            queue.add(target);
          });
    }
  }

  private LinkedHashSet<String> validateKeywords(Collection<String> keywords) {
    Objects.requireNonNull(keywords, "keywords");
    return keywords.stream()
        .map(this::validateKeyword)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private String validateKeyword(String keyword) {
    Objects.requireNonNull(keyword, "keyword");
    if (keyword.isEmpty()) {
      throw new IllegalArgumentException("keyword must not be empty");
    }
    return keyword;
  }

  private KeywordMatch toMatch(
      Keyword keyword, int endCodePointIndex, int endIndex, int[] charStarts) {
    var startCodePointIndex = endCodePointIndex - keyword.codePointLength() + 1;
    return new KeywordMatch(keyword.value(), charStarts[startCodePointIndex], endIndex);
  }

  private int normalize(int codePoint) {
    return ignoreCase ? Character.toLowerCase(codePoint) : codePoint;
  }

  private int[] charStarts(String text, int codePointCount) {
    var starts = new int[codePointCount];
    var offset = 0;
    for (var index = 0; index < codePointCount; index++) {
      starts[index] = offset;
      offset += Character.charCount(text.codePointAt(offset));
    }
    return starts;
  }

  private interface MatchConsumer {
    boolean accept(KeywordMatch match);
  }

  private record Keyword(String value, int codePointLength) {}

  private static final class Node {
    private final Map<Integer, Node> transitions = new LinkedHashMap<>();
    private final List<Keyword> outputs = new ArrayList<>();
    private Node failure;
  }
}
