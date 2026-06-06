# Toolkit4j Collection Package / toolkit4j 集合模块

---

## 中文

基于 JVM 的轻量级集合与数据结构工具包，补充 JDK 标准库中不完善或缺失的实现。

### 模块定位

- **轻量级**：最小化依赖，优先使用 JDK 能力
- **JDK 25**：基线版本

### 已实现

#### Table（二维表）
- `HashTable`、`TreeTable`、`ConcurrentHashTable`、`WeakTable`
- 支持 `stream()`、`rowKeyStream()`、`columnKeyStream()`、`valueStream()`
- `getOrDefault`、`putAll` 等便捷方法

#### PageableCollection（分页集合）
- `PageableList`、`PageableSet`
- 支持 `stream()`、`first()`、`last()`、`slice(from, to)`
- 超出数据范围的页请求返回空页

#### Trie（前缀树）
- `HashMapTrie`，支持 insert、search、startsWith、keysWithPrefix 等
- ⚠️ **试验性 API**：`HashMapTrie` 标注为 `@ApiStatus.Experimental`，后续版本可能调整
- `PrefixSet`，面向字符串的前缀匹配集合，支持 add/contains/remove、`containsPrefix`、`valuesWithPrefix`
- `CodePointPrefixSet`，按 Unicode code point 处理前缀的字符串集合（推荐用于 emoji 等超平面字符）

#### Tree（树形结构）
- 接口：`Tree`、`ListTree`、`SetTree`、`LinkedTree`、`SortedTree`、`TreeNode`、`ListTreeNode`、`SetTreeNode`、`SortedTreeNode`、`LinkedTreeNode`
- 构建：`Trees.build()` / `buildList()` / `buildSet()` / `buildLinked()` / `buildSorted()`
- `stream()`、`breadthFirst()`、`find()`、`pathTo()` 等

#### CollectionUtil
- `merge`、`mergeDistinct`、`intersection` 等集合操作

### 依赖

- Lombok（static）
- JetBrains Annotations（static）
- Gson（仅测试）

### 使用示例

```java
// Table
var table = new HashTable<String, String, Integer>();
table.put("r1", "c1", 100);
table.rowKeyStream().forEach(System.out::println);

// PageableList
var list = new PageableList<>(List.of(1, 2, 3, 4, 5));
var page = list.page(1, 2);  // [1, 2]
var first = list.first();    // Optional[1]

// Tree (flat list implements FlatNode)
record Node(Long id, Long parentId) implements FlatNode<Long> {}
var flat = List.of(new Node(1L, null), new Node(2L, 1L));
var tree = Trees.build(flat);
tree.stream().forEach(n -> System.out.println(n.data().id()));
```

### 基准测试 / Benchmarks

JMH 微基准覆盖：Table（HashTable vs ConcurrentHashTable 单线程/多线程）、Trie、Tree 构建、PageableList/PageableSet。

```bash
./gradlew :libs:collection:jmh
# 运行单个 benchmark：-Pjmh.includes='.*TableBenchmark.*'
```

### 单元测试 / Tests

```bash
./gradlew :libs:collection:test
```

---

## English

A lightweight JVM collection and data structure toolkit that complements the JDK standard library where it is incomplete or lacking.

### Module Overview

- **Lightweight**: Minimal dependencies, JDK-first
- **JDK 25**: Baseline version

### Implemented

#### Table (2D mapping)
- `HashTable`, `TreeTable`, `ConcurrentHashTable`, `WeakTable`
- `stream()`, `rowKeyStream()`, `columnKeyStream()`, `valueStream()`
- Convenience methods: `getOrDefault`, `putAll`, etc.

#### PageableCollection
- `PageableList`, `PageableSet`
- `stream()`, `first()`, `last()`, `slice(from, to)`
- Requests beyond the end of the backing data return an empty page

#### Trie (prefix tree)
- `HashMapTrie` with insert, search, startsWith, keysWithPrefix, etc.
- ⚠️ **Experimental API**: `HashMapTrie` is annotated with `@ApiStatus.Experimental`; API may change in future releases
- `PrefixSet`, string-based prefix matching set with add/contains/remove, `containsPrefix`, `valuesWithPrefix`
- `CodePointPrefixSet`, prefix set that matches by Unicode code points (recommended for emojis / non-BMP chars)

#### Tree (hierarchical structure)
- Interfaces: `Tree`, `ListTree`, `SetTree`, `LinkedTree`, `SortedTree`, `TreeNode`, `ListTreeNode`, `SetTreeNode`, `SortedTreeNode`, `LinkedTreeNode`
- Build: `Trees.build()` / `buildList()` / `buildSet()` / `buildLinked()` / `buildSorted()`
- `stream()`, `breadthFirst()`, `find()`, `pathTo()`, etc.

#### CollectionUtil
- `merge`, `mergeDistinct`, `intersection` for collection operations

### Dependencies

- Lombok (static)
- JetBrains Annotations (static)
- Gson (test only)

### Usage Example

```java
// Table
var table = new HashTable<String, String, Integer>();
table.put("r1", "c1", 100);
table.rowKeyStream().forEach(System.out::println);

// PageableList
var list = new PageableList<>(List.of(1, 2, 3, 4, 5));
var page = list.page(1, 2);  // [1, 2]
var first = list.first();    // Optional[1]

// Tree (flat list implements FlatNode)
record Node(Long id, Long parentId) implements FlatNode<Long> {}
var flat = List.of(new Node(1L, null), new Node(2L, 1L));
var tree = Trees.build(flat);
tree.stream().forEach(n -> System.out.println(n.data().id()));
```

### Benchmarks

JMH micro-benchmarks for: Table (HashTable vs ConcurrentHashTable, single/multi-thread), Trie, Tree build, PageableList/PageableSet.

```bash
./gradlew :libs:collection:jmh
# Run single benchmark: -Pjmh.includes='.*TableBenchmark.*'
```

### Tests

```bash
./gradlew :libs:collection:test
```
