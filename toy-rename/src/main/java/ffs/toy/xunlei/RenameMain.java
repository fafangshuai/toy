package ffs.toy.xunlei;


import ffs.toy.util.UsageBuilder;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 指定目录下批量文件重命名
 * <pre>
 * Usage: rename targetDir filterRegex namePattern begin length
 *   targetDir 目标目录
 *   filterRegex 过滤正则
 *   namePattern 新名称格式，数字用?代替
 *   begin 起始编号
 *   length 数字串补零后长度 1 -> 001
 * </pre>
 */
public class RenameMain {

  public static void main(String[] args) {
    if (args.length < 5) {
      usage();
    }
    String targetDirStr = args[0];
    String filterRegex = args[1];
    String namePattern = args[2];
    String beginStr = args[3];
    String lengthStr = args[4];

    File targetDir = new File(targetDirStr);
    if (!targetDir.exists() || !targetDir.isDirectory()) {
      System.err.println("解析目标目录出错");
      usage();
    }

    Pattern pattern = Pattern.compile(filterRegex);
    File[] files = targetDir.listFiles((dir, name) -> pattern.matcher(name).find());
    rename(files, new NumberNameStrategy(namePattern, Integer.parseInt(beginStr), Integer.parseInt(lengthStr)));
  }

  private static void usage() {
    String usageText = new UsageBuilder()
        .cmd("rename", "批量重命名指定目录下文件")
        .arg("targetDir", "目录")
        .arg("filterRegex", "正则表达式，按名字过滤出需要的文件")
        .arg("namePattern", "新文件名格式，数字用?代替")
        .arg("begin", "起始编号")
        .arg("length", "数字串补零后长度 例： lenght为3 则 1 -> 001")
        .build();
    System.out.println(usageText);
    System.exit(1);
  }

  private static void rename(File[] files, NumberNameStrategy factory) {
    if (files == null || files.length == 0) {
      System.err.println("没有匹配的文件");
      return;
    }
    int i = 0;
    for (File file : files) {
      String oldName = file.getName();
      String newName = factory.getName(oldName, i++);
      boolean ok = file.renameTo(new File(file.getParent(), newName));
      if (ok) {
        System.out.printf("重命名成功：%s -> %s\n", oldName, newName);
      } else {
        System.err.printf("重命名失败：%s -> %s\n", oldName, newName);
      }
    }
  }

}
