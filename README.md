# Social Network Simulator

## Students
- Allan Brunner
- Valentin Monod
- Axel Hall
- James Zeiger

## Event types
- new-user (id, first, last)
  - ```neo4j
    MERGE (u:USER {id: ${id}})
    ON CREATE SET u.first = "${first}", u.last = "${last}"
    ```
- new-post (id, user, text, date)
  - ```neo4j
    MATCH (u:USER {id: ${user}})
    MERGE (p:POST {id: ${id}})
    ON CREATE SET p.text = "${text}", p.date = "${date}"
    MERGE (u)-[:POSTED]->(p)
    ```
- new-comment (id, post, user, text, date)
  - ```neo4j
    MATCH (u:USER {id: ${user}})
    MATCH (p:POST {id: ${post}})
    MERGE (c:COMMENT {id: ${id}})
    ON CREATE SET c.text = "${text}", c.date = "${date}"
    MERGE (u)-[:COMMENTED]->(c)
    MERGE (c)-[:ON]->(p)
    ```
- like (post, user)
  - ```neo4j
    MATCH (u:USER {id: ${user}})
    MATCH (p:POST {id: ${post}})
    MERGE (u)-[:LIKED]->(p)
    ```
- update-post (id, text)
  - ```neo4j
    MATCH (p:POST {id: ${id}})
    SET p.text = "${text}"
    ```
- delete-post (id)
  - ```neo4j
    MATCH (p:POST {id: ${id}})
    OPTIONAL MATCH (c:COMMENT)-[:ON]->(p)
    DETACH DELETE p, c
    ```
- delete-user (id)
  - ```neo4j
    MATCH (u:USER {id: ${id}})
    DETACH DELETE u
    ```

## Nodes
- USER (UNIQUE(id), first, last)
  - ```neo4j
    CREATE CONSTRAINT unique_USER
    FOR u:USER
    REQUIRE u.id IS UNIQUE
    ```
- COMMENTS (UNIQUE(id), text, date)
  - ```neo4j
    CREATE CONSTRAINT unique_COMMENT
    FOR c:COMMENT
    REQUIRE c.id IS UNIQUE
    ```
- POST (UNIQUE(id), text, date)
  - ```neo4j
    CREATE CONSTRAINT unique_POST
    FOR p:POST
    REQUIRE p.id IS UNIQUE
    ```

## Relationships
- USER -COMMENTED {UNIQUE(userid, commentId)}> COMMENT
  - Duplication constraint: 
    ```neo4j 
    CREATE CONSTRAINT unique_COMMENTED
    FOR ()-[r:COMMENTED]-()
    REQUIRE (r.comment, r.user) IS UNIQUE
    ```
- COMMENT -ON {UNIQUE(commentId, postId)}> POST
  - Duplication constraint:
    ```neo4j
    CREATE CONSTRAINT unique_ONCOMMENT
    FOR ()-[r:ON]-()
    REQUIRE (r.post, r.comment) IS UNIQUE
    ```
- USER -LIKED {UNIQUE(userId, postId)}> POST
  - Duplication constraint:
    ```neo4j
    CREATE CONSTRAINT unique_LIKED
    FOR ()-[r:LIKED]-()
    REQUIRE (r.post, r.user) IS UNIQUE
    ```
- USER -POSTED {UNIQUE(userId, postId)}> POST
  - Duplication constraint:
    ```neo4j
    CREATE CONSTRAINT unique_POSTED
    FOR ()-[r:POSTED]-()
    REQUIRE (r.post, r.user) IS UNIQUE
    ```
_NB: The duplication constraints aren't needed since we always use MERGE instead of CREATE_
