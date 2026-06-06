package org.toolkit4j.collection.trie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PrefixSetTest {

  @Test
  void testAddContainsAndRemove() {
    var set = new PrefixSet();

    assertFalse(set.contains("hello"));
    assertFalse(set.containsPrefix("hel"));
    assertEquals(0, set.size());

    assertTrue(set.add("hello"));
    assertTrue(set.add("hell"));
    assertTrue(set.add("help"));
    assertEquals(3, set.size());
    assertTrue(set.contains("hello"));
    assertTrue(set.containsPrefix("hel"));
    assertTrue(set.containsPrefix("hello"));
    assertFalse(set.containsPrefix("world"));

    assertFalse(set.add("hello"));
    assertEquals(3, set.size());
    assertTrue(set.remove("hello"));
    assertFalse(set.contains("hello"));
    assertEquals(2, set.size());
    assertFalse(set.remove("hello"));
  }

  @Test
  void testValuesWithPrefix() {
    var set = new PrefixSet();
    set.add("apple");
    set.add("app");
    set.add("application");
    set.add("banana");

    Set<String> appMatches = set.valuesWithPrefix("app");
    assertEquals(Set.of("app", "apple", "application"), appMatches);

    assertTrue(set.valuesWithPrefix("ba").contains("banana"));
    assertEquals(1, set.valuesWithPrefix("ba").size());
    assertTrue(set.valuesWithPrefix("zoo").isEmpty());
  }

  @Test
  void testClearAndIterableConstructor() {
    var set = new PrefixSet(Set.of("alpha", "beta", "alphabet"));

    assertEquals(3, set.size());
    assertTrue(set.containsPrefix(""));
    assertEquals(Set.of("alpha", "alphabet"), set.valuesWithPrefix("alph"));

    set.clear();
    assertTrue(set.isEmpty());
    assertEquals(0, set.size());
    assertFalse(set.containsPrefix("a"));
    assertFalse(set.containsPrefix(""));
  }
}
