package org.toolkit4j.data.model.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DataSizeTest {

  @Test
  void parsesDecimalAndBinaryUnits() {
    assertEquals(DataSize.bytes(1_500), DataSize.parse("1.5KB"));
    assertEquals(DataSize.bytes(1_536), DataSize.parse("1.5 KiB"));
    assertEquals(DataSize.bytes(512), DataSize.parse("512"));
  }

  @Test
  void convertsToUnits() {
    var size = DataSize.mebibytes(2);

    assertEquals(2, size.to(DataUnit.MEBIBYTES));
    assertEquals(new BigDecimal("2.097152"), size.toDecimal(DataUnit.MEGABYTES));
  }

  @Test
  void supportsArithmetic() {
    var size = DataSize.kilobytes(1).plus(DataSize.bytes(24));

    assertEquals(DataSize.bytes(1_024), size);
    assertEquals(DataSize.bytes(2_048), size.multiply(2));
  }

  @Test
  void rejectsInvalidSizes() {
    assertThrows(IllegalArgumentException.class, () -> DataSize.bytes(-1));
    assertThrows(IllegalArgumentException.class, () -> DataSize.parse("0.1B"));
    assertThrows(IllegalArgumentException.class, () -> DataSize.parse("12XB"));
  }
}
