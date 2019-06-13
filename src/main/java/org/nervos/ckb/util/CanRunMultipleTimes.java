package org.nervos.ckb.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on the @Test method to indicate the number of times the test method was called and the size of the thread pool
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CanRunMultipleTimes {
  //the number of times a method is called
  public String invocationCountKey() default "";
  //the size of the threadPool
  public String threadPoolSizeKey() default "";
}