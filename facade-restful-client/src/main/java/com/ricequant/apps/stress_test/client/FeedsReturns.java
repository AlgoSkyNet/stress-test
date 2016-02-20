package com.ricequant.apps.stress_test.client;

import io.vertx.core.json.JsonObject;

/**
 * This struct wraps all feeds returned by a single request
 *
 * @author chenfeng
 */
public class FeedsReturns {

  private final GetFeedsRequest iReq;

  private FeedsReturn iPortfoliosReturn;

  private FeedsReturn iBenchmarkPortfoliosReturn;

  private FeedsReturn iRisksReturn;

  private FeedsReturn iPositionsReturn;

  private FeedsReturn iTradesReturn;

  private FeedsReturn iOrdersReturn;

  private FeedsReturn iPortfolioGridsReturn;

  private FeedsReturn iRiskGridsReturn;

  private FeedsReturn iLogsReturn;

  private FeedsReturn iPlotsReturn;

  private JsonObject iStatus;

  private FeedsReturns(GetFeedsRequest req) {
    iReq = req;
  }

  /**
   * Creates FeedsReturns object associated with a GetFeedsRequest
   *
   * @param req
   *         the request to get feeds
   *
   * @return an empty instance of FeedReturns
   */
  public static FeedsReturns create(GetFeedsRequest req) {
    return new FeedsReturns(req);
  }

  public long runID() {
    return iReq.runID();
  }

  /**
   * Set the strategy running status
   *
   * @param status
   *         the status JsonObject
   *
   * @return this object
   */
  public FeedsReturns status(JsonObject status) {
    iStatus = status;
    return this;
  }

  public FeedsReturns portfolios(FeedsReturn feeds) {
    iPortfoliosReturn = feeds;
    return this;
  }

  public FeedsReturns benchmarkPortfolios(FeedsReturn feeds) {
    iBenchmarkPortfoliosReturn = feeds;
    return this;
  }

  public FeedsReturns risks(FeedsReturn feeds) {
    iRisksReturn = feeds;
    return this;
  }

  public FeedsReturns positions(FeedsReturn feeds) {
    iPositionsReturn = feeds;
    return this;
  }

  public FeedsReturns trades(FeedsReturn feeds) {
    iTradesReturn = feeds;
    return this;
  }

  public FeedsReturns orders(FeedsReturn feeds) {
    iOrdersReturn = feeds;
    return this;
  }

  public FeedsReturns portfolioGrids(FeedsReturn feeds) {
    iPortfolioGridsReturn = feeds;
    return this;
  }

  public FeedsReturns riskGrids(FeedsReturn feeds) {
    iRiskGridsReturn = feeds;
    return this;
  }

  public FeedsReturns logs(FeedsReturn feeds) {
    iLogsReturn = feeds;
    return this;
  }

  public FeedsReturns plots(FeedsReturn feeds) {
    iPlotsReturn = feeds;
    return this;
  }

  public JsonObject status() {
    return iStatus;
  }

  public FeedsReturn portfolios() {
    return iPortfoliosReturn;
  }

  public FeedsReturn benchmarkPortfolios() {
    return iBenchmarkPortfoliosReturn;
  }

  public FeedsReturn risks() {
    return iRisksReturn;
  }

  public FeedsReturn positions() {
    return iPositionsReturn;
  }

  public FeedsReturn trades() {
    return iTradesReturn;
  }

  /**
   * Orders have not been supported yet, it always returns null
   *
   * @return null
   */
  public FeedsReturn orders() {
    return iOrdersReturn;
  }

  public FeedsReturn portfolioGrids() {
    return iPortfolioGridsReturn;
  }

  public FeedsReturn riskGrids() {
    return iRiskGridsReturn;
  }

  public FeedsReturn logs() {
    return iLogsReturn;
  }

  public FeedsReturn plots() {
    return iPlotsReturn;
  }

  @Override
  public String toString() {
    return "{status:" + iStatus + ", portfolios:" + iPortfoliosReturn + "," + "benchmark-portfolios:"
            + iBenchmarkPortfoliosReturn + ",positions:" + iPositionsReturn + "," + "risks:" + iRisksReturn + ",trades:"
            + iTradesReturn + ",orders:" + iOrdersReturn + "," + "portfolio-grids:" + iPortfolioGridsReturn
            + ",riskGrids:" + iRiskGridsReturn + ",logs:" + iLogsReturn + "," + "plots:" + iPlotsReturn + "}";
  }

