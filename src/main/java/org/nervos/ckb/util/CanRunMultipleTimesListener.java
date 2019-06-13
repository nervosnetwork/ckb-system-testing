package org.nervos.ckb.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Use to update CanRunMutipleTimes annotation fields
 */
public class CanRunMultipleTimesListener implements IAnnotationTransformer {


  @Override
  public void transform(ITestAnnotation iTestAnnotation, Class aClass, Constructor constructor,
      Method method) {
    CanRunMultipleTimes canRunMultipleTimes;
    if (method == null || (canRunMultipleTimes=method.getAnnotation(CanRunMultipleTimes.class)) == null) {
      return;
    }
//    //TODO: add code to change @Test annotation incocationCount and threadPoolSize
//    int counter = Integer.parseInt(System.getProperty("iteration.count", "3"));
//    iTestAnnotation.setInvocationCount(counter);

    String invocationCountKey = canRunMultipleTimes.invocationCountKey();
    String threadPoolSizeKey = canRunMultipleTimes.threadPoolSizeKey();

    ReadConfig conf = new ReadConfig();
    String YML_TITLE= "LoadConfig";
    int invocationCount = (int) conf.getYMLValue(YML_TITLE, invocationCountKey);
    iTestAnnotation.setInvocationCount(invocationCount);
    int threadPoolSize = (int) conf.getYMLValue(YML_TITLE, threadPoolSizeKey);
    iTestAnnotation.setThreadPoolSize(threadPoolSize);

  }

}