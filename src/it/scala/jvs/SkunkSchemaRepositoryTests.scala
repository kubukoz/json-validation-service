package jvs

import cats.effect.IO
import cats.effect.Resource
import cats.effect.std.UUIDGen
import cats.implicits._
import io.circe.Json
import jvs.model.Schema
import jvs.model.SchemaId
import jvs.persistence.DatabaseConfig
import jvs.persistence.PersistenceError
import jvs.persistence.SchemaRepository
import jvs.persistence.SkunkClient
import weaver._

object SkunkSchemaRepositoryTests extends IOSuite {
  type Res = SchemaRepository[IO]

  val sharedResource: Resource[IO, Res] = DatabaseConfig
    .config[IO]
    .resource
    .flatMap(SkunkClient.connectionPool[IO](_))
    .map(SchemaRepository.skunkBased(_))

  test("a schema can be retrieved once inserted") { repo =>
    UUIDGen[IO].randomUUID.flatMap { uuid =>
      val aSchema = Schema(SchemaId(uuid.toString()), Json.obj())

      val result =
        repo.insert(aSchema) *>
          repo.get(aSchema.id)

      result.map { found =>
        assert(found == aSchema)
      }
    }
  }

  test("non-existent item lookup is caught") { repo =>
    UUIDGen[IO].randomUUID.flatMap { uuid =>
      val aSchemaId = SchemaId(uuid.toString())

      val result = repo.get(aSchemaId)

      result.attempt.map { result =>
        assert(result.isLeft) &&
        assert(result == PersistenceError.NotFound.asLeft)
      }
    }
  }

}