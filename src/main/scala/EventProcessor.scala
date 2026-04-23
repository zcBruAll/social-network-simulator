import ujson.Value

object EventProcessor {

  def processEventString(jsonString: String): Option[ExecutableQuery] = {
    try {
      val parsed: Value = ujson.read(jsonString)
      val eventType = parsed("event").str
      val args = parsed("arguments")

      val graphEvent: GraphEvent = eventType match {
        case "new-user" =>
          GraphEvent.NewUser(args("id").str, args("first").str, args("last").str)

        case "new-post" =>
          GraphEvent.NewPost(args("id").str, args("user").str, args("text").str, args("date").str)

        case "new-comment" =>
          GraphEvent.NewComment(args("id").str, args("post").str, args("user").str, args("text").str, args("date").str)

        case "like" =>
          GraphEvent.Like(args("post").str, args("user").str)

        case "update-post" =>
          GraphEvent.UpdatePost(args("id").str, args("text").str)

        case "delete-post" =>
          GraphEvent.DeletePost(args("id").str)

        case "delete-user" =>
          GraphEvent.DeleteUser(args("id").str)

        case unknown =>
          throw new IllegalArgumentException(s"Unknown event type received: $unknown")
      }

      Some(CypherGenerator.generate(graphEvent))

    } catch {
      case e: Exception =>
        System.err.println(s"Failed to process JSON payload. Reason: ${e.getMessage}")
        None
    }
  }
}