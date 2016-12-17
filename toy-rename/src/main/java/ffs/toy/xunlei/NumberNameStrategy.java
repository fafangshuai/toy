package ffs.toy.xunlei;

import java.text.DecimalFormat;

/**
 * 基于编号的命名策略
 */
class NumberNameStrategy implements NameStrategy {
  // 新名字格式，数字用 "?" 代替
  private final String pattern;
  // 起始编号
  private final int begin;
  // 编号的长度，例：如果长度为3，则编号为003
  private final int length;

  private DecimalFormat df;

  NumberNameStrategy(String pattern, int begin, int length) {
    this.pattern = pattern;
    this.begin = begin < 1 ? 1 : begin;
    this.length = length < 1 ? 1 : length;

    this.df = new DecimalFormat(getZeros());
  }

  @Override
  public String getName(String oldName, int index) {
    String ext = oldName.substring(oldName.lastIndexOf('.'));
    return pattern.replaceAll("\\?", df.format(begin + index)) + ext;
  }

  private String getZeros() {
    char[] zeros = new char[length];
    for (int i = 0; i < length; i++) {
      zeros[i] = '0';
    }
    return new String(zeros);
  }
}
