package org.toolkit4j.data.model.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class DataRateTest {

  @Test
  void calculatesBytesPerSecond() {
    var rate = DataRate.of(DataSize.mebibytes(1), Duration.ofSeconds(2));

    assertEquals(new BigDecimal("524288"), rate.bytesPerSecond());
    assertEquals(new BigDecimal("512"), rate.to(DataUnit.KIBIBYTES));
  }

  @Test
  void rejectsInvalidRates() {
    assertThrows(IllegalArgumentException.class, () -> DataRate.bytesPerSecond(-1));
    assertThrows(IllegalArgumentException.class, () -> DataRate.of(DataSize.bytes(1), Duration.ZERO));
  }
}
