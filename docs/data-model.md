# Data Model

Artifact: `io.github.lyonbrown4d:data-model:0.0.6`

## What it provides

- `page`: `PageRequest`, `PageResult`
- `envelope`: `Result<C, T>`
- `sort`: `Sortable`
- `time`: `DateTimePattern`, `DateTimeFormats`, `LocalDateRange`, `LocalDateTimeRange`, `InstantRange`, `YearMonthValue`, `YearMonthRange`
- `range`: `Range<T>`, `Bound<T>`, `BoundType`
- `money`: `Money`
- `measure`: `DataSize`, `DataUnit`, `DataRate`
- `enumeration`: `EnumValue<T>`, `EnumLookup`, `EnumValues`
- `error`: `ErrorCode`, `ErrorInfo`, `FieldError`
- `value`: `KeyValue<K, V>`, `Option<T>`, `Geo`

## Package shape

- `page`: paging request/result carriers
- `envelope`: generic result wrapper
- `sort`: lightweight ordering contract
- `time`: formatter presets plus semantic date/time range models
- `range`: generic comparable range model
- `money`: same-currency monetary value object
- `measure`: data size and data rate value objects
- `enumeration`: enum primary-value lookup support
- `error`: reusable error and field-error models
- `value`: small generic value carriers

## Minimal examples

```java
import org.toolkit4j.data.model.page.PageRequest;
import org.toolkit4j.data.model.page.PageResult;
import org.toolkit4j.data.model.envelope.Result;
import org.toolkit4j.data.model.money.Money;
import org.toolkit4j.data.model.measure.DataRate;
import org.toolkit4j.data.model.measure.DataSize;
import org.toolkit4j.data.model.measure.DataUnit;
import org.toolkit4j.data.model.range.Range;
import org.toolkit4j.data.model.sort.Sortable;
import org.toolkit4j.data.model.time.DateTimeFormats;
import org.toolkit4j.data.model.time.DateTimePattern;
import org.toolkit4j.data.model.time.LocalDateRange;
import org.toolkit4j.data.model.time.YearMonthValue;

var request = new PageRequest();
request.setPage(2);
request.setSize(20);
var page = request.getPage();   // 2
var offset = request.getOffset(); // 20

var result = PageResult.<String>empty();
var normalized = result.normalized();

var ok = Result.of(0, "ok", java.util.Map.of("id", 1L));
var noData = ok.withoutData();

var total = Money.of(new java.math.BigDecimal("19.99"), java.util.Currency.getInstance("USD"));
var uploadSize = DataSize.parse("1.5 MiB");
var uploadRate = DataRate.of(uploadSize, java.time.Duration.ofSeconds(3));
var kibPerSecond = uploadRate.to(DataUnit.KIBIBYTES);
var activeWindow = Range.closed(1, 10);
var timestamp = DateTimeFormats.of(DateTimePattern.STANDARD_DATE_TIME)
  .format(java.time.LocalDateTime.of(2026, 3, 26, 8, 9, 10));
var billingCycle = LocalDateRange.closed(
  java.time.LocalDate.of(2026, 3, 1),
  java.time.LocalDate.of(2026, 3, 31)
);
var period = YearMonthValue.parse("2026-03");

record Step(String name, int order) implements Sortable {
  @Override public int getOrder() { return order; }
}
var orderedSteps = Sortable.sort(new java.util.ArrayList<>(java.util.List.of(
  new Step("second", 20),
  new Step("first", 10)
)));
```

## Enumeration

```java
import org.toolkit4j.data.model.enumeration.EnumValue;
import org.toolkit4j.data.model.enumeration.EnumValues;

enum Status implements EnumValue<String> {
  ENABLED("enabled"),
  DISABLED("disabled");

  private final String primaryValue;

  Status(String primaryValue) {
    this.primaryValue = primaryValue;
  }

  @Override
  public String getPrimaryValue() {
    return primaryValue;
  }
}

var lookup = EnumValues.lookup(Status.class);
var enabled = lookup.fromPrimaryValue("enabled");
var optional = lookup.findByPrimaryValue("missing");
```

## Money

```java
import org.toolkit4j.data.model.money.Money;

var usd = java.util.Currency.getInstance("USD");
var subtotal = Money.of(new java.math.BigDecimal("19.99"), usd);
var tax = Money.of(new java.math.BigDecimal("1.50"), usd);
var total = subtotal.add(tax);
var doubled = subtotal.multiply(new java.math.BigDecimal("2"));
```

