/*  大道至简 (C)2020 */
package org.start2do.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;

/** @author HelloBox@outlook.com */
@DataObject
@Data
public class Pageable {
  private Integer cur = 1;
  private Integer size = 10;
  private Integer start = (cur - 1) * size;
  private String keyword;

  public Pageable(Integer cur, Integer size, Integer start, String keyword) {
    this.cur = cur;
    this.size = size;
    this.start = start;
    this.keyword = keyword;
  }

  public Pageable() {}

  public JsonObject toJson() {
    JsonObject result = new JsonObject();
    result.put("cur", cur);
    result.put("size", size);
    result.put("start", start);
    result.put("keyword", keyword);
    return result;
  }

  public Pageable(JsonObject json) {
    cur = (Integer) json.getValue("cur");
    size = (Integer) json.getValue("size");
    start = (Integer) json.getValue("start");
    keyword = (String) json.getValue("keyword");
  }

  public Integer getCur() {
    return cur;
  }

  public void setCur(Integer cur) {
    this.cur = cur;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }
}
