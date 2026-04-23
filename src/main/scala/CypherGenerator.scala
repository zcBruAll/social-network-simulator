object CypherGenerator {

  def generate(event: GraphEvent): ExecutableQuery = event match {
    case GraphEvent.NewUser(id, first, last) =>
      ExecutableQuery(
        """MERGE (u:USER {id: $id})
          |ON CREATE SET u.first = $first, u.last = $last""".stripMargin,
        Map("id" -> id, "first" -> first, "last" -> last)
      )

    case GraphEvent.NewPost(id, user, text, date) =>
      ExecutableQuery(
        """MATCH (u:USER {id: $user})
          |MERGE (p:POST {id: $id})
          |ON CREATE SET p.text = $text, p.date = $date
          |MERGE (u)-[:POSTED]->(p)""".stripMargin,
        Map("id" -> id, "user" -> user, "text" -> text, "date" -> date)
      )

    case GraphEvent.NewComment(id, post, user, text, date) =>
      ExecutableQuery(
        """MATCH (u:USER {id: $user})
          |MATCH (p:POST {id: $post})
          |MERGE (c:COMMENT {id: $id})
          |ON CREATE SET c.text = $text, c.date = $date
          |MERGE (u)-[:COMMENTED]->(c)
          |MERGE (c)-[:ON]->(p)""".stripMargin,
        Map("id" -> id, "post" -> post, "user" -> user, "text" -> text, "date" -> date)
      )

    case GraphEvent.Like(post, user) =>
      ExecutableQuery(
        """MATCH (u:USER {id: $user})
          |MATCH (p:POST {id: $post})
          |MERGE (u)-[:LIKED]->(p)""".stripMargin,
        Map("post" -> post, "user" -> user)
      )

    case GraphEvent.UpdatePost(id, text) =>
      ExecutableQuery(
        """MATCH (p:POST {id: $id})
          |SET p.text = $text""".stripMargin,
        Map("id" -> id, "text" -> text)
      )

    case GraphEvent.DeletePost(id) =>
      ExecutableQuery(
        """MATCH (p:POST {id: $id})
          |OPTIONAL MATCH (c:COMMENT)-[:ON]->(p)
          |DETACH DELETE p, c""".stripMargin,
        Map("id" -> id)
      )

    case GraphEvent.DeleteUser(id) =>
      ExecutableQuery(
        """MATCH (u:USER {id: $id})
          |DETACH DELETE u""".stripMargin,
        Map("id" -> id)
      )
  }
}