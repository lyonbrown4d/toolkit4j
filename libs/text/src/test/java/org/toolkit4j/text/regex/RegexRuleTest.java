package org.toolkit4j.text.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RegexRuleTest {

  @Test
  void buildsRuleFromRegex() {
    var rule = RegexRule.of("DIGIT", "\\d+", "Digits");

    assertTrue(rule.matches("123"));
    assertEquals("\\d+", rule.regex());
  }

  @Test
  void rejectsBlankMetadata() {
    assertThrows(
        IllegalArgumentException.class, () -> RegexRule.of(" ", "\\d+", "Digits"));
    assertThrows(
        IllegalArgumentException.class, () -> RegexRule.of("DIGIT", "\\d+", " "));
  }

  @Test
  void findsAndStreamsMatches() {
    var rule = RegexRule.of("WORD", "[a-z]+", "Words");

    assertEquals(List.of("abc", "def"), rule.findAll("abc-1-def"));
    assertEquals(Optional.of("abc"), rule.findFirst("abc-1-def"));
    assertTrue(rule.contains("1-abc"));
    assertFalse(rule.contains("123"));
  }

  @Test
  void hasReusableCommonEmailRule() {
    assertTrue(CommonRegex.EMAIL.matches("alice@example.com"));
    assertFalse(CommonRegex.EMAIL.matches("alice@.com"));
    assertTrue(CommonRegex.EMAIL.contains("email: alice@example.com"));
    assertFalse(CommonRegex.EMAIL.contains("email: alice@.com"));
  }

  @Test
  void hasReusableCommonChineseMobileRule() {
    assertTrue(CommonRegex.CHINESE_MOBILE.matches("13812345678"));
    assertFalse(CommonRegex.CHINESE_MOBILE.matches("23812345678"));
    assertFalse(CommonRegex.CHINESE_MOBILE.contains("phone:23812345678"));
    assertFalse(CommonRegex.CHINESE_MOBILE.contains("phone:1381234567"));
  }

  @Test
  void hasReusableCommonUuidRule() {
    assertTrue(CommonRegex.UUID.matches("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
    assertFalse(CommonRegex.UUID.matches("f47ac10b-58cc-4372-a567-0e02b2c3d47"));
    assertTrue(CommonRegex.UUID.contains("x f47ac10b-58cc-4372-a567-0e02b2c3d479 y"));
  }

  @Test
  void hasReusableCommonPortRule() {
    assertTrue(CommonRegex.PORT.matches("65535"));
    assertFalse(CommonRegex.PORT.matches("65536"));
    assertTrue(CommonRegex.PORT.contains("port=8080"));
    assertFalse(CommonRegex.PORT.contains("port=70000"));
  }

  @Test
  void hasReusableCommonDecimalRule() {
    assertTrue(CommonRegex.DECIMAL.matches("12"));
    assertTrue(CommonRegex.DECIMAL.matches("12.5"));
    assertFalse(CommonRegex.DECIMAL.matches("12."));
    assertFalse(CommonRegex.DECIMAL.matches("."));
  }

  @Test
  void exposesPatternText() {
    assertNotNull(CommonRegex.HTTP_URL.regex());
    assertNotNull(CommonRegex.HTML_TAG.regex());
  }
}
