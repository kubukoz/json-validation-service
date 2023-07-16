package jvs

import cats.effect.IO
import cats.implicits._
import io.circe.Json
import io.circe.syntax.*
import jvs.model.Schema
import jvs.model.SchemaId
import jvs.persistence.SchemaRepository
import jvs.services.AppError
import jvs.services.SchemaService
import weaver._

object SchemaServiceTests extends SimpleIOSuite {
  private val aValidSchema = Schema(SchemaId("valid schema"), Json.obj())

  private val aStrictSchema = Schema(
    SchemaId("string only"),
    Json.obj("type" := "string"),
  )

  private val anObjectSchema = Schema(
    SchemaId("schema for some objects"),
    Json.obj(
      "type" := "object",
      "properties" := Map(
        "greeting" := Map("type" := "string"),
        "age" := Map("type" := "number"),
      ),
      "required" := List("greeting"),
    ),
  )

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

  test("validateDocument: success") {
    mkService.flatMap { service =>
      service.persistSchema(anObjectSchema) *>
        service
          .validateDocument(
            anObjectSchema.id,
            Json.obj("greeting" := "hello", "age" := 40),
          )
          .as(success)
    }
  }

  test("validateDocument: schema doesn't exist") {
    mkService.flatMap { service =>
      service
        .validateDocument(aValidSchema.id, Json.obj())
        .attempt
        .map { result =>
          assert(result.isLeft) &&
          assert(result == Left(AppError.SchemaNotFound))
        }
    }
  }

  test("validateDocument: invalid document") {
    mkService.flatMap { service =>
      service.persistSchema(aStrictSchema) *>
        service
          .validateDocument(aStrictSchema.id, Json.obj())
          .attempt
          .map { result =>
            val msg =
              """instance type (object) does not match any allowed primitive type (allowed: ["string"])"""

            assert(result.isLeft) &&
            assert(result == AppError.InvalidDocument(msg :: Nil).asLeft)
          }
    }
  }

  test("validateDocument: document is valid with extra nulls") {
    mkService.flatMap { service =>
      service.persistSchema(anObjectSchema) *>
        service
          .validateDocument(
            anObjectSchema.id,
            Json.obj(
              "greeting" := "hello",
              "age" := Json.Null,
            ),
          )
          .as(success)
    }
  }
}
