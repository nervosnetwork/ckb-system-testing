package org.nervos.ckb.framework.system;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

/**
 * manage the cita chain .It can be create/start/stop/delete the chain.
 */
public class CKBSystem {

  /**
   * If open debug , the framework output debug information.
   */
  private boolean debug = false;
  /**
   * It is mean framework run in local host or remote host.
   */
  private boolean runInLocal = true;
  private String ckbDockerImageTagName =
      System.getenv("CKB_DOCKER_IMAGE_TAG_NAME") == null ? "latest"
          : System.getenv("CKB_DOCKER_IMAGE_TAG_NAME");
  private String remoteIp = "";
  private String username = "";
  private String password = "";
  private int remotePort = 22;
  private String minerArgs = "0x7f52f0fccdd1d11391c441adfb174f87bca612b0";

  /**
   * is docker
   */
  private boolean runInDocker = true;
  private String dockerName = "run-ckb-" + UUID.randomUUID();
  /**
   * The ip address list is run host for chain node.
   */
  private int rpcPort = 8114;
  private ArrayList<String> stdout = new ArrayList<String>();
  private ArrayList<String> stderror = new ArrayList<String>();

  public CKBSystem() {
  }

  public String getDockerName() {
    return dockerName;
  }

  public int getRpcPort() {
    return rpcPort;
  }

  public void setRpcPort(int rpcPort) {
    this.rpcPort = rpcPort;
  }

  public void setRunInDocker(boolean docker) {
    this.runInDocker = docker;
  }

  public String getCkbDockerImageTagName() {
    return ckbDockerImageTagName;
  }


  public void setRunInLocal(boolean local) {
    this.runInLocal = local;
  }

  public void setRemoteInfo(String ip, String user, String passwd) {
    setRemoteInfo(ip, user, passwd, this.remotePort);
  }

  public ECode setRemoteInfo(String ip, String user, String passwd, int port) {
    if (ip.isEmpty() || user.isEmpty() || passwd.isEmpty()) {
      this.runInLocal = true;
      return ECode.remoteInfoErr;
    }
    this.remoteIp = ip;
    this.username = user;
    this.password = passwd;
    this.remotePort = port;
    this.runInLocal = false;
    return ECode.success;
  }

  public void enableDebug() {
    this.debug = true;
  }

  public void disableDebug() {
    this.debug = false;
  }

  private void debugOutput(String message) {
    if (this.debug) {
      System.out.println("[DEBUG] CKBSystem: " + message);
    }
  }

  public ECode initEnv() {
    return initEnv(this.rpcPort, this.dockerName, this.ckbDockerImageTagName);
  }

  // just stop ckb miner
  public void stopCkbMiner() {
    //stop this miner
    String stopMiner = "ps -ef |grep 'ckb miner'|grep -v 'grep'|cut -c 9-15 |xargs kill -9";
    runCommandWithDocker(stopMiner);
  }

  // just stop ckb run
  public void stopCkbRun() {
    //stop this miner
    String stopMiner = "ps -ef |grep 'ckb run'|grep -v 'grep'|cut -c 9-15 |xargs kill -9";
    runCommandWithDocker(stopMiner);
  }

  /**
   * init and start Node and Miner
   */
  public void startNodeAndMiner() {
    ckbInitRun();
    startCKBMiner();
  }

  /**
   * init CKB config
   */
  public void initCKB() {
    String ckbInit = "ckb init";
    runCommandWithDocker(ckbInit, "-d -it");
  }

  /**
   * just start ckb run
   */
  public void startCKBRun() {
    String ckbRun = "ckb run";
    runCommandWithDocker(ckbRun, "-d -it");
  }

  /**
   * just start ckb miner
   */
  public void startCKBMiner() {
    String ckbMiner = "ckb miner";
    runCommandWithDocker(ckbMiner, "-d -it");
  }

  /**
   * init and update config then start ckb run
   */
  public void ckbInitRun() {
    initCKB();
    updateBlockAssemblerConfig();
    startCKBRun();
  }

  /**
   * update to un-comment the block_assembler
   *
   * the minerArgs can be get from 'ckb cli secp256k1-lock <pubkey>'
   */
  public void updateBlockAssemblerConfig() {
    String updateCmd =
        "sed -i 's/# code_hash =/code_hash =/;s/# \\[block_assembler]/\\[block_assembler]/;/# args =/aargs = \\[\\\""
            + minerArgs + "\\\"]' ckb.toml";
    runCommandWithDocker(updateCmd);
  }

