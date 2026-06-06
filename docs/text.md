# Text

Artifact: `io.github.lyonbrown4d:text:0.0.6`

## What it provides

- Keyword matching: `KeywordMatcher`
- Match result model: `KeywordMatch`

## Minimal examples

```java
import org.toolkit4j.text.keyword.KeywordMatcher;

var matcher = KeywordMatcher.of("java", "jvm", "toolkit");
var matches = matcher.findAll("toolkit4j is a java toolkit");
var contains = matcher.containsAny("modern JVM utilities");

var ignoreCase = KeywordMatcher.ignoreCase("Java");
var caseInsensitiveMatches = ignoreCase.findAll("modern java utilities");
```

## Notes

- Matching is based on keyword literals, not regular expressions.
- `KeywordMatch.startIndex()` and `KeywordMatch.endIndex()` use Java `String` UTF-16 indexes, so they can be passed directly to `String.substring(...)`.
- Empty keywords are rejected because they make match semantics ambiguous.
