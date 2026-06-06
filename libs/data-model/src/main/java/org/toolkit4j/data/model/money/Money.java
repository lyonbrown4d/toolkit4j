package org.toolkit4j.data.model.money;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@RecordBuilder
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {
  public Money {
    amount = normalizeAmount(Objects.requireNonNull(amount, "amount"));
    Objects.requireNonNull(currency, "currency");
  }

  public static @NotNull Money of(@NotNull BigDecimal amount, @NotNull Currency currency) {
    return new Money(amount, currency);
  }

  public static @NotNull Money zero(@NotNull Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public @NotNull String currencyCode() {
    return currency.getCurrencyCode();
  }

  public boolean isZero() {
    return amount.signum() == 0;
  }

  public boolean isPositive() {
    return amount.signum() > 0;
  }

  public boolean isNegative() {
    return amount.signum() < 0;
  }

  public @NotNull Money add(@NotNull Money other) {
    return new Money(amount.add(requireSameCurrency(other).amount), currency);
  }

  public @NotNull Money subtract(@NotNull Money other) {
    return new Money(amount.subtract(requireSameCurrency(other).amount), currency);
  }

  public @NotNull Money negate() {
    return new Money(amount.negate(), currency);
  }

  public @NotNull Money abs() {
    return isNegative() ? negate() : this;
  }

  public @NotNull Money multiply(@NotNull BigDecimal multiplicand) {
    return new Money(
        amount.multiply(Objects.requireNonNull(multiplicand, "multiplicand")), currency);
  }

  public @NotNull Money withAmount(@NotNull BigDecimal amount) {
    return new Money(amount, currency);
  }

  public boolean sameCurrency(@NotNull Money other) {
    return currency.equals(Objects.requireNonNull(other, "other").currency);
  }

  @Override
  public int compareTo(@NotNull Money other) {
    return amount.compareTo(requireSameCurrency(other).amount);
  }

  private Money requireSameCurrency(Money other) {
    if (!sameCurrency(other)) {
      throw new IllegalArgumentException(
          "Currency mismatch: %s vs %s"
              .formatted(currency.getCurrencyCode(), other.currency.getCurrencyCode()));
    }
    return other;
  }

  private BigDecimal normalizeAmount(@NotNull BigDecimal amount) {
    var normalized = amount.stripTrailingZeros();
    return normalized.scale() < 0 ? normalized.setScale(0, RoundingMode.HALF_DOWN) : normalized;
  }
}
