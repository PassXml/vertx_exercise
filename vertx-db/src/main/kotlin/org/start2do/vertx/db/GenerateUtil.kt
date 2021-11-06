package org.start2do.vertx.db

import io.github.jklingsporn.vertx.jooq.generate.builder.BuildOptions
import io.github.jklingsporn.vertx.jooq.generate.builder.BuildOptions.BuildFlag
import io.github.jklingsporn.vertx.jooq.generate.builder.DelegatingVertxGenerator
import io.github.jklingsporn.vertx.jooq.generate.builder.PredefinedNamedInjectionStrategy
import io.github.jklingsporn.vertx.jooq.generate.builder.VertxGeneratorBuilder
import io.vertx.kotlin.core.datagram.datagramSocketOptionsOf
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.mysql.mysql.Mysql
import java.util.*
import kotlin.system.exitProcess

object GenerateUtil {
  @JvmStatic
  fun PostgresqlDatabaseOption(): Database {
    return Database()
      .withSchemata(
        SchemaMappingType().withInputSchema("public")
      )
      .withIncludes(".*")
      .withExcludes("schema_vertsion")
      .withIncludeTables(true)
      .withIncludePackages(false)
      .withIncludeUDTs(false)
      .withIncludeSequences(false)
      .withIncludeIndexes(false)
  }

  @JvmStatic
  fun MysqlDatabaseOption(): Database {
    return Database()
      .withIncludeTables(true)
      .withIncludePackages(false)
      .withIncludeUDTs(false)
      .withIncludeSequences(false)
      .withIncludeIndexes(false)
  }


  /**
   * driver url username password path packageName dataType
   */
  @JvmStatic
  fun main(array: Array<String?>) {
    if (array.isEmpty() || array.size != 7) {
      println("没有找到参数")
      println("driver url username password path packageName dataType")
      return
    }
    val databaseType = array[6]
    var database: Database
    when (databaseType) {
      "postgresql" -> {
        database = PostgresqlDatabaseOption()
      }
      "mysql" -> {
        database = MysqlDatabaseOption()
      }
      else -> {
        println("不支持数据库类型$databaseType")
        return
      }
    }
    run(
      Options(
        array[0]!!, array[1]!!, array[2]!!, array[3]!!
      ),
      array[4]!!,
      array[5]!!, database
    )
  }

  @JvmStatic
  fun run(option: Options, path: String, packageName: String, database: Database) {
    GenerationTool.generate(
      Configuration()
        .withJdbc(
          Jdbc()
            .withDriver(option.driver)
            .withUrl(option.url)
            .withUser(option.user)
            .withPassword(option.password)
        )
        .withGenerator(
          Generator().withName("org.start2do.vertx.db.ClassicReactiveVertxGeneratorWithDataObjectSupport")
            .withDatabase(database)
            .withGenerate(
              Generate()
                .withPojos(true)
                .withDaos(true)
                .withRecords(true)
                .withJavaTimeTypes(true)
                .withInterfaces(false)
                .withDeprecated(false)
                .withIndexes(false)
            )
            .withTarget(
              org.jooq.meta.jaxb.Target()
                .withClean(true)
                .withPackageName(packageName)
                .withDirectory(path)
            ).withStrategy(
              Strategy().withName("io.github.jklingsporn.vertx.jooq.generate.VertxGeneratorStrategy")
            )
        )
    )
  }
}

data class Options(
  val driver: String,
  val url: String,
  val user: String,
  val password: String
)

class ClassicReactiveVertxGeneratorWithDataObjectSupport : DelegatingVertxGenerator(
  VertxGeneratorBuilder.init()
    .withClassicAPI()
    .withPostgresReactiveDriver()
    .withGuice(true, PredefinedNamedInjectionStrategy.SCHEMA)
    .build(
      BuildOptions()
        .withBuildFlags(
          EnumSet.of(BuildFlag.GENERATE_DATA_OBJECT_ANNOTATION)
        )
    )
)
