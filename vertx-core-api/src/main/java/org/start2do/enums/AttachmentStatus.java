package org.start2do.enums;

public enum AttachmentStatus {
  /** 上传完成 */
  FINISH((short) 0),
  /** 上传中 */
  UPLOADING((short) 1);
  private Short value;

  public Short getValue() {
    return value;
  }

  AttachmentStatus(Short value) {
    this.value = value;
  }
}
