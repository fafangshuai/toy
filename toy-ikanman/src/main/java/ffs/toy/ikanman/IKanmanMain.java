package ffs.toy.ikanman;

import com.alibaba.fastjson.JSON;
import ffs.toy.util.UsageBuilder;
import ffs.toy.util.Util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.String.format;

/**
 * 从ikanman漫画网站下载漫画
 */
public class IKanmanMain {
  private static IKanmanService iKanmanService = new IKanmanService();
  private static ExecutorService pool = Executors.newFixedThreadPool(10);
  private static DecimalFormat df = new DecimalFormat("000");

  public static void main(String[] args) {
    try {
      long s = System.currentTimeMillis();
      IKanmanService.initScriptEngine();
      if (args.length < 2) {
        usage();
      }
      String action = args[0];
      switch (action) {
        case "printCatalog":
          String comicUrl = args[1];
          printCatalog(comicUrl);
          break;
        case "printCatalogForH":
          if (args.length < 3) {
            usage();
          }
          printCatalogForH(args[1], args[2]);
          break;
        case "download":
          if (args.length < 3) {
            usage();
          }
          downloadChaptersFromUrl(args[1], args[2], determineCustomName(args));
          break;
        case "downloadFromJson":
          if (args.length < 3) {
            usage();
          }
          downloadChaptersFromJson(args[1], args[2], determineCustomName(args));
          break;
        default:
          usage();
          break;
      }
      shutdownAndAwaitTermination();
      System.out.printf("Spend time: %s ms\n", System.currentTimeMillis() - s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 解析自定义漫画名称
   *
   * @param args 入参数组
   * @return 自定义漫画名
   */
  private static String determineCustomName(String[] args) {
    if (args.length > 3) {
      return args[3];
    }
    return null;
  }

  private static void usage() {
    String text = new UsageBuilder()
        .cmd("printCatalog", "打印漫画目录").arg("comicUrl", "漫画Url")
        .cmd("printCatalogForH", "特殊漫画目录打印").arg("bid", "漫画Id").arg("startCid", "起始章节Id")
        .cmd("download", "从章节Url下载").arg("inputFile", "章节Url文件（utf-8）").arg("parentDir", "保存漫画的目录").arg("customName", "自定义漫画名称", false)
        .cmd("downloadFromJson", "从章节Json下载").arg("inputFile", "章节Json文件（utf-8）").arg("parentDir", "保存漫画的目录").arg("customName", "自定义漫画名称", false)
        .build();
    System.err.println(text);
    System.exit(1);
  }

  /**
   * 输出目录信息
   *
   * @param comicUrl 漫画url
   */
  private static void printCatalog(String comicUrl) {
    try {
      List<String[]> list = iKanmanService.resolveCatalog(comicUrl);
      // 输出到控制台和文件
      for (String[] item : list) {
        System.out.println(format("%s\t%s", item[0], item[1]));
      }
    } catch (Exception e) {
      throw new RuntimeException("打印漫画目录信息出错：" + comicUrl, e);
    }
  }

  /**
   * 输出目录信息
   *
   * @param bid      漫画id
   * @param startCid 起始章节id
   */
  private static void printCatalogForH(String bid, String startCid) {
    try {
      List<String> list = iKanmanService.resolveCatalogFromChapterNav2(Integer.parseInt(bid), Integer.parseInt(startCid));
      resolveChapters(list);
    } catch (Exception e) {
      throw new RuntimeException("打印漫画目录信息出错：bid=" + bid + ", startCid=" + startCid, e);
    }
  }

  /**
   * 根据输入的url下载
   *
   * @param inputFile  输入文件，内容为要解析的章节url，每行一条
   * @param parentDir  父目录
   * @param customName 自定义漫画名称
   */
  private static void downloadChaptersFromUrl(String inputFile, String parentDir, String customName) {
    List<String> chapterUrlList = Util.getLines(inputFile);
    List<String> jsonList = resolveChapters(chapterUrlList);
    downloadChapters(parentDir, customName, jsonList);
  }

  /**
   * 根据输入的章节json下载
   *
   * @param inputFile  章节信息json
   * @param parentDir  父目录
   * @param customName 自定义漫画名称
   */
  private static void downloadChaptersFromJson(String inputFile, String parentDir, String customName) {
    List<String> jsonList = Util.getLines(inputFile);
    downloadChapters(parentDir, customName, jsonList);
  }

  /**
   * 批量下载章节信息
   *
   * @param parentDir  父目录
   * @param customName 自定义漫画名称
   * @param jsonList   json列表
   */
  private static void downloadChapters(String parentDir, String customName, List<String> jsonList) {
    for (String json : jsonList) {
      downloadChapter(json, parentDir, customName);
    }
  }

  /**
   * 批量解析章节信息，获取json数据
   *
   * @param chapterUrlList 章节url列表
   * @return json列表
   */
  private static List<String> resolveChapters(List<String> chapterUrlList) {// 提交任务
    List<Future<String>> futureList = new ArrayList<>();
    for (String chapterUrl : chapterUrlList) {
      futureList.add(pool.submit(new ResolveChapterTask(chapterUrl)));
    }
    // 等待获取结果
    List<String> jsonList = new ArrayList<>();
    for (Future<String> future : futureList) {
      try {
        jsonList.add(future.get());
      } catch (InterruptedException | ExecutionException e) {
        System.err.println("解析章节JSON失败：" + future.toString());
      }
    }
    return jsonList;
  }

  /**
   * 解析章节信息任务
   */
  private static class ResolveChapterTask implements Callable<String> {
    private String chapterUrl;

    ResolveChapterTask(String chapterUrl) {
      this.chapterUrl = chapterUrl;
    }

    @Override
    public String call() throws Exception {
      String json = null;
      try {
        json = iKanmanService.resolveChapter2(chapterUrl);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return json;
    }
  }

  /**
   * 解析单个章节信息，并下载。自动在父目录下创建：漫画名/章节/xxx.jpg
   *
   * @param cInfoJson  章节信息json
   * @param parentDir  父目录
   * @param customName 自定义漫画名称
   */
  private static void downloadChapter(String cInfoJson, String parentDir, String customName) {
    CInfo cInfo = JSON.parseObject(cInfoJson, CInfo.class);
    String comicName = customName != null ? customName : cInfo.getBname();
    String chapterName = cInfo.getCname();
    String chapterDirName = comicName + " " + chapterName;
    File destDir = Util.mkdirs(parentDir, comicName, chapterDirName);
    if (destDir == null) {
      System.err.printf("创建目录失败：%s\n", parentDir + File.separator + comicName + File.separator + chapterDirName);
      return;
    }
    String referer = iKanmanService.getReferer(cInfo.getBid(), cInfo.getCid());
    String pathPrefix = IKanmanService.IMAGE_HOST + cInfo.getPath();
    int num = 1;
    for (String remoteFileName : cInfo.getFiles()) {
      String filename = iKanmanService.resolveFilename(remoteFileName);
      String remoteUrl = pathPrefix + filename;
      String localPath = new File(destDir, df.format(num++) + Util.getExt(filename, true)).getAbsolutePath();
      pool.execute(new DownloadChapterTask(remoteUrl, localPath, referer));
    }
  }

  /**
   * 下载文件任务
   */
  private static class DownloadChapterTask implements Runnable {
    private String remoteUrl;
    private String localPath;
    private String referer;

    DownloadChapterTask(String remoteUrl, String localPath, String referer) {
      this.remoteUrl = remoteUrl;
      this.localPath = localPath;
      this.referer = referer;
    }

    @Override
    public void run() {
      iKanmanService.download(remoteUrl, localPath, referer);
    }
  }


  /**
   * 停止线程并等待所有任务执行完毕
   */
  private static void shutdownAndAwaitTermination() {
    pool.shutdown();
    try {
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
