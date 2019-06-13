package org.nervos.ckb.util;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

// print test class name and method name for each test
// similar to use <test verbose="3"> but more clean output message
public class TestRunnerListener extends TestListenerAdapter {

  private String prefix = "[" + getClass().getSimpleName() + "]";

  // on <test> Start
  @Override
  public void onStart(ITestContext tc) {
    int testsCount = tc.getAllTestMethods().length;
    int threadsCount = tc.getCurrentXmlTest().getThreadCount();
    println(String
        .format("\n%s Running %d tests in '%s' using %d threads", prefix, testsCount, tc.getName(),
            threadsCount));
  }

  // on <test> Finish
  @Override
  public void onFinish(ITestContext tc) {
    int failuresCount = tc.getFailedTests().size();
    int skippedCount = tc.getSkippedTests().size();
    println(String
        .format("\n%s Run all tests in '%s' finished( Failures: %d, Skipped: %d )\n", prefix,
            tc.getName(), failuresCount, skippedCount));
  }

  // on @Test method Start
  @Override
  public void onTestStart(ITestResult tr) {
    println(String.format("\n%s Running test '%s # %s()'", prefix, tr.getTestClass().getName(),
        tr.getMethod().getMethodName()));
  }

  @Override
  public void onTestSuccess(ITestResult tr) {
    finish(tr);
  }

  // on @Test method Finish
  @Override
  public void onTestFailure(ITestResult tr) {
    finish(tr);
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    finish(tr);
  }

  // PRIVATE METHODS

  private void finish(ITestResult tr) {
    long timeSpent = tr.getEndMillis() - tr.getStartMillis();
    String status = "";
    switch (tr.getStatus()) {
      case ITestResult.SUCCESS:
        status = "PASSED";
        break;
      case ITestResult.FAILURE:
        status = "FAILED";
        break;
      case ITestResult.SKIP:
        status = "SKIPPED";
        break;
    }
    println(String.format("%s ===== %s (%d ms) =====", prefix, status, timeSpent));
  }

  private void println(String string) {
    System.out.println(string);
  }
}
