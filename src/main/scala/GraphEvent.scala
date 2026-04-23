sealed trait GraphEvent

object GraphEvent {
  case class NewUser(id: String, first: String, last: String) extends GraphEvent
  case class NewPost(id: String, user: String, text: String, date: String) extends GraphEvent
  case class NewComment(id: String, post: String, user: String, text: String, date: String) extends GraphEvent
  case class Like(post: String, user: String) extends GraphEvent
  case class UpdatePost(id: String, text: String) extends GraphEvent
  case class DeletePost(id: String) extends GraphEvent
  case class DeleteUser(id: String) extends GraphEvent
}
