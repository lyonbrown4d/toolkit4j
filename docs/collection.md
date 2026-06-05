# Collection

Artifact: `io.github.lyonbrown4d:collection:0.0.4`

## What it provides

- Pageable collections: `PageableList`, `PageableSet`
- Table structures: `HashTable`, `TreeTable`, `ConcurrentHashTable`
- Trie: `HashMapTrie`
- Tree models and builders in `org.toolkit4j.collection.tree`

## Minimal examples

```java
import org.toolkit4j.collection.pageable.PageableList;
import org.toolkit4j.collection.table.HashTable;
import org.toolkit4j.collection.trie.HashMapTrie;

var pageable = new PageableList<>(java.util.List.of(1, 2, 3, 4, 5));
var page = pageable.page(1, 2); // [1, 2]

var table = new HashTable<String, String, Integer>();
table.put("r1", "c1", 100);

var trie = new HashMapTrie<Character, String>();
trie.insert(java.util.List.of('j', 'a', 'v', 'a'), "java");
var value = trie.search(java.util.List.of('j', 'a', 'v', 'a'));
```

## Notes

- Requesting a page beyond the end of the backing data returns an empty page for both `PageableList` and `PageableSet`.
- `HashMapTrie` is marked experimental in the codebase.
- This module targets utility scenarios and avoids heavyweight dependencies.
