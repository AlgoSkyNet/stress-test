package com.ricequant.apps.stress_test.client;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The class to manage lifecycle of a single strategy run
 *
 * @author chenfeng
 */
public class StrategyPlayer {

  private final PlayParams iParams;

  private final FacadeClient iClient;

  private final PlayStats iPlayStats = new PlayStats();

  private final ScheduledExecutorService iExecutor = Executors.newSingleThreadScheduledExecutor();

  private final Logger iLogger = LoggerFactory.getLogger(getClass());

  private Function<Boolean, Consumer<FeedsReturns>> iFeedsConsumer;

  private StrategyPlayer(PlayParams params, FacadeClient client) {
    iParams = params;
    iClient = client;

    iFeedsConsumer = stopNextFetching -> feeds -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("Received feeds for run-id <" + feeds.runID() + ">: " + feeds.toString());

      JsonObject statusJson = feeds.status();
      String lifeCycleStatus = statusJson.getString("LifeCycleStatus");
      iPlayStats.status = lifeCycleStatus;

      if (stopNextFetching) {
        notifyResult();
        return;
      }

      if (ExecutionStatusHelper.isSuccess(lifeCycleStatus)) {
        // normal exit, fetch the remaining feeds and grids if any, and stop the next fetching
        GetFeedsRequest req = feeds.lastRequest(true);
        iClient.getFeeds(req, iFeedsConsumer.apply(true));
        return;
      }
      else if (ExecutionStatusHelper.isError(lifeCycleStatus) || ExecutionStatusHelper.isCanceled(lifeCycleStatus)) {
        iLogger.info("Execution of run-id <" + feeds.runID() + "> interrupted due to: " + lifeCycleStatus);
        notifyResult();
        return;
      }

      GetFeedsRequest req = feeds.nextRequest();

      if (req.equals(feeds.request()))
        iExecutor.schedule(() -> iClient.getFeeds(req, iFeedsConsumer.apply(false)), 5, TimeUnit.SECONDS);
      else {
        iExecutor.schedule(() -> iClient.getFeeds(req, iFeedsConsumer.apply(false)), 1, TimeUnit.SECONDS);
      }
    };
  }

  /**
   * Creates a player instance
   *
   * @param params
   *         the parameters needed to start a strategy
   * @param client
   *         the FacadeClient object
   *
   * @return the StrategyPlayer instance
   */
  public static StrategyPlayer create(PlayParams params, FacadeClient client) {
    return new StrategyPlayer(params, client);
  }

  private boolean checkTime() {
    if (iLogger.isDebugEnabled())
      iLogger.debug("Still waiting: " + iPlayStats.runID);

    if (iPlayStats.waitMillis == 0)
      return true;

    long time = System.currentTimeMillis();
    if (time - iPlayStats.startMillis > iPlayStats.waitMillis) {
      iLogger.warn("Strategy run-id=" + iPlayStats.runID + " runs over hard deadline, canceling...");
      iClient.stopStrategy(iPlayStats.runID);
      return false;
    }

    return true;
  }

  private void notifyResult() {
    if (iPlayStats.resultAcceptor != null) {
      iPlayStats.resultAcceptor.accept(iPlayStats.status, System.currentTimeMillis() - iPlayStats.startMillis);
    }
    iExecutor.shutdownNow();
  }

  private void scheduleTime() {
    if (checkTime())
      iExecutor.schedule(this::scheduleTime, 5000, TimeUnit.MILLISECONDS);
  }

  /**
   * Start running the strategy within given waiting time
   *
   * @param waitMillis
   *         milliseconds to wait before kill the strategy
   * @param resultAcceptor
   *         callback to accept the finishing status of the strategy. The first parameter has three possible values:
   *         "NormalExit", "AbnormalExit" and "CancelExit". It is recommended to use {@link ExecutionStatusHelper} to
   *         identify the status; the second parameter is the milliseconds taken to run the strategy
   */
  public void play(int waitMillis, BiConsumer<String, Long> resultAcceptor) {
    iPlayStats.waitMillis = waitMillis;
    iPlayStats.startMillis = System.currentTimeMillis();
    iPlayStats.resultAcceptor = resultAcceptor;

    scheduleTime();

    iClient.play(iParams, ret -> {
      iPlayStats.runID = ret.runID();
      if (!ret.isSuccess()) {
        iLogger.info("Error running strategy for run-id <" + ret.runID() + ">: " + ret.reason());
        iPlayStats.status = "AbnormalExit";
        notifyResult();
        return;
      }
      else {
        iLogger.info("Successfully started strategy with run-id=" + ret.runID());
      }

      GetFeedsRequest req = GetFeedsRequest.create(ret.runID());
      req.portfolios(0, 100);
      req.benchmarkPortfolios(0, 100);
      req.positions(0, 100);
      req.risks(0, 100);
      req.trades(0, 100);

      iClient.getFeeds(req, iFeedsConsumer.apply(false));
    });
  }

  private static class PlayStats {

    public String status;

    public BiConsumer<String, Long> resultAcceptor;

    long runID;

    int waitMillis;

    long startMillis;
  }
}