  //    init runtime environment
  public ECode initEnv(int rpcPort, String dockerName, String ckbDockerImageTagName) {
    ECode result = ECode.success;
//        setCKBDockerImageTagName();
    if (this.runInDocker) {
      debugOutput("initEnv: start a docker container");
      result = runScript(
          "../scripts/ckb-entrypoint.sh start " + dockerName + " " + ckbDockerImageTagName + " "
              + rpcPort);
    } else if (this.runInLocal) {
      // do nothing
      debugOutput("initEnv: runInLocal");
    }
    return result;
  }

  public ECode cleanEnv() {
    return cleanEnv(this.rpcPort, this.dockerName, this.ckbDockerImageTagName);
  }

  // clean runtime environment
  public ECode cleanEnv(int rpcPort, String dockerName, String ckbDockerImageTagName) {
    ECode result = ECode.success;
//        setCKBDockerImageTagName();
    if (this.runInDocker) {
      debugOutput("cleanEnv: stop a docker container");
      result = runScript(
          "../scripts/ckb-entrypoint.sh stop " + dockerName + " " + ckbDockerImageTagName + " "
              + rpcPort);
      debugOutput("cleanEnv: stop a docker container");
    } else if (this.runInLocal) {
      // do nothing
      debugOutput("cleanEnv: runInLocal");
    }
    return result;
  }

  public ECode init() {
    return init(this.dockerName, this.ckbDockerImageTagName, this.rpcPort);
  }

  public ECode init(String dockerName, String ckbDockerImageTagName, int rpcPort) {
    if (this.runInDocker) {
      debugOutput("init:start a idle docker container");
      return runScript(
          "../scripts/ckb-entrypoint.sh init " + dockerName + " " + ckbDockerImageTagName + " "
              + rpcPort);
    } else if (this.runInLocal) {
      debugOutput("init:runInLocal");
    }
    return ECode.success;
  }
    /*public ECode startChainInDocker() {
	setCitaDockerImageTagName();
        ECode result = runScript("../scripts/cita-entrypoint.sh start " + this.defaultRpcPort + " " + this.dockerName + " " + this.citaDockerImageTagName);
        return result;
    }*/
    
    /*public void setCitaDockerImageTagName() {
	this.citaDockerImageTagName = System.getenv("CITA_DOCKER_IMAGE_TAG_NAME") == null ? "" : System.getenv("CITA_DOCKER_IMAGE_TAG_NAME");
        debugOutput("cita docker image tag name: " + citaDockerImageTagName);
    }*/

  /**
   * Executor command. The single or multiple command can be executed.
   *
   * @param cmd single or multiple command.
   */

  public ECode runCommand(String cmd) {
    debugOutput("exec command : " + cmd);
    ECode ret;
    if (this.runInLocal) {
      String[] command = new String[]{"/bin/sh", "-c", cmd};
      ret = runLocalCommand(command);
    } else {
      ret = runRemoteCommand(cmd);
    }
    return ret;
  }

  // run cmd with a docker container
  public ECode runCommandWithDocker(String cmd, String... option) {
    debugOutput("runCommandWithDocker: " + cmd);
    ECode ret;
    String realOption = "";
    if (option != null && option.length > 0) {
      realOption = String.join(" ", option);
    }
    String command =
        "docker exec " + realOption + " " + this.dockerName + " /bin/bash -c \"" + cmd + "\"";

    ret = runCommand(command);
    return ret;
  }

  // run a script saved in scripts/ dir using full file path
  public ECode runScript(String cmd) {
    debugOutput("runScript: " + cmd);

    String projectRootPath = this.getClass().getResource("/")
        .getPath(); // # cita_quality/systemTest/target/test-classes/
    debugOutput("projectRootPath: " + projectRootPath);
    projectRootPath = projectRootPath.replace("target/test-classes/", "");

    if (cmd.startsWith("../")) {
      cmd = cmd.replaceFirst("../", "");
    }
    String fullcmd = projectRootPath + cmd;

    ECode ret;
    String[] command = new String[]{"/bin/sh", "-c", fullcmd};
    ret = runLocalCommand(command);
    return ret;
  }

