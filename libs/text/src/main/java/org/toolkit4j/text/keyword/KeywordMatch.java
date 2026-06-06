package org.toolkit4j.text.keyword;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;

@RecordBuilder
public record KeywordMatch(String keyword, int startIndex, int endIndex) {
  public KeywordMatch {
    Objects.requireNonNull(keyword, "keyword");
    if (startIndex < 0) {
      throw new IllegalArgumentException("startIndex must be greater than or equal to 0");
    }
    if (endIndex < startIndex) {
      throw new IllegalArgumentException("endIndex must be greater than or equal to startIndex");
    }
  }

  public int length() {
    return endIndex - startIndex;
  }
}
