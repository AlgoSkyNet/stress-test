package com.ricequant.apps.stress_test.client;

import io.vertx.core.json.JsonObject;

/**
 * Parameters needed to run the strategy
 *
 * @author chenfeng
 */
public class PlayParams {

  private int iStartDate = 20140101;

  private int iEndDate = 20150101;

  private String iCode = "";

  private String iOwner = "";

  private double iCash = 100000;

  private String iRunType = "backtest";

  private String iTitle = "";

  private String iTimeUnit = "d";

  private String iLanguage = "python";

  private String iBenchmarkName;

  private String iPortfolioName;

  public PlayParams portfolioName(String portfolioName) {
    iPortfolioName = portfolioName;
    return this;
  }

  public PlayParams benchmarkName(String benchmarkName) {
    iBenchmarkName = benchmarkName;
    return this;
  }

  public PlayParams startDate(int date) {
    iStartDate = date;
    return this;
  }

  public PlayParams endDate(int date) {
    iEndDate = date;
    return this;
  }

  public PlayParams code(String code) {
    iCode = code;
    return this;
  }

  public PlayParams owner(String owner) {
    iOwner = owner;
    return this;
  }

  public PlayParams initialCash(double cash) {
    iCash = cash;
    return this;
  }

  public PlayParams runType(String runType) {
    iRunType = runType;
    return this;
  }

  public PlayParams title(String title) {
    iTitle = title;
    return this;
  }

  public PlayParams timeUnit(String timeUnit) {
    iTimeUnit = timeUnit;
    return this;
  }

  public PlayParams language(String language) {
    iLanguage = language;
    return this;
  }

  void appendToJsonObject(JsonObject obj) {
    obj.put("code", iCode);
    obj.put("owner", iOwner);
    obj.put("title", iTitle);
    obj.put("time-unit", iTimeUnit);
    obj.put("language", iLanguage);
    obj.put("start-date", iStartDate);
    obj.put("end-date", iEndDate);
    obj.put("initial-cash", iCash);
    obj.put("run-type", iRunType);
    obj.put("portfolio-name", iPortfolioName);
    obj.put("benchmark-name", iBenchmarkName);
  }

  public String toString() {
    JsonObject json = new JsonObject();
    appendToJsonObject(json);
    return json.toString();
  }
}