  private ECode runLocalCommand(String[] command) {
    ECode ret = ECode.success;
    debugOutput("runLocalCommand exec: " + String.join(" ", command));

    try {
      Runtime run = Runtime.getRuntime();
      Process p = run.exec(command);
      BufferedInputStream in = new BufferedInputStream(p.getInputStream());
      BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
      String lineStr;
      stdout.clear();
      while ((lineStr = inBr.readLine()) != null) {
        stdout.add(lineStr);
        debugOutput(lineStr);
      }
      if (p.waitFor() != 0) {
        if (p.exitValue() != 0) {//p.exitValue()==0表示正常结束，>0：非正常结束
          debugOutput("runLocalCommand error with exit status: " + p.exitValue());

          // collect stderr and print error message
          InputStream cmdStdErr = p.getErrorStream();
          BufferedReader br = new BufferedReader(new InputStreamReader(cmdStdErr));
          String errMsg = "";
          stderror.clear();
          while ((lineStr = br.readLine()) != null) {
            stderror.add(lineStr);
            errMsg += lineStr;
          }
          cmdStdErr.close();
          debugOutput("runLocalCommand error message: " + errMsg);

          ret = ECode.runLocalCmdError;
        }
      }
      inBr.close();
      in.close();
    } catch (Exception e) {
      e.printStackTrace();
      ret = ECode.runLocalCmdException;
    }
    debugOutput("runLocalCommand finish");

    return ret;
  }

  private ECode runRemoteCommand(String command) {
    ECode returnCode = ECode.success;
    debugOutput("remote executor command : " + command);
    try {

      JSch jsch = new JSch();
      CKBUserInfo userInfo = new CKBUserInfo();
      Session session = jsch.getSession(this.username, this.remoteIp, this.remotePort);
      session.setPassword(this.password);
      session.setUserInfo(userInfo);
      session.setTimeout(20000);
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();

      Channel channel = session.openChannel("exec");
      ChannelExec channelExec = (ChannelExec) channel;

      channelExec.setCommand(command);

      channelExec.setInputStream(null);
      BufferedReader input = new BufferedReader(new InputStreamReader
          (channelExec.getInputStream()));

      channelExec.connect();

      String line;
      stdout.clear();
      while ((line = input.readLine()) != null) {
        stdout.add(line);
        debugOutput(line);
      }
      input.close();

      if (channelExec.isClosed()) {
        int ret = channelExec.getExitStatus();
        if (ret != 0) {
          debugOutput("framework exec command error : " + ret);
          returnCode = ECode.runRemoteCmdErr;
        }
      }

      channelExec.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
      return ECode.runRemoteCmdException;
    }
    debugOutput("remote executor command finish");
    return returnCode;
  }

  public ArrayList<String> getStandardOutput() {
    return stdout;
  }

  public ArrayList<String> getStandardError() {
    return stderror;
  }

  public String getStdErrorString() {
    return String.join(System.lineSeparator(), stderror);
  }

  public String getStdoutString() {
    return String.join(System.lineSeparator(), stdout);
  }

  /**
   * the framework have any error is return user. the error is only one.
   */
  public enum ECode {
    success,
    remoteInfoErr,
    chainIsExists,
    chainIsNotExists,
    noNodes,
    createFailure,
    nodeIsExists,
    appendFailure,
    runLocalCmdError,
    /**
     * the command may be error
     */
    runLocalCmdException,
    runRemoteCmdErr,
    runRemoteCmdException;
  }

  public class CKBUserInfo implements UserInfo {

    @Override
    public String getPassphrase() {
      // TODO Auto-generated method stub
      System.out.println("--getPassphrase - begin--");
      return null;
    }

    @Override
    public String getPassword() {
      // TODO Auto-generated method stub
      System.out.println("--getPassword - begin--");
      return null;
    }

    @Override
    public boolean promptPassphrase(String arg0) {
      // TODO Auto-generated method stub
      System.out.println("--promptPassphrase - begin--");
      if (debug) {
        System.out.println("--promptPassphrase - begin--");
        System.out.println(arg0);
        System.out.println("--end--");
      }
      return false;
    }

    @Override
    public boolean promptPassword(String arg0) {
      // TODO Auto-generated method stub
      System.out.println("--promptPassword - begin--");
      if (debug) {
        System.out.println("--promptPassword - begin--");
        System.out.println(arg0);
        System.out.println("--end--");
      }
      return false;
    }

    @Override
    public boolean promptYesNo(String arg0) {
      // TODO Auto-generated method stub'
      System.out.println("--promptYesNo - begin--");
      if (debug) {

      }
      return true;
    }

    @Override
    public void showMessage(String arg0) {
      // TODO Auto-generated method stub
      System.out.println("--showMessage - begin--");
      if (debug) {
        System.out.println("--showMessage - begin--");
        System.out.println(arg0);
        System.out.println("--end--");
      }
    }
  }
}

