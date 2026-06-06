package org.toolkit4j.collection.trie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CodePointPrefixSetTest {

  @Test
  void testCodePointPrefixMatch() {
    var set = new CodePointPrefixSet();
    assertTrue(set.add("😀"));
    assertTrue(set.add("😀a"));
    assertTrue(set.add("😀ab"));

    assertTrue(set.contains("😀"));
    assertTrue(set.containsPrefix("😀"));
    assertTrue(set.containsPrefix("😀a"));
    assertTrue(set.valuesWithPrefix("😀").contains("😀ab"));
    assertEquals(3, set.size());

    assertTrue(set.remove("😀"));
    assertFalse(set.contains("😀"));
    assertTrue(set.containsPrefix("😀"));
    assertEquals(2, set.size());
  }

  @Test
  void testCodePointAndEmptyPrefix() {
    var set = new CodePointPrefixSet(Set.of("😀a", "😀b"));
    assertTrue(set.containsPrefix("😀"));
    assertTrue(set.valuesWithPrefix("😀").containsAll(Set.of("😀a", "😀b")));
    set.clear();
    assertFalse(set.containsPrefix("😀"));
    assertTrue(set.isEmpty());
  }

  @Test
  void testPrefixSetStillUsesCharUnitsForComparison() {
    var set = new PrefixSet();
    assertTrue(set.add("😀"));
    assertTrue(set.add("😀ab"));
    assertEquals(Set.of("😀", "😀ab"), set.valuesWithPrefix("😀"));
  }
}
