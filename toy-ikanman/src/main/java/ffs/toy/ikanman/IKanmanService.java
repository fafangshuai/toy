package ffs.toy.ikanman;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.script.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

class IKanmanService {
  static final String WWW_HOST = "http://www.ikanman.com";
  static final String IMAGE_HOST = "http://p.yogajx.com";
  static final String CHAPTER_NAV_URL = "http://www.ikanman.com/support/chapter.ashx?";
  private static final int MAX_RECONNECT_TIMES = 3;

  public static void main(String[] args) throws IOException {
    String remoteUrl = IMAGE_HOST + "/ps3/l/恋着的♀X爱着的♀[藤坂空树]/act_15/jmydm0016-83822.JPG";
    URL url = new URL(remoteUrl);
    URLConnection conn = url.openConnection();
    InputStream is = conn.getInputStream();
    copy(is, new FileOutputStream("D:\\Desktop\\a.jpg"));
  }

  private static ScriptEngine engine;

  /**
   * 初始化脚本引擎
   */
  static void initScriptEngine() {
    try {
      engine = new ScriptEngineManager().getEngineByName("javascript");
      InputStream in = IKanmanService.class.getClassLoader().getResourceAsStream("ikanman.js");
      engine.eval(new InputStreamReader(in));
    } catch (ScriptException e) {
      throw new RuntimeException("初始化js引擎出错", e);
    }
  }

  /**
   * 解析漫画目录获取章节地址，从wap站解析
   *
   * @param comicUrl 漫画url后缀 e.g. HOST/comic/4705/
   * @return List 章节url后缀集合
   */
  List<String[]> resolveCatalog(String comicUrl) {
    Document doc = getDocument(comicUrl);
    Elements aList = doc.select(".chapter-list a");
    List<String[]> result = new ArrayList<>();
    for (Element a : aList) {
      result.add(new String[]{IKanmanService.WWW_HOST + a.attr("href"), a.attr("title")});
    }
    return result;
  }

