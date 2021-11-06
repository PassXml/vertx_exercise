package org.start2do.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

@DataObject
@Setter
@Getter
public class AttachmentPojo {
  private Long id;
  private String path;
  private String fileName;
  private Integer userId;
  private String md5;

  public JsonObject toJson() {
    JsonObject result = new JsonObject();
    result.put("id", id);
    result.put("path", path);
    result.put("fileName", fileName);
    result.put("userId", userId);
    result.put("md5", md5);
    return result;
  }

  public AttachmentPojo(JsonObject json) {
    id = json.getLong("id");
    path = json.getString("path");
    fileName = json.getString("fileName");
    userId = json.getInteger("userId");
    md5 = json.getString("md5");
  }

  public AttachmentPojo() {}

  public AttachmentPojo(Long id, String path, String fileName, Integer userId, String md5) {
    this.id = id;
    this.path = path;
    this.fileName = fileName;
    this.userId = userId;
    this.md5 = md5;
  }

}
