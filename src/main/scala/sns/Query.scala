package sns

import sns.Simulator

/** A query against the contents of a simulator. */
sealed trait Query:

  /** Returns the result of this query evaluated on the contents of `simulator`. */
  def evaluate(simulator: Simulator): Int

object Query:

  sealed trait ResultSet:
    def count(simulator: Simulator): Int

  /** A tag representing a type of entity. */
  sealed trait Entity extends ResultSet

  /** A tag representing users. */
  case class User() extends Entity:

    def count(simulator: Simulator): Int =
      simulator.users.count(_.isDefined)

    override def toString(): String = "user"

  /** A tag representing posts. */
  case class Post() extends Entity:

    def count(simulator: Simulator): Int =
      simulator.posts.count(_.isDefined)

    override def toString(): String = "post"

  /** A tag representing likes. */
  case class Like() extends Entity:

    def count(simulator: Simulator): Int =
      simulator.posts.flatMap((p) => p.map(_.likes)).length

    override def toString(): String = "like"

  /** Queries the number of instances of an entity. */
  case class Count(entity: ResultSet) extends Query:

    def evaluate(simulator: Simulator): Int =
      entity match
        case e : User =>
          e.count(simulator)
        case e : Post =>
          e.count(simulator)
        case Like() =>
          simulator.posts.flatMap((p) => p.map(_.likes)).length

    override def toString(): String =
      s"count:${entity}"