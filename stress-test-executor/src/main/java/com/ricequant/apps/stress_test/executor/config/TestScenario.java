package com.ricequant.apps.stress_test.executor.config;

import com.ricequant.generated_config.apps.stress_test.ScenarioType;
import com.ricequant.generated_config.apps.stress_test.ScenariosType;
import com.ricequant.apps.stress_test.client.PlayParams;
import io.vertx.core.json.JsonObject;

/**
 * The configuration of the test scenario
 *
 * @author chenfeng
 */
public class TestScenario {

  private final double iRefineGrowFactor;

  private final double iRefineShrinkFactor;

  private final double iOvertimeToleranceMultiplier;

  private final int iMaxRefineRuns;

  private final double iSuccessTolerancePercentage;

  private final PlayParams iPlayParams;

  private final int iTimeoutToKillMillis;

  private final int iNumInitialSpeedTestRuns;

  private final int iInitialParallels;

  private final double iExpandGrowFactor;

  private final int iTheoreticalUpperBound;

  private final String iName;

  private TestScenario(PlayParams params, ScenariosType scenariosXmlConfig, ScenarioType scenarioXmlConfig) {
    iPlayParams = params;

    iTimeoutToKillMillis = scenarioXmlConfig.getTimeoutMillis();

    iExpandGrowFactor = scenariosXmlConfig.getExpandGrowFactor();
    iRefineGrowFactor = scenariosXmlConfig.getRefineGrowFactor();
    iRefineShrinkFactor = scenariosXmlConfig.getRefineShrinkFactor();
    iOvertimeToleranceMultiplier = scenariosXmlConfig.getOvertimeToleranceMultiplier();
    iInitialParallels = scenariosXmlConfig.getInitialParallelRuns();
    iMaxRefineRuns = scenariosXmlConfig.getMaxRefineRuns();
    iSuccessTolerancePercentage = scenariosXmlConfig.getSuccessTolerancePercentage();
    iNumInitialSpeedTestRuns = scenarioXmlConfig.getNumInitialSpeedTestRuns();
    iTheoreticalUpperBound = scenariosXmlConfig.getTheoreticalUpperBound();
    iName = scenarioXmlConfig.getTitle();
  }

  public PlayParams playParams() {
    return iPlayParams;
  }

  /**
   * Milliseconds that one strategy can run at most
   *
   * @return milliseconds
   */
  public int timeoutToKillMillis() {
    return iTimeoutToKillMillis;
  }

  /**
   * Number of passes run to decide the load-free speed
   *
   * @return the number of passes to run
   */
  public int numInitialSpeedTestRuns() {
    return iNumInitialSpeedTestRuns;
  }

  /**
   * Number of parallel strategies to start with, bounded by TheoreticalUpperBound
   *
   * @return number
   */
  public int initialParallels() {
    return iInitialParallels < iTheoreticalUpperBound ? iInitialParallels : iTheoreticalUpperBound;
  }

  /**
   * A multiplier applied to the load-free running time. If the actual running time during stress test is smaller than
   * this, it is considered pass, otherwise considered overtime
   *
   * @return multiplier [1, +infinity)
   */
  public double overtimeToleranceMultiplier() {
    return iOvertimeToleranceMultiplier;
  }

  /**
   * Number of refine runs
   *
   * @return number
   */
  public int maxRefineRuns() {
    return iMaxRefineRuns;
  }

  /**
   * In expansion phase, how fast should the number of concurrent running strategies to grow. The minimum increment will
   * be 2 if factor * current-number-concurrent-strategies less than 2
   *
   * @return the factor
   */
  public double expandGrowFactor() {
    return iExpandGrowFactor;
  }

  /**
   * In refining phase, how fast should the number of concurrent running strategies to grow. It's usually smaller than
   * the expanding factor. The minimum increment will be 2 if factor * current-number-concurrent-strategies less than 2
   *
   * @return the factor
   */
  public double refineGrowFactor() {
    return iRefineGrowFactor;
  }

  /**
   * In refining phase, how fast should the number of concurrent running strategies to reduce. The minimum reduction
   * will be 1 if factor * current-number-concurrent-strategies is 0
   *
   * @return the factor
   */
  public double refineShrinkFactor() {
    return iRefineShrinkFactor;
  }

  /**
   * The percentage of successful run strategies to pass the pass. E.g. if in a refine-grow pass, 10 strategies ran, 8
   * success, 2 overtime. If the percentage is 0.8, the pass is considered successful, otherwise failed.
   *
   * @return the percentage
   */
  public double successTolerancePercentage() {
    return iSuccessTolerancePercentage;
  }

  /**
   * The number of strategies can be executed in parallel theoretically, bounded by the total memory installed in the
   * server. The stress test process should not start parallel strategies more than this number.
   *
   * @return the number of strategies can be executed in parallel
   */
  public int theoreticalUpperBound() {
    return iTheoreticalUpperBound;
  }

  /**
   * Name of the scenario who takes the same value of title of the strategy
   *
   * @return name of the scenario
   */
  public String name() {
    return iName;
  }

  @Override
  public String toString() {
    JsonObject json = new JsonObject();
    json.put("params", iPlayParams.toString())

            .put("timeout-to-kill", iTimeoutToKillMillis)

            .put("speed-test-trials", iNumInitialSpeedTestRuns)

            .put("initial-parallels", iInitialParallels)

            .put("overtime-tolerance-multiplier", iOvertimeToleranceMultiplier)

            .put("expand-grow-factor", iExpandGrowFactor)

            .put("refine-grow-factor", iRefineGrowFactor)

            .put("refine-shrink-factor", iRefineShrinkFactor)

            .put("max-refine-runs", iMaxRefineRuns)

            .put("success-tolerance-percentage", iSuccessTolerancePercentage);

    return json.toString();
  }

  /**
   * Create the TestScenario instance
   *
   * @param params
   *         the params used to start a strategy run
   * @param scenariosXmlConfig
   *         the scenarios config parsed by generated jaxb object
   * @param scenarioXmlConfig
   *         the scenario config parsed by generated jaxb object
   *
   * @return the TestScenario instance
   */
  public static TestScenario create(PlayParams params, ScenariosType scenariosXmlConfig,
          ScenarioType scenarioXmlConfig) {
    return new TestScenario(params, scenariosXmlConfig, scenarioXmlConfig);
  }

}
