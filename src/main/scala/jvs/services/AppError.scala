package jvs.services

sealed trait AppError extends Exception with Product with Serializable {
  def getMessage: String
}

object AppError {
  case object SchemaAlreadyExists extends Exception("Schema already exists") with AppError
  case object SchemaNotFound extends Exception("Schema not found") with AppError

  final case class InvalidDocument(messages: List[String])
    extends Exception("Invalid document: " + messages.mkString(", "))
    with AppError

}
