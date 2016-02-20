package com.ricequant.apps.stress_test.client;

/**
 * Helper class to determine strategy run lifecycle status
 *
 * @author chenfeng
 */
public class ExecutionStatusHelper {

  public static boolean isSuccess(String status) {
    return "NormalExit".equals(status);
  }

  public static boolean isCanceled(String status) {
    return "CancelExit".equals(status);
  }

  public static boolean isError(String status) {
    return "AbnormalExit".equals(status);
  }

  public static boolean isExit(String status) {
    return status != null && status.contains("Exit");
  }
}
