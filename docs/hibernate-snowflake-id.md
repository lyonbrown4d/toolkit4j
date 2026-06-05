# Hibernate Snowflake ID

Artifact: `io.github.lyonbrown4d:hibernate-snowflake-id:0.0.4`

## What it provides

- Hibernate ID generator integration based on Agrona `SnowflakeIdGenerator`
- Annotation: `@SnowflakeGenerator`
- Optional node-id configuration key: `snow.flake.generator.node-id`

## Minimal examples

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.toolkit4j.hibernate.snowflake.id.SnowflakeGenerator;

@Entity
class OrderEntity {
  @Id
  @SnowflakeGenerator
  private Long id;
}
```

Set explicit node id in generator parameters/properties with key:

`snow.flake.generator.node-id`

## Notes

- Current implementation validates node id against default Agrona layout bounds.
- Debug logs are emitted through SLF4J under `org.toolkit4j.hibernate.snowflake.id` for node-id resolution and generator initialization.
