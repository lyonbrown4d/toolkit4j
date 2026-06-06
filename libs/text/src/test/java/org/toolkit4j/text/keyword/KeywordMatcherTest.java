package org.toolkit4j.text.keyword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordMatcherTest {

  @Test
  void findsOverlappingKeywords() {
    var matcher = KeywordMatcher.of("he", "she", "his", "hers");

    var matches = matcher.findAll("ushers");

    assertEquals(
        List.of(
            new KeywordMatch("she", 1, 4),
            new KeywordMatch("he", 2, 4),
            new KeywordMatch("hers", 2, 6)),
        matches);
  }

  @Test
  void supportsSimpleCaseInsensitiveMatching() {
    var matcher = KeywordMatcher.ignoreCase("Java", "JVM");

    assertTrue(matcher.containsAny("modern java utility"));
    assertEquals(List.of(new KeywordMatch("Java", 7, 11)), matcher.findAll("modern java utility"));
  }

  @Test
  void keepsUtf16StringIndexesForUnicodeMatches() {
    var matcher = KeywordMatcher.of("😀a");

    var matches = matcher.findAll("x😀abc");

    assertEquals(List.of(new KeywordMatch("😀a", 1, 4)), matches);
    assertEquals(
        "😀a", "x😀abc".substring(matches.getFirst().startIndex(), matches.getFirst().endIndex()));
  }

  @Test
  void rejectsEmptyKeywords() {
    assertThrows(IllegalArgumentException.class, () -> KeywordMatcher.of(""));
  }

  @Test
  void emptyMatcherNeverMatches() {
    var matcher = KeywordMatcher.of(List.of());

    assertFalse(matcher.containsAny("anything"));
    assertEquals(List.of(), matcher.findAll("anything"));
  }
}
