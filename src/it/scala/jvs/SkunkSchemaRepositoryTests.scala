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
import skunk.Session
import skunk.implicits._
import weaver._

import scala.concurrent.duration._

object SkunkSchemaRepositoryTests extends IOSuite {
  type Res = SchemaRepository[IO]

  val sharedResource: Resource[IO, Res] = DatabaseConfig
    .config[IO]
    .resource
    .flatMap(SkunkClient.connectionPool[IO](_))
    .evalTap(awaitReady)
    .map(SchemaRepository.skunkBased(_))

  private def awaitReady(
    pool: Resource[IO, Session[IO]]
  ): IO[Unit] = {

    val attempt = pool.use(_.execute(sql"select 1".query(skunk.codec.numeric.int4)))

    // using fs2 to avoid pulling in a retry library or reimplementing the same logic
    fs2.Stream.retry(attempt, 1.second, _ * 2, 10).compile.drain
  }

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
