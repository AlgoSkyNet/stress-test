package com.ricequant.apps.stress_test.client;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.function.Consumer;

/**
 * This is the client to the facade Restful API. It only serves the purpose of stress testing.
 *
 * @author chenfeng
 */
public class FacadeClient {

  private final HttpClient iHttpClient;

  private final URL iUrl;

  private final Logger iLogger = LoggerFactory.getLogger(getClass());

  private final String iUsername;

  private final String iPassword;

  private final int iPort;

  private FacadeClient(URL url, String username, String password) {
    iHttpClient = Vertx.vertx().createHttpClient(new HttpClientOptions().setMaxPoolSize(300));
    iPort = url.getPort() < 0 ? url.getDefaultPort() : url.getPort();
    iUrl = url;
    iUsername = username;
    iPassword = password;
  }

  /**
   * Creates an instance of FacadeClient who communicates with the url specified.
   *
   * @param url
   *         the url to connect to, like new URL("http://test.ricequant.com/backend-restful")
   * @param username
   *         the user name distributed by Ricequant
   * @param password
   *         the password associated with the user name, also distributed by Ricequant
   *
   * @return the instance of the client
   */
  public static FacadeClient create(URL url, String username, String password) {
    return new FacadeClient(url, username, password);
  }

  /**
   * Start a strategy instance and receive the results
   *
   * @param playParams
   *         parameters needed to run a strategy
   * @param resultAcceptor
   *         callback to receive the responses.
   */
  public void play(PlayParams playParams, Consumer<PlayResponse> resultAcceptor) {
    JsonObject message = authenticatingMessage();
    message.put("command", "play");
    playParams.appendToJsonObject(message);
    iHttpClient.post(iPort, iUrl.getHost(), iUrl.getPath(), rsp -> rsp.bodyHandler(buffer -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("<- " + buffer);

      JsonObject ret = buffer.toJsonObject();
      resultAcceptor.accept(new PlayResponse(ret));
    })).exceptionHandler(e -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("Error sending play command", e);
      play(playParams, resultAcceptor);
    }).end(message.toString(), "utf-8");
  }

  /**
   * Get feeds of a specific run-id in certain ranges asynchronously
   *
   * @param request
   *         the request object
   * @param feedsAcceptor
   *         callback to receive the feeds
   */
  public void getFeeds(GetFeedsRequest request, Consumer<FeedsReturns> feedsAcceptor) {
    JsonObject message = authenticatingMessage();
    message.put("command", "pull");
    message.put("feeds-params", request.toJsonObject());

    iHttpClient.post(iPort, iUrl.getHost(), iUrl.getPath(), rsp -> rsp.bodyHandler(buffer -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("<- " + buffer);

      JsonObject ret = buffer.toJsonObject();
      Integer statusCode = ret.getInteger("status");
      if (statusCode == null || statusCode != 0) {
        iLogger.error("Error getting results from server: " + buffer);
        return;
      }

      JsonObject results = ret.getJsonObject("results");
      FeedsReturns feeds = FeedsReturns.create(request);
      if (results.containsKey("status"))
        feeds.status(results.getJsonObject("status"));

      if (results.containsKey("portfolios"))
        feeds.portfolios(FeedsReturn.fromSlice(results.getJsonObject("portfolios"), "Portfolios"));

      if (results.containsKey("benchmark-portfolios"))
        feeds.benchmarkPortfolios(FeedsReturn.fromSlice(results.getJsonObject("benchmark-portfolios"), "Portfolios"));

      if (results.containsKey("positions"))
        feeds.positions(FeedsReturn.fromSlice(results.getJsonObject("positions"), "Positions"));

      if (results.containsKey("risks"))
        feeds.risks(FeedsReturn.fromSlice(results.getJsonObject("risks"), "PortfolioRisks"));

      if (results.containsKey("trades"))
        feeds.trades(FeedsReturn.fromSlice(results.getJsonObject("trades"), "Trades"));

      if (results.containsKey("orders"))
        feeds.orders(FeedsReturn.fromSlice(results.getJsonObject("orders"), "Orders"));

      if (results.containsKey("plots"))
        feeds.plots(FeedsReturn.fromSlice(results.getJsonObject("plots"), "Plots"));

      if (results.containsKey("logs"))
        feeds.logs(FeedsReturn.fromSlice(results.getJsonObject("logs"), "Logs"));

      if (results.containsKey("portfolio-grids"))
        feeds.portfolioGrids(FeedsReturn.fromFeeds(results.getJsonArray("portfolio-grids")));

      if (results.containsKey("risk-grids"))
        feeds.riskGrids(FeedsReturn.fromFeeds(results.getJsonArray("risk-grids")));

      feedsAcceptor.accept(feeds);
    })).exceptionHandler(e -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("Error getting feeds", e);
      getFeeds(request, feedsAcceptor);
    }).end(message.toString(), "utf-8");
  }

  /**
   * Stop the strategy associated with this runID
   *
   * @param runID
   *         the runID returned by the play method
   */
  public void stopStrategy(long runID) {
    JsonObject message = authenticatingMessage();
    message.put("command", "stop");
    message.put("run-id", runID);

    String messageString = message.toString();
    iHttpClient.post(iPort, iUrl.getHost(), iUrl.getPath()).exceptionHandler(e -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("Error sending stop strategy command", e);
      stopStrategy(runID);
    }).end(messageString, "utf-8");
    if (iLogger.isDebugEnabled())
      iLogger.debug("-> " + messageString);
  }

  /**
   * Kill all running strategies to form a clean environment
   *
   * @param onStopResponse
   *         callback when all strategies stopped
   */
  public void stopAllStrategies(Runnable onStopResponse) {
    JsonObject message = authenticatingMessage();
    message.put("command", "stop-all");

    String messageString = message.toString();
    iHttpClient.post(iPort, iUrl.getHost(), iUrl.getPath(), rsp -> rsp.bodyHandler(buffer -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("<- " + buffer);

      JsonObject ret = buffer.toJsonObject();
      Integer statusCode = ret.getInteger("status");
      if (statusCode == null || statusCode != 0) {
        iLogger.error("Getting error status from server: " + buffer);
        iLogger.info("Retry stop all strategies...");
        stopAllStrategies(onStopResponse);
        return;
      }

      onStopResponse.run();
    })).exceptionHandler(e -> {
      if (iLogger.isDebugEnabled())
        iLogger.debug("Error sending stop all strategies command", e);
      stopAllStrategies(onStopResponse);
    }).end(messageString, "utf-8");
    if (iLogger.isDebugEnabled())
      iLogger.debug("-> " + messageString);
  }

  /**
   * Shutdown this client
   */
  public void shutdown() {
    new Thread(() -> {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
      iHttpClient.close();
    }).start();
  }

  private JsonObject authenticatingMessage() {
    JsonObject message = new JsonObject();
    message.put("username", iUsername);
    message.put("password", iPassword);
    return message;
  }

}
