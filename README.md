# toolkit4j

**English** · A lightweight JVM utility toolkit for collections, data models, and small infrastructure helpers.  
**中文** · 面向 JVM 的轻量级工具库，提供集合、数据模型及小型基础设施扩展。

---

## Installation

`toolkit4j` artifacts are published to Maven Central under `io.github.lyonbrown4d`.

### Gradle BOM

```kotlin
dependencies {
  implementation(platform("io.github.lyonbrown4d:toolkit4j-bom:0.0.8"))
  implementation("io.github.lyonbrown4d:collection")
  // implementation("io.github.lyonbrown4d:text")
  // implementation("io.github.lyonbrown4d:data-model")
  // implementation("io.github.lyonbrown4d:net")
  // implementation("io.github.lyonbrown4d:hibernate-snowflake-id")
  // implementation("io.github.lyonbrown4d:quartz-task")
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
  implementation("io.github.lyonbrown4d:collection:0.0.8")
  // implementation("io.github.lyonbrown4d:text:0.0.8")
  // implementation("io.github.lyonbrown4d:data-model:0.0.8")
  // implementation("io.github.lyonbrown4d:net:0.0.8")
  // implementation("io.github.lyonbrown4d:hibernate-snowflake-id:0.0.8")
  // implementation("io.github.lyonbrown4d:quartz-task:0.0.8")
}
```

### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.lyonbrown4d</groupId>
      <artifactId>toolkit4j-bom</artifactId>
      <version>0.0.8</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### Maven Module Dependency

```xml
<dependency>
  <groupId>io.github.lyonbrown4d</groupId>
  <artifactId>collection</artifactId>
</dependency>
```

---

## Modules

- `collection`: data structures such as pageable collections, table, trie, tree helpers, and collection diffing.
- `text`: lightweight keyword matching utilities.
- `data-model`: reusable model types such as `PageRequest`, `PageResult`, `Result`, `Sortable`, `Range`, `Money`, `DataSize`, `DataRate`, `ErrorInfo`, `EnumValue`, plus time presets and semantic time ranges.
- `net`: IP / CIDR utility types (`Ipv4Address`, `Ipv6Address`, `Cidr`, `IpInfo`).
- `hibernate-snowflake-id`: Hibernate integration for Agrona Snowflake ID generator.
- `quartz-task`: high-level Quartz task registration and scheduling API.

---

## Documentation

Start from the docs index:

- [docs/README.md](./docs/README.md)

Per-module guides:

- [Collection](./docs/collection.md)
- [Text](./docs/text.md)
- [Data Model](./docs/data-model.md)
- [Net](./docs/net.md)
- [Hibernate Snowflake ID](./docs/hibernate-snowflake-id.md)
- [Quartz Task](./docs/quartz-task.md)

---

## License

[Apache License 2.0](./LICENSE.txt)

