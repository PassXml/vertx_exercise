package org.start2do.vertx.pojo

@Target(AnnotationTarget.CLASS)
annotation class Configuration(
  val scanPackages: Array<String> = [],
  val resourcesDir: String = "./"

)
