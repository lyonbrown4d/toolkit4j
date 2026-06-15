package org.toolkit4j.text.regex;

import java.util.regex.Pattern;

public final class CommonRegex {

  public static final RegexRule ALPHANUMERIC = RegexRule.of(
      "ALPHANUMERIC",
      "^[A-Za-z0-9]+$",
      "Matches one or more ASCII letters or digits.");

  public static final RegexRule IDENTIFIER = RegexRule.of(
      "IDENTIFIER",
      "^[A-Za-z_][A-Za-z0-9_]*$",
      "Matches Java-like identifiers starting with a letter or underscore.");

  public static final RegexRule INTEGER = RegexRule.of(
      "INTEGER",
      "^[+-]?\\d+$",
      "Matches optional-sign decimal integers.");

  public static final RegexRule NON_NEGATIVE_INTEGER = RegexRule.of(
      "NON_NEGATIVE_INTEGER",
      "^\\d+$",
      "Matches zero or positive integers.");

  public static final RegexRule DECIMAL = RegexRule.of(
      "DECIMAL",
      "^[+-]?(?:\\d+\\.\\d+|\\d+|\\.\\d+)$",
      "Matches integer or decimal numbers.");

  public static final RegexRule EMAIL = RegexRule.of(
      "EMAIL",
      "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
      "Matches common email address formats.");

  public static final RegexRule DOMAIN = RegexRule.of(
      "DOMAIN",
      "^(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$",
      "Matches simple domain names.");

  public static final RegexRule HTTP_URL = RegexRule.of(
      "HTTP_URL",
      "^(https?)://[\\w.-]+(?::\\d{2,5})?(?:/[^\\s]*)?$",
      "Matches simple HTTP/HTTPS URLs.");

  public static final RegexRule UUID = RegexRule.of(
      "UUID",
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
      "Matches canonical UUID values.");

  public static final RegexRule HEX = RegexRule.of(
      "HEX",
      "^[0-9A-Fa-f]+$",
      "Matches one or more hexadecimal characters.");

  public static final RegexRule HEX_COLOR = RegexRule.of(
      "HEX_COLOR",
      "^#?([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$",
      "Matches hex RGB or RGBA color code.");

  public static final RegexRule BASE64 = RegexRule.of(
      "BASE64",
      "^[A-Za-z0-9+\\/]+[=]{0,2}$",
      "Matches Base64-like character set with optional padding.");

  public static final RegexRule ISO_LOCAL_DATE = RegexRule.of(
      "ISO_LOCAL_DATE",
      "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
      "Matches ISO yyyy-MM-dd format.");

  public static final RegexRule SEMVER = RegexRule.of(
      "SEMVER",
      "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$",
      "Matches semantic versions.");

  public static final RegexRule PASSWORD_STRENGTH_1 = RegexRule.of(
      "PASSWORD_STRENGTH_1",
      "(?=.{8,})(?=.*[A-Za-z])(?=.*\\d).*$",
      "At least 8 chars, one letter and one digit.");

  public static final RegexRule PASSWORD_STRENGTH_2 = RegexRule.of(
      "PASSWORD_STRENGTH_2",
      "(?=.{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
      "At least 8 chars, contains upper case, lower case and digits.");

  public static final RegexRule CHINESE_MOBILE = RegexRule.of(
      "CHINESE_MOBILE",
      "^1[3-9]\\d{9}$",
      "Matches Mainland China mobile phone numbers.");

  public static final RegexRule CHINA_MOBILE_WITH_AREA_PREFIX = RegexRule.of(
      "CHINA_MOBILE_WITH_AREA_PREFIX",
      "^\\+86-?1[3-9]\\d{9}$",
      "Matches Mainland China mobile phone numbers with optional +86 prefix.");

  public static final RegexRule INTERNATIONAL_PHONE_E164 = RegexRule.of(
      "INTERNATIONAL_PHONE_E164",
      "^\\+[1-9]\\d{1,14}$",
      "Matches E.164 international phone numbers.");

