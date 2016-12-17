package ffs.toy.ikanman;

import java.util.List;

/**
 * 章节信息
 */
public class CInfo {
  // 漫画id
  private String bid;
  // 漫画名称
  private String bname;
  // 章节id
  private String cid;
  // 章节名称
  private String cname;
  // 文件名称列表
  private List<String> files;
  // 文件地址前缀（不包括域名信息）
  private String path;
  // 文件数
  private Integer len;

  public String getBname() {
    return bname;
  }

  public void setBname(String bname) {
    this.bname = bname;
  }

  public String getBid() {
    return bid;
  }

  public void setBid(String bid) {
    this.bid = bid;
  }

  public String getCid() {
    return cid;
  }

  public void setCid(String cid) {
    this.cid = cid;
  }

  public String getCname() {
    return cname;
  }

  public void setCname(String cname) {
    this.cname = cname;
  }

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Integer getLen() {
    return len;
  }

  public void setLen(Integer len) {
    this.len = len;
  }
}
