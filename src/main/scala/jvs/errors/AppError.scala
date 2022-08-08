package jvs.errors

sealed trait AppError extends Exception with Product with Serializable {
  def getMessage: String
}

object AppError {
  case object SchemaAlreadyExists extends Exception("Schema already exists") with AppError
}