## Measure

```java
import java.time.Duration;
import org.toolkit4j.data.model.measure.DataRate;
import org.toolkit4j.data.model.measure.DataSize;
import org.toolkit4j.data.model.measure.DataUnit;

var size = DataSize.parse("1.5 MiB");
var bytes = size.bytes();
var megabytes = size.toDecimal(DataUnit.MEGABYTES);

var rate = DataRate.of(size, Duration.ofSeconds(3));
var kibPerSecond = rate.to(DataUnit.KIBIBYTES);
```

## Range

```java
import org.toolkit4j.data.model.range.Range;

var closed = Range.closed(1, 10);
var open = Range.open(1, 10);
var anyPositive = Range.greaterThan(0);

var includesTen = closed.contains(10);  // true
var includesOne = open.contains(1);     // false
```

## Time

```java
import org.toolkit4j.data.model.time.DateTimeFormats;
import org.toolkit4j.data.model.time.DateTimePattern;
import org.toolkit4j.data.model.time.InstantRange;
import org.toolkit4j.data.model.time.LocalDateRange;
import org.toolkit4j.data.model.time.LocalDateTimeRange;
import org.toolkit4j.data.model.time.YearMonthRange;
import org.toolkit4j.data.model.time.YearMonthValue;

var formatter = DateTimeFormats.of(DateTimePattern.DATE);
var date = formatter.format(java.time.LocalDate.of(2026, 3, 26));

var basicTimestamp = DateTimeFormats.ofPattern("yyyyMMddHHmmss")
  .format(java.time.LocalDateTime.of(2026, 3, 26, 8, 9, 10));

var iso = DateTimeFormats.ISO_OFFSET_DATE_TIME
  .format(java.time.OffsetDateTime.parse("2026-03-26T08:09:10+08:00"));

var days = LocalDateRange.closed(
  java.time.LocalDate.of(2026, 3, 1),
  java.time.LocalDate.of(2026, 3, 31)
);

var instants = InstantRange.closedOpen(
  java.time.Instant.parse("2026-03-26T00:00:00Z"),
  java.time.Instant.parse("2026-03-27T00:00:00Z")
);

var workHours = LocalDateTimeRange.closedOpen(
  java.time.LocalDateTime.of(2026, 3, 26, 9, 0),
  java.time.LocalDateTime.of(2026, 3, 26, 18, 0)
);

var accountingMonth = YearMonthValue.parse("2026-03");
var quarter = YearMonthRange.closed(
  YearMonthValue.of(2026, 1),
  YearMonthValue.of(2026, 3)
);
```

### Time Design Notes

- `DateTimePattern` holds the common non-ISO pattern presets that are often repeated across applications.
- `DateTimeFormats` exposes both those presets and JDK ISO formatters without introducing a mutable registry.
- `LocalDateRange`, `LocalDateTimeRange`, and `InstantRange` are semantic wrappers over `Range<T>` for clearer APIs at call sites.
- `YearMonthValue` is a small value object for month-level business concepts such as billing month, accounting period, or statement cycle.
- `YearMonthRange` is useful when month granularity matters more than exact dates.

## Error

```java
import org.toolkit4j.data.model.error.ErrorInfo;
import org.toolkit4j.data.model.error.FieldError;

var error = ErrorInfo.of("INVALID_INPUT", "invalid input")
  .withDetail(new FieldError("name", "blank", "must not be blank", null));
```

## Notes

- Integration-facing models such as `page`, `envelope`, and `error` prefer extensible `class` types with Lombok-generated accessors.
- Closed, immutable value objects such as `money`, `range`, and small `value` carriers prefer `record`.
- `PageRequest#getPage()` returns the page number stored in the DTO.
- `PageRequest#getOffset()` provides the database offset derived from page and size.
- `PageResult.empty()` uses first-page semantics (`page = 1`) rather than zero-based page numbering.
- `Result<C, T>` now lives under `org.toolkit4j.data.model.envelope`.
- `Money` is intentionally lightweight: same-currency arithmetic only, with no exchange-rate or formatting layer.
- `DataSize` is non-negative and stores an exact byte count.
- Decimal data units use powers of 1000; binary data units use powers of 1024.
- `time` currently focuses on formatter presets, semantic time ranges, and a small set of reusable time value objects; it does not add mutable registries or time-zone policy.
- Module name: `org.toolkit4j.data.model`
- `data-model` is organized by subpackage responsibility rather than a flat package.
- Models are intended for reuse across services and API layers.
