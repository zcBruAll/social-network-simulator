import org.neo4j.driver.{AuthTokens, GraphDatabase, Session}
import sns.Simulator
import scala.jdk.CollectionConverters._

@main def Main =
  val s = Simulator(seed = 1337)

  val driver = GraphDatabase.driver(
    "neo4j://localhost:7687", AuthTokens.basic("neo4j", "beydb-beepr"))
  driver.verifyConnectivity()

  val handlerObj = new QueryHandler(driver)

  for i <- 0 until 676 do {
    if ((i & 0b11) != 0b11) {
      val payload = s.randomEvent()
      EventProcessor.processEventString(payload) match {
        case Some(cypherQuery) =>
          val result = driver.executableQuery(cypherQuery.cypher).withParameters(cypherQuery.parameters.asJava).execute
          //println(result.summary())
        case None =>
          println("Could not generate query.")
      }
    } else {
      s.challenge(q => handlerObj.handle(q))
    }
  }
  println(s.score())
  driver.close()