package org.nervos.ckb.util.items;

import com.alibaba.fastjson.annotation.JSONField;
import java.util.List;

public class Inputs {

  @JSONField(name = "previous_output")
  private OutPoint previousOutput;
  private List<String> args;

  public String getSince() {
    return since;
  }

  public void setSince(String since) {
    this.since = since;
  }

  private String since;

  public OutPoint getPreviousOutput() {
    return previousOutput;
  }

  public void setPreviousOutput(OutPoint previousOutput) {
    this.previousOutput = previousOutput;
  }

  public List<String> getArgs() {
    return args;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }

  @Override
  public String toString() {
    return "Inputs{" +
        "previousOutput=" + previousOutput +
        ", args=" + args +
        ", since='" + since + '\'' +
        '}';
  }

  public Inputs(OutPoint previousOutput, List<String> args, String since) {
    this.previousOutput = previousOutput;
    this.args = args;
    this.since = since;
  }
}
