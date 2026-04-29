import org.neo4j.driver.Driver
import scala.jdk.CollectionConverters._
import sns.{Query, Users, Posts, Clause, Condition}

class QueryHandler(driver: Driver) {
  private var aliasCount = 0

  def handle(query: Query): Option[Int] = {
    val (label, clause) = query match {
      case Users(c) => ("USER", c)
      case Posts(c) => ("POST", c)
    }

    val (where, params) = translateClause("n", clause)

    val cypher = s"MATCH (n:$label) WHERE $where RETURN count(n) AS result"

    println(cypher)

    val session = driver.session()
    try {
      val result = session.run(cypher, params.asJava)
      if (result.hasNext) Some(result.single().get("result").asInt()) else Some(0)
    } catch {
      case e: Exception =>
        System.err.println(s"Query failed: ${e.getMessage}")
        None
    } finally {
      session.close()
    }
  }

  private def nextAlias(prefix: String): String = {
    aliasCount += 1
    s"$prefix$aliasCount"
  }

  private def translateClause(alias: String, clause: Clause): (String, Map[String, Any]) = {
    clause match {
      case Clause.True => ("true", Map.empty)

      case Clause.HasFirstName(name) =>
        (s"$alias.first = $$first", Map("first" -> name))

      case Clause.HasLastName(name) =>
        (s"$alias.last = $$last", Map("last" -> name))

      case Clause.HasPost(inner) =>
        val pAlias = nextAlias("p")
        val (subWhere, subParams) = translateClause(pAlias, inner)
        (s"EXISTS { MATCH ($alias)-[:POSTED|COMMENTED]->($pAlias:POST) WHERE $subWhere }", subParams)

      case Clause.HasComment(inner) =>
        val cAlias = nextAlias("c")
        val (subWhere, subParams) = translateClause(cAlias, inner)
        (s"EXISTS { MATCH ($alias)<-[:ON]-($cAlias:POST) WHERE $subWhere }", subParams)

      case Clause.HasAuthor(inner) =>
        val uAlias = nextAlias("u")
        val (subWhere, subParams) = translateClause(uAlias, inner)
        (s"EXISTS { MATCH ($alias)<-[:POSTED|COMMENTED]-($uAlias:USER) WHERE $subWhere }", subParams)

      case Clause.LikeCount(cond) =>
        val op = cond match {
          case Condition.Exactly(n) => "="
          case Condition.GreaterThan(n) => ">"
          case Condition.LessThan(n) => "<"
        }
        val threshold = cond match {
          case Condition.Exactly(n) => n
          case Condition.GreaterThan(n) => n
          case Condition.LessThan(n) => n
        }

        (s"COUNT { MATCH ()-[:LIKED]->($alias) } $op $$limit", Map("limit" -> threshold))
    }
  }
}