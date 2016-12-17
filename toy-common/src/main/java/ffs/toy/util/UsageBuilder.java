package ffs.toy.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用方法构造器
 * <pre>
 * 注意cmd()和后面的arg()方法组成一个完整的命令
 * String usageText = new UsageBuilder()
 *   .cmd().arg().arg()
 *   .cmd().arg()
 *   .build()
 * </pre>
 */
public class UsageBuilder {
  private List<Command> commands = new ArrayList<>();

  /**
   * 指定一个命令
   *
   * @param name 命名名
   * @param desc 描述
   */
  public UsageBuilder cmd(String name, String desc) {
    this.commands.add(new Command(name, desc));
    return this;
  }

  /**
   * 指定参数
   *
   * @param argName 参数名
   * @param argDesc 参数描述
   */
  public UsageBuilder arg(String argName, String argDesc) {
    return arg(argName, argDesc, true);
  }

  /**
   * 为命令指定参数，必须在cmd()方法后使用，并且和最接近的cmd()方法组合
   *
   * @param argName  参数名
   * @param argDesc  参数描述
   * @param required 是否为空
   */
  public UsageBuilder arg(String argName, String argDesc, boolean required) {
    if (this.commands.isEmpty()) {
      throw new RuntimeException("没有设置命令");
    }
    this.commands.get(this.commands.size() - 1).args.add(new Arg(argName, required, argDesc));
    return this;
  }

  /**
   * 构造文本
   *
   * @return String 使用方法文本
   */
  public String build() {
    String text = "使用方法:\n";
    for (Command cmd : commands) {
      List<Arg> args = cmd.args;
      String argNameText = "";
      String argDescText = "";
      for (Arg arg : args) {
        argNameText += " " + (arg.required ? "<" + arg.name + ">" : "[" + arg.name + "]");
        argDescText += "  " + arg.name + "：" + arg.desc + (arg.required ? "" : "（可选）") + "\n";
      }
      text += cmd.desc + "\n用法：" + cmd.name + argNameText + "\n选项：\n" + argDescText + "\n";
    }
    return text;
  }

  private static class Command {
    private final String name;
    private final String desc;
    private final List<Arg> args = new ArrayList<>();

    Command(String name, String desc) {
      this.name = name;
      this.desc = desc;
    }
  }

  private class Arg {
    private final String name;
    private final boolean required;
    private final String desc;

    Arg(String name, boolean required, String desc) {
      this.name = name;
      this.required = required;
      this.desc = desc;
    }
  }
}
