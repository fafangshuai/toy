package ffs.toy.xunlei;

import ffs.toy.util.UsageBuilder;
import ffs.toy.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class XunleiMain {
  private static CrackSuperSpeedService crackSuperSpeedService = new CrackSuperSpeedService();
  private static UsageBuilder usageBuilder = new UsageBuilder();

  public static void main(String[] args) {
    if (args.length < 1) {
      usage();
    }
    try {
      if (args[0].equals("crack")) {
        String inputHome = args.length > 1 ? args[1] : null;
        crackSuperSpeedService.crack(getXunleiHome(inputHome));
      } else {
        usage();
      }
    } catch (Exception e) {
      System.err.println("错误信息：" + e.getMessage());
      usage();
    }
  }

  private static String getXunleiHome(String arg) {
    try {
      String home;
      File config = new File("config");
      if (arg == null) {
        List<String> lines = Util.getLines(new FileInputStream(config));
        if (lines.size() > 0) {
          return lines.get(0);
        }
      } else {
        home = arg.trim();
        FileOutputStream fos = new FileOutputStream(config);
        byte[] bytes = home.getBytes("utf-8");
        fos.write(bytes, 0, bytes.length);
        fos.close();
        return home;
      }
      throw new RuntimeException("没有指定迅雷安装目录");
    } catch (Exception e) {
      throw new RuntimeException("获取迅雷安装目录出错", e);
    }
  }

  private static void usage() {
    String text = usageBuilder
        .cmd("crack", "破解被举报的高速通道资源")
        .arg("xunleiHome", "迅雷安装目录（第一次需指定）", false)
        .build();
    System.err.println(text);
    System.exit(1);
  }
}