  /**
   * 解析章节信息获取每一页的具体地址信息，从www站解析
   *
   * @param chapterUrl 章节url e.g. HOST/comic/4705/251315.html
   * @return 章节信息的json
   */
  String resolveChapter(String chapterUrl) {
    try {
      Document doc = getDocument(chapterUrl.trim());
      Elements scriptList = doc.select("script");
      String reg = "decryptDES\\([\"'](.*)[\"']\\)";
      Pattern p = Pattern.compile(reg);
      for (Element script : scriptList) {
        String text = script.toString();
        Matcher m = p.matcher(text);
        if (m.find()) {
          String cipherText = m.group(1);
          String json = decryptToJson(cipherText);
          System.out.printf("%s --> %s\n", chapterUrl, json);
          return json;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("解析章节信息出错：" + chapterUrl, e);
    }
    return null;
  }

  /**
   * 下载
   *
   * @param remoteUrl 文件url
   * @param localPath 本地路径
   * @param referer   跨域支持
   */
  void download(String remoteUrl, String localPath, String referer) {
    try {
      // 把空格转换为%20
      String noSpaceUrl = remoteUrl.replaceAll(" ", "%20");
      // 编码路径中的中文
      URL url = new URL(noSpaceUrl);
      URLConnection conn = url.openConnection();
      // 增加跨域支持
      if (referer != null) {
        conn.addRequestProperty("Referer", referer);
      }
      // 从网络中下载
      InputStream is = conn.getInputStream();
      // 写入本地
      FileOutputStream fos = new FileOutputStream(new File(localPath));
      copy(is, fos);
      System.out.printf("下载文件成功：%s --> %s\n", remoteUrl, localPath);
      is.close();
      fos.close();
    } catch (IOException e) {
      throw new RuntimeException(format("下载文件失败：%s --> %s", remoteUrl, localPath), e);
    }
  }

  /**
   * 从章节导航中解析漫画目录
   *
   * @param bid      漫画id
   * @param startCid 起始章节id
   * @return 章节url列表
   */
  List<String> resolveCatalogFromChapterNav(int bid, int startCid) {
    String chapterNavUrlPrefix = CHAPTER_NAV_URL + "bid=" + bid + "&cid=";
    String chapterUrlPrefix = WWW_HOST + "/comic/" + bid + "/";
    List<String> result = new ArrayList<>();
    try {
      int nextCid = startCid;
      while (nextCid > 0) {
        result.add(chapterUrlPrefix + nextCid + ".html");
        URL url = new URL(chapterNavUrlPrefix + nextCid);
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        Map<String, Object> data = JSON.parseObject(getBytes(in), Map.class);
        nextCid = (int) data.get("n");
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException("解析章节地址出错", e);
    }
  }

  /**
   * 从输入流获取字节数组
   *
   * @param in 输入流
   * @return 字节数组
   */
  private byte[] getBytes(InputStream in) throws IOException {
    byte[] b = new byte[512];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int len;
    while ((len = in.read(b)) != -1) {
      out.write(b, 0, len);
    }
    out.close();
    in.close();
    return out.toByteArray();
  }

  /**
   * 解密章节详细信息密文
   *
   * @param input 密文
   * @return json
   */
  private String decryptToJson(String input) throws Exception {
    Invocable inv = (Invocable) engine;
    Object o = inv.invokeFunction("decryptDES", input);
    engine.eval(String.valueOf(o));
    return JSON.toJSONString(engine.get("cInfo"), new ValueFilter() {
      @Override
      public Object process(Object object, String name, Object value) {
        if (name.equals("files")) {
          Bindings jsFiles = (Bindings) value;
          return jsFiles.values();
        }
        return value;
      }
    });
  }

  /**
   * 获取html文档，提供重试机制
   *
   * @param url 地址
   * @return 文档
   */
  private Document getDocument(String url) {
    Document doc = null;
    int reconnectCount = 1;
    do {
      try {
        doc = Jsoup.connect(url).get();
      } catch (Exception e) {
        if (reconnectCount > MAX_RECONNECT_TIMES) {
          System.err.printf("Connect to url: [%s] failed\n", url);
          throw new RuntimeException("获取html文档失败", e);
        }
        System.out.printf("Reconnect to url: [%s], %d times\n", url, reconnectCount++);
      }
    } while (doc == null);
    return doc;
  }

  /**
   * 从输入流向输出流复制字节
   *
   * @param in  输入流
   * @param out 输出流
   * @throws IOException IO异常
   */
  private static void copy(InputStream in, OutputStream out) throws IOException {
    int n;
    byte[] buffer = new byte[1024 * 4];
    while ((n = in.read(buffer)) != -1) {
      out.write(buffer, 0, n);
    }
  }

  /**
   * 特殊的referer格式，http://www.ikanman.com/comic/4732/235794_4.html
   *
   * @param comicId   漫画id
   * @param chapterId 章节id
   * @param num       第几页
   * @return referer
   */
  String getReferer(String comicId, String chapterId, int num) {
    return WWW_HOST + "/comic/" + comicId + "/" + chapterId + "_p" + num + ".html";
  }

  /**
   * 一般的referer格式，http://www.ikanman.com/comic/4732/235794.html
   *
   * @param comicId   漫画id
   * @param chapterId 章节id
   * @return referer
   */
  String getReferer(String comicId, String chapterId) {
    return WWW_HOST + "/comic/" + comicId + "/" + chapterId + ".html";
  }

  /**
   * 处理文件名，去后缀等
   *
   * @return 解析后的文件名
   */
  String resolveFilename(String remoteFilename) {
    String resolvedFilename;
    // 处理后缀.webp的图片
    if (remoteFilename.endsWith(".webp")) {
      resolvedFilename = remoteFilename.substring(0, remoteFilename.lastIndexOf("."));
    } else {
      resolvedFilename = remoteFilename;
    }
    return resolvedFilename;
  }
}
