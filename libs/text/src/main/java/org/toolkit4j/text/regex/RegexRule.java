package org.toolkit4j.text.regex;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

@RecordBuilder
public record RegexRule(
    @NotNull String name,
    @NotNull Pattern pattern,
    @NotNull String description) {

  public RegexRule {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(pattern, "pattern");
    Objects.requireNonNull(description, "description");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (description.isBlank()) {
      throw new IllegalArgumentException("description must not be blank");
    }
    pattern.pattern();
  }

  public static @NotNull RegexRule of(
      @NotNull String name, @NotNull String regex, @NotNull String description) {
    return of(name, regex, description, 0);
  }

  public static @NotNull RegexRule of(
      @NotNull String name,
      @NotNull String regex,
      @NotNull String description,
      int flags) {
    return new RegexRule(name, Pattern.compile(regex, flags), description);
  }

  public @NotNull String regex() {
    return pattern.pattern();
  }

  public boolean matches(@NotNull CharSequence text) {
    return pattern.matcher(text).matches();
  }

  public boolean contains(@NotNull CharSequence text) {
    return pattern.matcher(text).find();
  }

  public @NotNull Optional<String> findFirst(@NotNull CharSequence text) {
    return pattern.matcher(text).results().findFirst().map(MatchResult::group);
  }

  public @NotNull List<String> findAll(@NotNull CharSequence text) {
    return pattern.matcher(text).results().map(MatchResult::group).toList();
  }
}
