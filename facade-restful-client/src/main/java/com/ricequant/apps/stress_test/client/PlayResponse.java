package com.ricequant.apps.stress_test.client;

import io.vertx.core.json.JsonObject;

/**
 * The response object parsed from the JsonObject for the play command
 *
 * @author chenfeng
 */
public class PlayResponse {

  private final String iReason;

  private final long iRunID;

  PlayResponse(JsonObject ret) {
    Integer status = ret.getInteger("status");

    if (status == null || status != 0) {
      String reason = ret.getString("reason");
      if (reason == null)
        iReason = "Fail reason not set, please contact Ricequant";
      else
        iReason = reason;

      iRunID = -1;
      return;
    }

    Long runID = ret.getLong("run-id");
    if (runID == null) {
      iReason = "RunID not set, please contact Ricequant";
      iRunID = -1;
    }
    else {
      iReason = null;
      iRunID = runID;
    }
  }

  /**
   * See if the play command succeeds
   *
   * @return true if successful, false otherwise
   */
  public boolean isSuccess() {
    return iReason == null;
  }

  /**
   * The reason of failures
   *
   * @return reason of failure in plain text, or null if the command succeeds
   */
  public String reason() {
    return iReason;
  }

  /**
   * The runID returned by server associated with the strategy being played
   *
   * @return the runID
   */
  public long runID() {
    return iRunID;
  }
}
