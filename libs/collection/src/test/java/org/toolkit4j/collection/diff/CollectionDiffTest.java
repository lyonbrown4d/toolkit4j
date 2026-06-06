package org.toolkit4j.collection.diff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CollectionDiffTest {

  @Test
  void compareBySameItemType() {
    var previous = List.of(new User(1, "Alice"), new User(2, "Bob"), new User(3, "Cindy"));
    var current = List.of(new User(1, "Alice"), new User(2, "Bobby"), new User(4, "Dora"));

    var diff = CollectionDiff.compare(previous, current, User::id);

    assertTrue(diff.hasChanges());
    assertEquals(4, diff.totalCompared());
    assertEquals(List.of(new DiffItem<>(4, new User(4, "Dora"))), diff.added());
    assertEquals(List.of(new DiffItem<>(3, new User(3, "Cindy"))), diff.removed());
    assertEquals(
        List.of(new DiffPair<>(1, new User(1, "Alice"), new User(1, "Alice"))), diff.unchanged());
    assertEquals(
        List.of(new DiffPair<>(2, new User(2, "Bob"), new User(2, "Bobby"))), diff.changed());
  }

  @Test
  void compareByDifferentItemTypes() {
    var previous = List.of(new User(1, "Alice"), new User(2, "Bob"));
    var current = List.of(new UserDto("1", "Alice"), new UserDto("2", "Bobby"));

    var diff =
        CollectionDiff.compareByKey(
            previous,
            current,
            User::id,
            dto -> Integer.parseInt(dto.id()),
            (left, right) -> left.name().equals(right.name()));

    assertEquals(1, diff.unchanged().size());
    assertEquals(1, diff.changed().size());
    assertEquals(2, diff.totalCompared());
  }

  @Test
  void returnsNoChangesForEqualCollections() {
    var previous = List.of(new User(1, "Alice"));
    var current = List.of(new User(1, "Alice"));

    var diff = CollectionDiff.compare(previous, current, User::id);

    assertFalse(diff.hasChanges());
    assertEquals(1, diff.totalCompared());
  }

  @Test
  void rejectsDuplicateKeys() {
    var users = List.of(new User(1, "Alice"), new User(1, "Bob"));

    assertThrows(
        IllegalArgumentException.class, () -> CollectionDiff.compare(users, List.of(), User::id));
  }

  private record User(int id, String name) {}

  private record UserDto(String id, String name) {}
}