  /**
   * Figure out what will be the following request based on the previous returned feeds
   *
   * @return the GetFeedsRequest object which could be sent directly to the server
   */
  public GetFeedsRequest nextRequest() {
    GetFeedsRequest req = GetFeedsRequest.create(iReq.runID());
    if (iReq.portfolios() != null)
      req.portfolios(
              iPortfoliosReturn != null && iPortfoliosReturn.nextIndex() >= 0 ? iPortfoliosReturn.nextIndex() : 0,
              iReq.portfolios().length());
    if (iReq.benchmarkPortfolios() != null)
      req.benchmarkPortfolios(iBenchmarkPortfoliosReturn != null && iBenchmarkPortfoliosReturn.nextIndex() >= 0
              ? iBenchmarkPortfoliosReturn.nextIndex() : 0, iReq.benchmarkPortfolios().length());
    if (iReq.positions() != null)
      req.positions(iPositionsReturn != null && iPositionsReturn.nextIndex() >= 0 ? iPositionsReturn.nextIndex() : 0,
              iReq.positions().length());
    if (iReq.risks() != null)
      req.risks(iRisksReturn != null && iRisksReturn.nextIndex() >= 0 ? iRisksReturn.nextIndex() : 0,
              iReq.risks().length());
    if (iReq.trades() != null)
      req.trades(iTradesReturn != null && iTradesReturn.nextIndex() >= 0 ? iTradesReturn.nextIndex() : 0,
              iReq.trades().length());
    if (iReq.orders() != null)
      req.orders(iOrdersReturn != null && iOrdersReturn.nextIndex() >= 0 ? iOrdersReturn.nextIndex() : 0,
              iReq.orders().length());
    if (iReq.logs() != null)
      req.logs(iLogsReturn != null && iLogsReturn.nextIndex() >= 0 ? iLogsReturn.nextIndex() : 0, iReq.logs().length());
    if (iReq.plots() != null)
      req.plots(iPlotsReturn != null && iPlotsReturn.nextIndex() >= 0 ? iPlotsReturn.nextIndex() : 0,
              iReq.plots().length());
    return req;
  }

  /**
   * When NormalExit status has been detected by the client, to avoid synchronization problem, this request pulls
   * everything if has not been pulled already
   *
   * @param includeGrids
   *         if grids are needed to be pulled, usually true when "run", false for "build" actions
   *
   * @return the GetFeedsRequest object which could be sent directly to the server
   */
  public GetFeedsRequest lastRequest(boolean includeGrids) {
    GetFeedsRequest req = GetFeedsRequest.create(iReq.runID());
    if (iReq.portfolios() != null)
      req.portfolios(
              iPortfoliosReturn != null && iPortfoliosReturn.nextIndex() >= 0 ? iPortfoliosReturn.nextIndex() : 0,
              Integer.MAX_VALUE);
    if (iReq.benchmarkPortfolios() != null)
      req.benchmarkPortfolios(iBenchmarkPortfoliosReturn != null && iBenchmarkPortfoliosReturn.nextIndex() >= 0
              ? iBenchmarkPortfoliosReturn.nextIndex() : 0, Integer.MAX_VALUE);
    if (iReq.positions() != null)
      req.positions(iPositionsReturn != null && iPositionsReturn.nextIndex() >= 0 ? iPositionsReturn.nextIndex() : 0,
              Integer.MAX_VALUE);
    if (iReq.risks() != null)
      req.risks(iRisksReturn != null && iRisksReturn.nextIndex() >= 0 ? iRisksReturn.nextIndex() : 0,
              Integer.MAX_VALUE);
    if (iReq.trades() != null)
      req.trades(iTradesReturn != null && iTradesReturn.nextIndex() >= 0 ? iTradesReturn.nextIndex() : 0,
              Integer.MAX_VALUE);
    if (iReq.orders() != null)
      req.orders(iOrdersReturn != null && iOrdersReturn.nextIndex() >= 0 ? iOrdersReturn.nextIndex() : 0,
              Integer.MAX_VALUE);
    if (iReq.logs() != null)
      req.logs(iLogsReturn != null && iLogsReturn.nextIndex() >= 0 ? iLogsReturn.nextIndex() : 0, Integer.MAX_VALUE);
    if (iReq.plots() != null)
      req.plots(iPlotsReturn != null && iPlotsReturn.nextIndex() >= 0 ? iPlotsReturn.nextIndex() : 0,
              Integer.MAX_VALUE);

    if (includeGrids) {
      req.riskGrids(0, Integer.MAX_VALUE);
      req.portfolioGrids(0, Integer.MAX_VALUE);
    }

    return req;
  }

  public GetFeedsRequest request() {
    return iReq;
  }
}