  public static final RegexRule US_PHONE = RegexRule.of(
      "US_PHONE",
      "^(?:\\+1[ -]?)?(?:\\(\\d{3}\\)|\\d{3})[ -]?\\d{3}[ -]?\\d{4}$",
      "Matches common US phone formats.");

  public static final RegexRule UK_PHONE = RegexRule.of(
      "UK_PHONE",
      "^(?:\\+44[ -]?)?(?:0\\d{2,4}[ -]?)?\\d{6,8}$",
      "Matches simplified UK phone number formats.");

  public static final RegexRule HONG_KONG_PHONE = RegexRule.of(
      "HONG_KONG_PHONE",
      "^(?:\\+852[ -]?)?\\d{4}[ -]?\\d{4}$",
      "Matches Hong Kong mobile and fixed-line formats.");

  public static final RegexRule CHINESE_SOCIAL_CREDIT_CODE = RegexRule.of(
      "CHINESE_SOCIAL_CREDIT_CODE",
      "^[0-9A-Z]{18}$",
      "Matches Mainland China Unified Social Credit Code-like values.");

  public static final RegexRule CHINESE_LANDLINE_PHONE = RegexRule.of(
      "CHINESE_LANDLINE_PHONE",
      "^(?:0\\d{2,3}-?)?\\d{7,8}$",
      "Matches common Mainland China fixed-line phone formats.");

  public static final RegexRule CHINESE_BANK_CARD = RegexRule.of(
      "CHINESE_BANK_CARD",
      "^\\d{16,19}$",
      "Matches common Mainland China bank card number length range.");

  public static final RegexRule CHINESE_ID_CARD = RegexRule.of(
      "CHINESE_ID_CARD",
      "^(?:[1-9]\\d{5}(?:18|19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx])$",
      "Matches common Mainland China 18-digit ID card numbers.");

  public static final RegexRule PORT = RegexRule.of(
      "PORT",
      "^(?:[1-9]\\d{0,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]|0)$",
      "Matches network ports in range 0-65535.");

  public static final RegexRule IPV4_ADDRESS = RegexRule.of(
      "IPV4_ADDRESS",
      "^(?:(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$",
      "Matches IPv4 dotted-quad addresses.");

  public static final RegexRule BOOLEAN = RegexRule.of(
      "BOOLEAN",
      "^(?i:true|false|1|0|yes|no)$",
      "Matches common boolean literals.");

  public static final RegexRule INTEGER_WORD = RegexRule.of(
      "INTEGER_WORD",
      "^(?i:one|two|three|four|five|six|seven|eight|nine|ten)$",
      "Matches simple English one-digit numbers.");

  public static final RegexRule CHINESE_POSTAL_CODE = RegexRule.of(
      "CHINESE_POSTAL_CODE",
      "^\\d{6}$",
      "Matches Chinese postal code format.");

  public static final RegexRule CHINESE_CAR_PLATE = RegexRule.of(
      "CHINESE_CAR_PLATE",
      "^[\\u4e00-\\u9fa5][A-HJ-NP-Z][A-Z0-9]{5}$",
      "Matches simplified Chinese vehicle plate format.");

  public static final RegexRule TRAILING_WHITESPACE = RegexRule.of(
      "TRAILING_WHITESPACE",
      "\\s+$",
      "Matches trailing whitespace at end of string.");

  public static final RegexRule NON_EMPTY = RegexRule.of(
      "NON_EMPTY",
      "^(?!\\s*$).+",
      "Matches a non-empty non-whitespace-only string.");

  public static final RegexRule HTML_TAG = RegexRule.of(
      "HTML_TAG",
      "</?[A-Za-z][A-Za-z0-9\\-]*[^>]*>",
      "Matches a simple HTML/XML-like tag.",
      Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private CommonRegex() {}
}
