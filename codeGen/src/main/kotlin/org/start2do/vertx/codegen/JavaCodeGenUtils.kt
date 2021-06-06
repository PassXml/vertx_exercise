/*  大道至简 (C)2021 */
package org.start2do.vertx.codegen

import io.vertx.core.json.JsonObject
import org.jboss.forge.roaster.Roaster
import org.jboss.forge.roaster.model.source.FieldSource
import org.jboss.forge.roaster.model.source.JavaClassSource
import org.jboss.forge.roaster.model.source.MethodSource
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.*

/**
 * @Author passxml
 * @date 2021/6/5:09:21
 */
class JavaCodeGenUtils {
  companion object {
    private fun defaultConstructor(parse: JavaClassSource) {
      if (parse.hasMethodSignature(parse.name)) {
        return
      }
      parse.addMethod().setName(parse.name).setConstructor(true).setPublic().setBody("")
    }

    private fun jsonObjectConstructor(parse: JavaClassSource) {
      var addAnnotation = false
      if (parse.hasMethodSignature(parse.name, JsonObject::class.java)) {
        println("${parse.qualifiedName}构造器存在")
        val method = parse.getMethod(parse.name, JsonObject::class.java)
        if (!checkIsForceOverride(method)) {
          return
        }
        println("构造器覆盖")
        addAnnotation = true
        parse.removeMethod(method)
      }
      defaultConstructor(parse)
      val body = StringJoiner("\n")
      for (field in parse.fields) {
        if (field.type.isArray) {
          body.add(CodeGenDecodeFactory.array2Json(field))
        } else {
          body.add(CodeGenDecodeFactory.getFunction(CodeGenDecodeFactory.fieldType(field)).invoke(field))
        }
      }
      val method = parse.addMethod()
      if (addAnnotation) {
        addAnnotation(method)
      } else {
        method
      }.setPublic().setConstructor(true).setBody(body.toString()).addParameter(
        JsonObject::class.java, "json"
      )
    }

    private fun checkIsForceOverride(method: MethodSource<JavaClassSource>): Boolean {
      val annotation = method.getAnnotation(Start2doCodeGen::class.java)
      return annotation != null && annotation.getStringValue("force") == "true"
    }

    private fun checkAnnotation(parse: JavaClassSource): Boolean =
      parse.getAnnotation(Start2doCodeGen::class.java) != null

    private fun addToJson(parse: JavaClassSource) {
      var addAnnotation = false
      if (parse.hasMethodSignature("toJson")) {
        println("${parse.qualifiedName}存在toJson")
        val method = parse.getMethod("toJson")
        if (!checkIsForceOverride(method)) {
          return
        }
        println("toJson覆盖")
        parse.removeMethod(method)
        addAnnotation = true
      }
      val body = StringJoiner("\n")
      body.add("JsonObject result=new JsonObject();")
      for (field in parse.fields) {
        if (field.type.isArray) {
          body.add(CodeGenEncodeFactory.array2JsonArray(field))
        } else {
          body.add(CodeGenEncodeFactory.getFunction(CodeGenEncodeFactory.fieldType(field)).invoke(field))
        }
      }
      body.add("return result;")
      val method = parse.addMethod()

      if (addAnnotation) {
        addAnnotation(method)
      } else {
        method
      }.setPublic().setName("toJson")
        .setReturnType(JsonObject::class.java).body =
        body.toString()
    }

    private fun addAnnotation(method: MethodSource<JavaClassSource>): MethodSource<JavaClassSource> {
      method.addAnnotation(Start2doCodeGen::class.java).setLiteralValue("force", "true")
      return method
    }

    @JvmStatic
    fun list(path: String): Array<String> {
      val file = Paths.get(path).toFile()
      return file.walk().filter { it.isFile }.filter {
        it.name !in listOf("package-info.java", "module-info.java")
      }.filter { it.extension in listOf("java") }.map { it.absolutePath }
        .toList().toTypedArray()
    }

    @JvmStatic
    fun create(path: Array<String>) {
      for (s in path) {
        val fileInputStream = FileInputStream(s)
        println(s)
        val parse = Roaster.parse(
          JavaClassSource::class.java,
          fileInputStream
        )
        if (checkAnnotation(parse)) {
          jsonObjectConstructor(parse)
          addToJson(parse)
          val fileOutputStream = FileOutputStream(s)
          fileOutputStream.write(
            parse.toString().toByteArray()
          )
          fileOutputStream.close()
        }
        fileInputStream.close()
      }
    }
  }
}

object CodeGenEncodeFactory {

  private val map = mutableMapOf<Class<*>, (field: FieldSource<JavaClassSource>) -> String>()
  val notFound: (field: FieldSource<JavaClassSource>) -> String = { field ->
    """
        result.put("${field.name}",Json.encode(this.${field.name}));
    """.trimIndent()
  }
  private val baseFunction: (field: FieldSource<JavaClassSource>) -> String = { field ->
    """
      result.put("${field.name}",this.${field.name});
    """.trimIndent()
  }

