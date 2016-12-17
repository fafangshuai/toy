package ffs.toy.xunlei;

/**
 * 新文件名策略接口
 */
interface NameStrategy {
  /**
   * 获取新文件名
   * @param oldName 旧文件名
   * @param index 序号
   * @return 新文件名
   */
  String getName(String oldName, int index);
}
