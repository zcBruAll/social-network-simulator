import org.neo4j.driver.{AuthTokens, GraphDatabase, Session}
import sns.Simulator
import scala.jdk.CollectionConverters._

def handler(query: Simulator.Query): Option[Int] =
  None

@main def Main =
  val s = Simulator(seed = 1337)

  val driver = GraphDatabase.driver(
    "neo4j://localhost:7687", AuthTokens.basic("neo4j", "beydb-beepr"))
  driver.verifyConnectivity()

  for i <- 0 until 130 do {
    val payload = s.randomEvent()
    EventProcessor.processEventString(payload) match {
    case Some(cypherQuery) =>
      println("Successfully generated query:")
      println(cypherQuery)

      val result = driver.executableQuery(cypherQuery.cypher).withParameters(cypherQuery.parameters.asJava).execute
      println(result.summary())
    case None =>
      println("Could not generate query.")
    }
  }
  driver.close()