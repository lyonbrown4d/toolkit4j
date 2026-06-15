# Text

Artifact: `io.github.lyonbrown4d:text:0.0.8`

## What it provides

- Keyword matching: `KeywordMatcher`
- Match result model: `KeywordMatch`
- Common regular expressions: `RegexRule`, `CommonRegex`

## Minimal examples

```java
import org.toolkit4j.text.keyword.KeywordMatcher;
import org.toolkit4j.text.regex.CommonRegex;
import org.toolkit4j.text.regex.RegexRule;

var matcher = KeywordMatcher.of("java", "jvm", "toolkit");
var matches = matcher.findAll("toolkit4j is a java toolkit");
var contains = matcher.containsAny("modern JVM utilities");

var ignoreCase = KeywordMatcher.ignoreCase("Java");
var caseInsensitiveMatches = ignoreCase.findAll("modern java utilities");

boolean isMobile = CommonRegex.CHINESE_MOBILE.matches("13812345678");
var allPhones = CommonRegex.CHINESE_MOBILE.matches("13812345678")
    || CommonRegex.US_PHONE.matches("202-555-0186")
    || CommonRegex.JAPANESE_PHONE.matches("090-1234-5678");

var isEmail = CommonRegex.EMAIL.matches("alice@example.com");
var isUuid = CommonRegex.UUID.contains("x-f47ac10b-58cc-4372-a567-0e02b2c3d479-y");

RegexRule mobileRule = CommonRegex.CHINA_MOBILE_WITH_AREA_PREFIX;
```

## Notes

- Matching is based on keyword literals, not regular expressions.
- `KeywordMatch.startIndex()` and `KeywordMatch.endIndex()` use Java `String` UTF-16 indexes, so they can be passed directly to `String.substring(...)`.
- Empty keywords are rejected because they make match semantics ambiguous.
- `CommonRegex` exposes shared patterns for common validation scenarios.  
  Some rules (`*_PHONE`) are **regional format helpers** and are not full telephony validation engines.

## Supported regex groups

### General rules

- `ALPHANUMERIC`, `IDENTIFIER`, `INTEGER`, `DECIMAL`, `EMAIL`, `UUID`, `DOMAIN`, `HTTP_URL`, `IPv4`, etc.

### Regional rules

| Group | Rule constants | Typical scenario |
|---|---|---|
| China | `CHINESE_MOBILE`, `CHINA_MOBILE_WITH_AREA_PREFIX`, `CHINESE_LANDLINE_PHONE`, `CHINESE_ID_CARD`, `CHINESE_SOCIAL_CREDIT_CODE`, `CHINESE_POSTAL_CODE`, `CHINESE_BANK_CARD`, `CHINESE_CAR_PLATE` | Mainland China identifiers and contact formats commonly used by business systems |
| North America / International | `INTERNATIONAL_PHONE_E164`, `US_PHONE` | E.164 and typical US 10/11 digit phone displays |
| UK / Asia | `UK_PHONE`, `HONG_KONG_PHONE`, `JAPANESE_PHONE`, `SINGAPORE_PHONE` | Commonly used regional phone patterns |

### Recommended usage

- For user-facing phone input, treat these patterns as pre-validation.
- For strict compliance, combine with provider-level checks or dedicated telephony validation if you need strict number assignment and portability guarantees.

