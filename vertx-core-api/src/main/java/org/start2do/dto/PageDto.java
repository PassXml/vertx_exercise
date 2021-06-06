/*  大道至简 (C)2020 */
package org.start2do.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** @author HelloBox@outlook.com */
@DataObject
public class PageDto {
  private int cur;
  private Long total;
  private JsonArray array;

  public int getCur() {
    return cur;
  }

  public static PageDto build(int cur, Long total, JsonArray array) {
    PageDto dto = new PageDto();
    dto.array = array;
    dto.total = total;
    dto.cur = cur;
    return dto;
  }

  public static PageDto build(int cur, Long total, List list) throws Exception {
    PageDto dto = new PageDto();
    JsonArray array = new JsonArray();
    Class<?> aClass = null;
    if (!list.isEmpty()) {
      aClass = list.get(0).getClass();
      if (aClass.getAnnotation(DataObject.class) == null) {
        throw new Exception("不符合的类");
      }
      Method method = aClass.getMethod("toJson");
      for (Object o : list) {
        array.add(method.invoke(o));
      }
    }
    dto.array = array;
    dto.total = total;
    dto.cur = cur;
    return dto;
  }

  public PageDto() {}

  public PageDto(int cur, Long total, JsonArray array) {
    this.cur = cur;
    this.total = total;
    this.array = array;
  }

  public PageDto(JsonObject jsonObject) {
    this.cur = jsonObject.getInteger("cur");
    this.total = jsonObject.getLong("total");
    this.array = jsonObject.getJsonArray("result");
  }

  public <T> List<T> getList(T c) throws Exception {
    Class<?> aClass = c.getClass();
    Constructor<?> constructor = aClass.getConstructor(JsonObject.class);
    if (aClass.getAnnotation(DataObject.class) == null || constructor == null) {
      throw new Exception("不符合的类");
    }
    List list = new ArrayList();
    for (int i = 0; i < array.size(); i++) {
      list.add(constructor.newInstance(array.getJsonObject(i)));
    }
    return list;
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("cur", cur);
    jsonObject.put("total", total);
    jsonObject.put("result", array);
    return jsonObject;
  }

  public void setCur(int cur) {
    this.cur = cur;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  public JsonArray getArray() {
    return array;
  }

  public void setArray(JsonArray array) {
    this.array = array;
  }

  @Override
  public String toString() {
    return "PageDtos{" + "cur=" + cur + ", total=" + total + ", result=" + array + '}';
  }
}
