package jvs.persistence

sealed trait PersistenceError extends Exception with Product with Serializable

object PersistenceError {
  case object Conflict extends PersistenceError
  case object NotFound extends PersistenceError
}
