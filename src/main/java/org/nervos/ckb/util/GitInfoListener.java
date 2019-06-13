package org.nervos.ckb.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.Reporter;

public class GitInfoListener implements ISuiteListener {

  @Override
  public void onStart(ISuite suite) {
    getGitInfo("git rev-parse --short HEAD", "The current git commit id is: ");
  }

  @Override
  public void onFinish(ISuite suite) {
    Reporter.log("About to end executing Suite " + suite.getName(), true);
  }

  /**
   * Report the git commit info
   */
  public void getGitInfo(String cmd, String logTitle) {
    Runtime runtime = Runtime.getRuntime();
    Process process = null;
    try {
      process = runtime.exec(cmd);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())
    )) {
      String result = reader.readLine();
      Reporter.log(logTitle + result, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
