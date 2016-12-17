package ffs.toy.xunlei;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.NameFilter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class CrackSuperSpeedService {

  private static final String UPDATE_SQL = "UPDATE AccelerateTaskMap218813268_superspeed_1_1 SET UserData = ? WHERE LocalTaskId = ? AND " +
      "LocalSubFileIndex = ?";
  private static final String QUERY_SQL = "SELECT * FROM AccelerateTaskMap218813268_superspeed_1_1";

  public void crack(String xunleiHome) {
    try {
      Class.forName("org.sqlite.JDBC");
      String url = String.format("jdbc:sqlite:%s%s", xunleiHome, File.separator + "Profiles" + File.separator + "TaskDb.dat");
      Connection conn = DriverManager.getConnection(url);
      List<SuperSpeedInfo> illegalInfo = getIllegalInfo(conn);
      for (SuperSpeedInfo info : illegalInfo) {
        updateIllegalInfo(conn, info);
      }
      conn.close();
      System.out.printf("破解完成，共破解%d个文件\n", illegalInfo.size());
    } catch (Exception e) {
      throw new RuntimeException("破解出错", e);
    }
  }

  private void updateIllegalInfo(Connection conn, SuperSpeedInfo info) throws SQLException, UnsupportedEncodingException {
    PreparedStatement ps = conn.prepareStatement(UPDATE_SQL);
    XLSuperSpeedState userData = info.getUserData();
    userData.setResult(0);
    String json = JSON.toJSONString(userData, new NameFilter() {
      @Override
      public String process(Object object, String name, Object value) {
        char[] nameChs = name.toCharArray();
        nameChs[0] = Character.toUpperCase(nameChs[0]);
        return new String(nameChs);
      }
    });
    ps.setBytes(1, json.getBytes("utf-8"));
    ps.setLong(2, info.getLocalTaskId());
    ps.setInt(3, info.getLocalSubFileIndex());
    ps.executeUpdate();
    ps.close();
  }

  private List<SuperSpeedInfo> getIllegalInfo(Connection conn) throws SQLException, IOException {
    List<SuperSpeedInfo> toUpdateList = new ArrayList<>();
    PreparedStatement ps = conn.prepareStatement(QUERY_SQL);
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
      XLSuperSpeedState userData = JSON.parseObject(rs.getBytes("UserData"), XLSuperSpeedState.class);
      if (userData.getResult() == 508) {
        toUpdateList.add(new SuperSpeedInfo(rs.getLong("LocalTaskId"), rs.getInt("LocalSubFileIndex"), userData));
      }
    }
    rs.close();
    ps.close();
    return toUpdateList;
  }

  private static class SuperSpeedInfo {
    private long localTaskId;
    private int localSubFileIndex;
    private XLSuperSpeedState userData;

    SuperSpeedInfo(long localTaskId, int localSubFileIndex, XLSuperSpeedState userData) {
      this.localTaskId = localTaskId;
      this.localSubFileIndex = localSubFileIndex;
      this.userData = userData;
    }

    public long getLocalTaskId() {
      return localTaskId;
    }

    public void setLocalTaskId(long localTaskId) {
      this.localTaskId = localTaskId;
    }

    public int getLocalSubFileIndex() {
      return localSubFileIndex;
    }

    public void setLocalSubFileIndex(int localSubFileIndex) {
      this.localSubFileIndex = localSubFileIndex;
    }

    public XLSuperSpeedState getUserData() {
      return userData;
    }

    public void setUserData(XLSuperSpeedState userData) {
      this.userData = userData;
    }
  }

  private static class XLSuperSpeedState {
    private String commitGcid;
    private String message;
    private int result;
    private int subId;

    public String getCommitGcid() {
      return commitGcid;
    }

    public void setCommitGcid(String commitGcid) {
      this.commitGcid = commitGcid;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public int getResult() {
      return result;
    }

    public void setResult(int result) {
      this.result = result;
    }

    public int getSubId() {
      return subId;
    }

    public void setSubId(int subId) {
      this.subId = subId;
    }
  }
}