  init {
    arrayOf(
      java.lang.Integer::class.java,
      java.lang.Float::class.java,
      java.lang.Double::class.java,
      java.lang.Byte::class.java,
      java.lang.String::class.java,
      Int::class.java,
      Float::class.java,
      String::class.java,
      Double::class.java,
      Byte::class.java,
      Char::class.java,
    ).forEach {
      map[it] = baseFunction
    }
  }

  fun array2JsonArray(field: FieldSource<JavaClassSource>): String {
    val fieldToType = map[fieldType(field)]
    if (fieldToType == notFound) {
      println("没有找到")
      println(field.name)
    } else {
      return """
                   if (this.${field.name}!=null){
                     JsonArray array = new JsonArray();
                      for (${fieldType(field).name} obj : this.${field.name}) {
                          array.add(obj);
                      }
                      result.put("${field.name}",array);
                   }
      """.trimIndent()
    }
    return "//TODO 手动实现"
  }

  fun getFunction(
    clazz: Class<*>
  ): (FieldSource<JavaClassSource>) -> String {
    return map[clazz] ?: notFound
  }

  fun fieldType(
    field: FieldSource<JavaClassSource>
  ): Class<*> {
    for (entry in map) {
      if (field.type.isType(entry.key)) {
        return entry.key
      }
    }
    return java.lang.Object::class.java
  }
}

object CodeGenDecodeFactory {

  private val longBase: (field: FieldSource<JavaClassSource>) -> String = {
    """if (json.containsKey("${it.name}")){
         this.${it.name}=json.getLong("${it.name}");
      }""".trimMargin()
  }
  private val doubleBase: (field: FieldSource<JavaClassSource>) -> String = {
    """if (json.containsKey("${it.name}")){
      this.${it.name}=json.getDouble(\"${it.name}\");
     }""".trimMargin()
  }
  private val floatBase: (field: FieldSource<JavaClassSource>) -> String = {
    """if (json.containsKey("${it.name}")){
      this.${it.name}=json.getFloat(\"${it.name}\");
      }""".trimMargin()
  }

  private val map = mutableMapOf<Class<*>, (field: FieldSource<JavaClassSource>) -> String>(
    Pair(Long::class.java, longBase),
    Pair(java.lang.Long::class.java, longBase),
    Pair(java.lang.Double::class.java, doubleBase),
    Pair(Double::class.java, doubleBase),
    Pair(java.lang.Float::class.java, floatBase),
    Pair(Float::class.java, floatBase)
  )
  val notFound: (field: FieldSource<JavaClassSource>) -> String = { field ->
    """if (json.containsKey("${field.name}")){
          this.${field.name}=Json.decodeValue(json.getString("${field.name}"),${fieldType(field).name}.class);
        }
    """.trimIndent()
  }
  private val baseFunction: (field: FieldSource<JavaClassSource>) -> String = { field ->
    """if (json.containsKey("${field.name}")){
              this.${field.name}=(${field.type})json.getValue("${field.name}");
      }
    """.trimIndent()
  }

  fun getFunction(
    clazz: Class<*>
  ): (FieldSource<JavaClassSource>) -> String {
    return map[clazz] ?: notFound
  }

  fun fieldType(
    field: FieldSource<JavaClassSource>
  ): Class<*> {
    for (entry in map) {
      if (field.type.isType(entry.key)) {
        return entry.key
      }
    }
    if (field.type.isType(List::class.java)) {
      return java.util.List::class.java
    }
    if (field.type.isType(Map::class.java)) {
      return java.util.Map::class.java
    }
    return java.lang.Object::class.java
  }

  fun array2Json(field: FieldSource<JavaClassSource>): String {
    return """
         if (json.containsKey("${field.name}")) {
           JsonArray ${field.name}JsonArray=json.getJsonArray("${field.name}");
           int ${field.name}Size=${field.name}JsonArray.size()-1;
           ${field.type} ${field.name}Array = new ${fieldType(field).simpleName}[${field.name}Size];
           for (int i = 0; i < ${field.name}Size; i++) {
              ${field.name}Array[i]=(${fieldType(field).name})${field.name}JsonArray.getValue(i);
           }
           this.${field.name} = ${field.name}Array;
         }
    """.trimIndent()
  }

  init {
    arrayOf(
      java.lang.Integer::class.java,
      java.lang.Byte::class.java,
      java.lang.String::class.java,
      Int::class.java,
      String::class.java,
      Byte::class.java,
      Char::class.java,
    ).forEach {
      map[it] = baseFunction
    }
  }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class Start2doCodeGen(
  val force: Boolean = false
)
