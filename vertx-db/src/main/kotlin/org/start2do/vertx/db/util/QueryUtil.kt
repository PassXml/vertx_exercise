package org.start2do.vertx.db.util

import org.jooq.Condition

object QueryUtil {
  fun create() = mutableListOf<Condition>()
  fun add(where: MutableList<Condition>, notEmpty: Boolean?, eq: Condition) {
    if (notEmpty == null) {
      return;
    }
    where.add(eq)
  }

  fun create(vararg eq: QueryCondition): MutableList<Condition> {
    val result = create();
    for (condition in eq) {
      add(result, condition.c, condition.eq)
    }
    return result
  }
}

data class QueryCondition(
  val c: Boolean? = false,
  val eq: Condition
)
