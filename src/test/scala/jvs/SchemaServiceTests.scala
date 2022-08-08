package jvs

import cats.effect.IO
import io.circe.Json
import jvs.services.AppError
import jvs.model.Schema
import jvs.model.SchemaId
import jvs.persistence.SchemaRepository
import jvs.services.SchemaService
import weaver._

object SchemaServiceTests extends SimpleIOSuite {
  private val aValidSchema = Schema(SchemaId("valid schema"), Json.obj())

  private val mkService = SchemaRepository.inMemory[IO].map { implicit repo =>
    SchemaService.instance[IO]
  }

  test("persistSchema succeeds once for a valid schema") {
    mkService
      .flatMap(_.persistSchema(aValidSchema))
      .as(success)
  }

  test("persistSchema fails when called twice with the same schema ID") {
    mkService.flatMap { service =>
      service.persistSchema(aValidSchema) *>
        service
          .persistSchema(aValidSchema)
          .attempt
          .map { result =>
            assert(result.isLeft) &&
            assert(result == Left(AppError.SchemaAlreadyExists))
          }
    }
  }

  test("getSchema: succeeds when the schema exists") {
    mkService.flatMap { service =>
      service.persistSchema(aValidSchema) *>
        service
          .getSchema(aValidSchema.id)
          .map { result =>
            assert(result == aValidSchema)
          }
    }
  }

  test("getSchema: fails when the schema doesn't exist") {
    mkService.flatMap { service =>
      service
        .getSchema(SchemaId("non-existent schema"))
        .attempt
        .map { result =>
          assert(result.isLeft) &&
          assert(result == Left(AppError.SchemaNotFound))
        }
    }
  }
}
