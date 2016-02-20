package com.ricequant.apps.stress_test.client;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.ObjectUtils;

/**
 * The request to pull feeds for a specific run
 *
 * @author chenfeng
 */
public class GetFeedsRequest {

  private final long iRunID;

  private IndexParams iPortfoliosParams;

  private IndexParams iRisksParams;

  private IndexParams iTradesParams;

  private IndexParams iOrdersParams;

  private IndexParams iPositionsParams;

  private IndexParams iPortfolioGridsParams;

  private IndexParams iRiskGridsParams;

  private IndexParams iLogsParams;

  private IndexParams iPlotsParams;

  private IndexParams iBenchmarkPortfoliosParams;

  private GetFeedsRequest(long runID) {
    iRunID = runID;
  }

  /**
   * Create a request associated with a runID
   *
   * @param runID
   *         the runID returned by the play command
   *
   * @return the GetFeedsRequest
   */
  public static GetFeedsRequest create(long runID) {
    return new GetFeedsRequest(runID);
  }

  IndexParams portfolios() {
    return iPortfoliosParams;
  }

  IndexParams benchmarkPortfolios() {
    return iBenchmarkPortfoliosParams;
  }

  IndexParams risks() {
    return iRisksParams;
  }

  IndexParams trades() {
    return iTradesParams;
  }

  IndexParams orders() {
    return iOrdersParams;
  }

  IndexParams positions() {
    return iPositionsParams;
  }

  IndexParams portfolioGrids() {
    return iPortfolioGridsParams;
  }

  IndexParams riskGrids() {
    return iRiskGridsParams;
  }

  IndexParams logs() {
    return iLogsParams;
  }

  IndexParams plots() {
    return iPlotsParams;
  }

  public GetFeedsRequest portfolios(int startIndex, int length) {
    iPortfoliosParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest benchmarkPortfolios(int startIndex, int length) {
    iBenchmarkPortfoliosParams = new IndexParams("b", startIndex, length);
    return this;
  }

  public GetFeedsRequest risks(int startIndex, int length) {
    iRisksParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest trades(int startIndex, int length) {
    iTradesParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest orders(int startIndex, int length) {
    iOrdersParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest positions(int startIndex, int length) {
    iPositionsParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest portfolioGrids(int startIndex, int length) {
    iPortfolioGridsParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest riskGrids(int startIndex, int length) {
    iRiskGridsParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest logs(int startIndex, int length) {
    iLogsParams = new IndexParams("p", startIndex, length);
    return this;
  }

  public GetFeedsRequest plots(int startIndex, int length) {
    iPlotsParams = new IndexParams("p", startIndex, length);
    return this;
  }

  JsonObject toJsonObject() {
    JsonObject ret = new JsonObject();
    ret.put("run-id", iRunID);

    if (iPortfoliosParams != null)
      ret.put("portfolios", iPortfoliosParams.toJsonObject());

    if (iBenchmarkPortfoliosParams != null)
      ret.put("benchmark-portfolios", iBenchmarkPortfoliosParams.toJsonObject());

    if (iPositionsParams != null)
      ret.put("positions", iPositionsParams.toJsonObject());

    if (iTradesParams != null)
      ret.put("trades", iTradesParams.toJsonObject());

    if (iOrdersParams != null)
      ret.put("orders", iOrdersParams.toJsonObject());

    if (iRisksParams != null)
      ret.put("risks", iRisksParams.toJsonObject());

    if (iLogsParams != null)
      ret.put("logs", iLogsParams.toJsonObject());

    if (iPlotsParams != null)
      ret.put("plots", iPlotsParams.toJsonObject());

    if (iPortfolioGridsParams != null)
      ret.put("portfolio-grids", iPortfolioGridsParams.toJsonObject());

    if (iRiskGridsParams != null)
      ret.put("risk-grids", iRiskGridsParams.toJsonObject());

    return ret;
  }

  public String toString() {
    return "{\"GetFeedsRequest\":" + toJsonObject() + "}";
  }

  @Override
  public boolean equals(Object that) {
    if (super.equals(that))
      return true;

    if (that == null)
      return false;

    if (!(that instanceof GetFeedsRequest))
      return false;

    GetFeedsRequest o = (GetFeedsRequest) that;
    return iRunID == o.iRunID && ObjectUtils.equals(iPortfoliosParams, o.iPortfoliosParams) && ObjectUtils
            .equals(iBenchmarkPortfoliosParams, o.iBenchmarkPortfoliosParams) &&
            ObjectUtils.equals(iPortfolioGridsParams, o.iPortfolioGridsParams) && ObjectUtils
            .equals(iPositionsParams, o.iPositionsParams) && ObjectUtils.equals(iTradesParams, o.iTradesParams)
            && ObjectUtils.equals(iOrdersParams, o.iOrdersParams) && ObjectUtils.equals(iRisksParams, o.iRisksParams)
            && ObjectUtils.equals(iLogsParams, o.iLogsParams) && ObjectUtils.equals(iPlotsParams, o.iPlotsParams)
            && ObjectUtils.equals(iPortfolioGridsParams, o.iPortfolioGridsParams) && ObjectUtils
            .equals(iRiskGridsParams, o.iRiskGridsParams);
  }

  public long runID() {
    return iRunID;
  }

  static class IndexParams {

    private final String portfolioName;

    private final int startIndex;

    private final int length;

    IndexParams(String portfolioName, int startIndex, int length) {
      this.portfolioName = portfolioName;
      this.startIndex = startIndex;
      this.length = length;
    }

    int length() {
      return length;
    }

    @Override
    public boolean equals(Object that) {
      if (super.equals(that))
        return true;

      if (that == null)
        return false;

      IndexParams o = (IndexParams) that;
      return ObjectUtils.equals(portfolioName, o.portfolioName) && startIndex == o.startIndex &&
              length == o.length;
    }

    public JsonObject toJsonObject() {
      JsonObject ret = new JsonObject();
      if (portfolioName != null)
        ret.put("portfolio-name", portfolioName);
      ret.put("start-index", startIndex);
      ret.put("length", length);
      ret.put("precision", 1);
      return ret;
    }
  }
}
