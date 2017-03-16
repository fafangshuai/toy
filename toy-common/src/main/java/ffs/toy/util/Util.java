package ffs.toy.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Util {
  /**
   * 获取文件扩展名
   *
   * @param name 文件名
   */
  public static String getExt(String name) {
    return getExt(name, false);
  }

  /**
   * 退出程序并打印消息，格式化消息
   *
   * @param fmt  消息格式
   * @param objs 参数
   */
  public static void exit(String fmt, Object... objs) {
    exit(String.format(fmt, objs));
  }

  /**
   * 退出程序并打印消息
   *
   * @param msg 消息
   */
  public static void exit(String msg) {
    System.err.println(msg);
    System.exit(1);
  }

  /**
   * 去除url前后的斜杠
   */
  public static String trimUrl(String url) {
    int start = 0;
    int end = url.length();
    char[] val = url.toCharArray();
    while (val[start++] == '/') {
    }
    start--;
    while (val[--end] == '/') {
    }
    end++;
    return new String(val, start, end - start);
  }

  /**
   * 如果文件不存在则创建
   *
   * @param file 文件
   * @return boolean 是否创建成功
   */
  public static boolean createFileIfAbsent(File file) throws IOException {
    if (!file.exists()) {
      File dirs = file.getParentFile();
      if (dirs != null && !dirs.exists()) {
        boolean ok = dirs.mkdirs();
        if (!ok) {
          return false;
        }
      }
      boolean ok = file.createNewFile();
      if (!ok) {
        return false;
      }
    }
    return true;
  }

  /**
   * 获取文件中的文本
   *
   * @param file    文件
   * @param charset 编码
   * @return 文本
   * @throws Exception
   */
  public static String getTextFromFile(File file, String charset) throws Exception {
    if (!file.exists()) {
      return null;
    }
    List<String> lines = Files.readAllLines(file.toPath(), Charset.forName(charset));
    StringBuilder text = new StringBuilder();
    for (String line : lines) {
      text.append(line).append("\n");
    }
    return text.toString();
  }

  /**
   * 创建多级目录
   *
   * @param parent 指定父目录
   * @param dirs   多个下级（可选）
   * @return boolean 是否创建成功
   */
  public static File mkdirs(String parent, String... dirs) {
    if (parent == null) {
      return null;
    }
    String illegalChars = "[/\\\\:*?<>|]";
    String path = parent;
    if (dirs.length > 0) {
      for (String dir : dirs) {
        path += File.separator + dir.replaceAll(illegalChars, "");
      }
    }
    File dir = new File(path);
    if (dir.exists() || dir.mkdirs()) {
      return dir;
    } else {
      return null;
    }
  }

  /**
   * 获取文件扩展名
   *
   * @param filename 原文件名
   * @param withDot  是否加前缀点，方便拼接
   * @return 文件扩展名
   */
  public static String getExt(String filename, boolean withDot) {
    int idx = filename.lastIndexOf(".");
    if (!withDot) {
      idx += 1;
    }
    return filename.substring(idx).toLowerCase();
  }

  /**
   * 获取文件中的所有行
   *
   * @param inputFile 输入文件
   * @return 所有行的List
   */
  public static List<String> getLines(String inputFile) {
    try {
      return getLines(new FileInputStream(inputFile));
    } catch (FileNotFoundException e) {
      throw new RuntimeException("获取文件中的所有行出错", e);
    }
  }

  /**
   * 获取文件中的所有行
   *
   * @param is 输入流
   * @return 所有行的List
   */
  public static List<String> getLines(InputStream is) {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
      List<String> result = new ArrayList<>();
      String line;
      while ((line = br.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          result.add(line);
        }
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException("获取文件中的所有行出错", e);
    }
  }
}
