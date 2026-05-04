# Social Network Simulator

## Students
- Allan Brunner
- Valentin Monod
- Axel Hall
- James Zeiger

---

## Graph Schema

| Label | Properties |
|---|---|
| `USER` | `id`, `first`, `last` |
| `POST` | `id`, `text`, `date`, `likes` |

> Both posts and comments use the `POST` label. Comments have an additional `[:ON]` relationship pointing to their parent post.

| Relationship | Pattern |
|---|---|
| `[:POSTED]` | `(USER)-[:POSTED]->(POST)` |
| `[:COMMENTED]` | `(USER)-[:COMMENTED]->(POST)` |
| `[:ON]` | `(comment:POST)-[:ON]->(parent:POST)` |

---

## Events into Cypher

### `new-user`: `{id, first, last}`
```cypher
MERGE (u:USER {id: $id})
ON CREATE SET u.first = $first, u.last = $last
```

### `new-post`: `{id, user, text, date}`
```cypher
MATCH (u:USER {id: $user})
MERGE (p:POST {id: $id})
ON CREATE SET p.text = $text, p.date = $date, p.likes = 0
MERGE (u)-[:POSTED]->(p)
```

### `new-comment`: `{id, post, user, text, date}`
```cypher
MATCH (u:USER {id: $user})
MATCH (p:POST {id: $post})
MERGE (c:POST {id: $id})
ON CREATE SET c.text = $text, c.date = $date, c.likes = 0
MERGE (u)-[:COMMENTED]->(c)
MERGE (c)-[:ON]->(p)
```

### `like`: `{post, user}`
Likes are stored as a counter on the node only: no relationship is created.
```cypher
MATCH (p:POST {id: $post})
SET p.likes = p.likes + 1
```

### `update-post`: `{id, text}`
```cypher
MATCH (p:POST {id: $id})
SET p.text = $text
```

### `delete-post`: `{id}`
Recursively deletes all comments (follows `[:ON]` chains of any depth).
```cypher
MATCH (p:POST {id: $id})
OPTIONAL MATCH (p)<-[:ON*]-(c:POST)
DETACH DELETE p, c
```

### `delete-user`: `{id}`
Deletes the user, all their posts/comments, and all comments on those posts.
```cypher
MATCH (u:USER {id: $id})
OPTIONAL MATCH (u)-[:POSTED|COMMENTED]->(authored:POST)
OPTIONAL MATCH (authored)<-[:ON*0..]-(tree:POST)
DETACH DELETE u, authored, tree
```

---

## Challenges into Cypher

Challenges ask for a count of `USER` or `POST` nodes matching a clause tree. The base query shape is:

```cypher
MATCH (n:USER|POST) WHERE <clause> RETURN count(n) AS result
```

### Clause translation

| Clause | Cypher |
|---|---|
| `True` | `true` |
| `HasFirstName(name)` | `n.first = $first` |
| `HasLastName(name)` | `n.last = $last` |
| `HasPost(inner)` | `EXISTS { MATCH (n)-[:POSTED\|COMMENTED]->(p:POST) WHERE <inner> }` |
| `HasAuthor(inner)` | `EXISTS { MATCH (n)<-[:POSTED\|COMMENTED]-(u:USER) WHERE <inner> }` |
| `HasComment(inner)` | `EXISTS { MATCH (n)<-[:ON]-(c:POST) WHERE <inner> }` |
| `LikeCount(Exactly(n))` | `n.likes = $limit` |
| `LikeCount(GreaterThan(n))` | `n.likes > $limit` |
| `LikeCount(LessThan(n))` | `n.likes < $limit` |

Clauses nest arbitrarily, e.g.:

```cypher
MATCH (n:USER)
WHERE EXISTS {
  MATCH (n)-[:POSTED|COMMENTED]->(p:POST)
  WHERE EXISTS { MATCH (p)<-[:ON]-(c:POST) WHERE c.likes > $limit }
}
RETURN count(n) AS result
```
