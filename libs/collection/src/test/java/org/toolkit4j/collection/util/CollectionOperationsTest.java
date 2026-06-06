package org.toolkit4j.collection.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class CollectionOperationsTest {

  @Test
  void testConcat_multipleCollections() {
    List<Integer> list1 = List.of(1, 2, 3);
    List<Integer> list2 = List.of(4, 5);
    List<Integer> list3 = List.of(6);

    val result = CollectionOperations.concat(list1, list2, list3);

    assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
  }

  @Test
  void testConcat_withNullAndEmptyCollections() {
    List<Integer> list1 = List.of(1, 2);
    List<Integer> list2 = null;
    List<Integer> list3 = Collections.emptyList();

    val result = CollectionOperations.concat(list1, list2, list3);

    assertEquals(List.of(1, 2), result);
  }

  @Test
  void testConcat_emptyInput() {
    val result = CollectionOperations.<Integer>concat();
    assertTrue(result.isEmpty());

    val resultWithNull = CollectionOperations.concat((Collection<Integer>[]) null);
    assertTrue(resultWithNull.isEmpty());
  }

  @Test
  void testUnion_removesDuplicatesAndPreservesFirstEncounterOrder() {
    List<Integer> list1 = List.of(1, 2, 3, 2);
    List<Integer> list2 = List.of(3, 4, 1);

    val result = CollectionOperations.union(list1, list2);

    assertEquals(List.of(1, 2, 3, 4), result);
  }

  @Test
  void testUnion_withNullAndEmptyCollections() {
    List<Integer> list1 = List.of(1, 2);
    List<Integer> list2 = null;
    List<Integer> list3 = Collections.emptyList();

    val result = CollectionOperations.union(list1, list2, list3);

    assertEquals(List.of(1, 2), result);
  }

  @Test
  void testIntersection_commonElements() {
    List<Integer> list1 = List.of(1, 2, 3, 4);
    List<Integer> list2 = List.of(3, 4, 5, 6);
    List<Integer> list3 = List.of(4, 7, 8);

    val result = CollectionOperations.intersection(list1, list2, list3);

    assertEquals(List.of(4), result);
  }

  @Test
  void testIntersection_preservesFirstCollectionOrderAndRemovesDuplicates() {
    List<Integer> list1 = List.of(3, 2, 2, 1, 4);
    List<Integer> list2 = List.of(2, 3, 4);
    List<Integer> list3 = List.of(4, 3, 2);

    val result = CollectionOperations.intersection(list1, list2, list3);

    assertEquals(List.of(3, 2, 4), result);
  }

  @Test
  void testIntersection_withEmptyCollection() {
    List<Integer> list1 = List.of(1, 2);
    List<Integer> list2 = Collections.emptyList();

    val result = CollectionOperations.intersection(list1, list2);

    assertTrue(result.isEmpty());
  }

  @Test
  void testIntersection_withNullCollections() {
    List<Integer> list1 = List.of(1, 2);

    val result = CollectionOperations.intersection(list1, null);

    assertEquals(List.of(1, 2), result);
  }

  @Test
  void testIntersection_noCommonElements() {
    List<Integer> list1 = List.of(1, 2);
    List<Integer> list2 = List.of(3, 4);

    val result = CollectionOperations.intersection(list1, list2);

    assertTrue(result.isEmpty());
  }

  @Test
  void testDifference_returnsElementsOnlyInLeft() {
    List<Integer> left = List.of(1, 2, 3, 4);
    List<Integer> right = List.of(3, 4, 5);

    val result = CollectionOperations.difference(left, right);

    assertEquals(List.of(1, 2), result);
  }

  @Test
  void testDifference_removesDuplicatesAndPreservesLeftOrder() {
    List<Integer> left = List.of(1, 2, 2, 3, 1, 4);
    List<Integer> right = List.of(2, 4);

    val result = CollectionOperations.difference(left, right);

    assertEquals(List.of(1, 3), result);
  }

  @Test
  void testDifference_withNullRight() {
    List<Integer> left = List.of(1, 2, 2, 3);

    val result = CollectionOperations.difference(left, null);

    assertEquals(List.of(1, 2, 3), result);
  }

  @Test
  void testDifference_withNullLeft() {
    val result = CollectionOperations.difference(null, List.of(1, 2));

    assertTrue(result.isEmpty());
  }

  @Test
  void testSymmetricDifference_returnsElementsOnlyInEitherSide() {
    List<Integer> left = List.of(1, 2, 3);
    List<Integer> right = List.of(3, 4, 5);

    val result = CollectionOperations.symmetricDifference(left, right);

    assertEquals(List.of(1, 2, 4, 5), result);
  }

  @Test
  void testSymmetricDifference_removesDuplicatesAndPreservesSideOrder() {
    List<Integer> left = List.of(1, 2, 2, 3);
    List<Integer> right = List.of(3, 4, 4, 5);

    val result = CollectionOperations.symmetricDifference(left, right);

    assertEquals(List.of(1, 2, 4, 5), result);
  }

  @Test
  void testIsSubset_whenTrue() {
    assertTrue(CollectionOperations.isSubset(List.of(1, 2), List.of(1, 2, 3)));
  }

  @Test
  void testIsSubset_whenFalse() {
    assertFalse(CollectionOperations.isSubset(List.of(1, 4), List.of(1, 2, 3)));
  }

  @Test
  void testIsSubset_emptySubsetShouldBeTrue() {
    assertTrue(CollectionOperations.isSubset(Collections.emptyList(), List.of(1, 2, 3)));
    assertTrue(CollectionOperations.isSubset(null, List.of(1, 2, 3)));
  }

  @Test
  void testIsSubset_whenSupersetIsNullOrEmpty() {
    assertFalse(CollectionOperations.isSubset(List.of(1), null));
    assertFalse(CollectionOperations.isSubset(List.of(1), Collections.emptyList()));
  }

  @Test
  void testIsProperSubset_whenTrue() {
    assertTrue(CollectionOperations.isProperSubset(List.of(1, 2), List.of(1, 2, 3)));
  }

  @Test
  void testIsProperSubset_whenEqualShouldBeFalse() {
    assertFalse(CollectionOperations.isProperSubset(List.of(1, 2), List.of(2, 1, 2)));
  }

  @Test
  void testIsProperSubset_whenNotSubsetShouldBeFalse() {
    assertFalse(CollectionOperations.isProperSubset(List.of(1, 4), List.of(1, 2, 3)));
  }

  @Test
  void testIsProperSubset_emptySetBehavior() {
    assertTrue(CollectionOperations.isProperSubset(Collections.emptyList(), List.of(1)));
    assertFalse(
        CollectionOperations.isProperSubset(Collections.emptyList(), Collections.emptyList()));
    assertFalse(CollectionOperations.isProperSubset(null, null));
  }
}
