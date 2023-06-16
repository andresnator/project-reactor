package com.project.reactor.utils;

import static java.lang.Thread.sleep;

public class CommonUtils {

  public static void delay(int millis) {
    try {
      sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
