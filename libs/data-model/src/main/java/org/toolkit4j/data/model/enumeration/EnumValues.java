package org.toolkit4j.data.model.enumeration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class EnumValues {
  private final ConcurrentMap<Class<?>, EnumLookup<?, ?>> LOOKUPS = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  public <T, E extends Enum<E> & EnumValue<T>> @NotNull EnumLookup<T, E> lookup(
      @NotNull Class<E> enumClass) {
    return (EnumLookup<T, E>)
        LOOKUPS.computeIfAbsent(enumClass, ignored -> EnumLookup.of(enumClass));
  }
}
