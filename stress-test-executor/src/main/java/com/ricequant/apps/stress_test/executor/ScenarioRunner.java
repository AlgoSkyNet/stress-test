package com.ricequant.apps.stress_test.executor;

import com.ricequant.apps.stress_test.client.FacadeClient;
import com.ricequant.apps.stress_test.client.StrategyPlayer;
import com.ricequant.apps.stress_test.executor.config.TestScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * @author chenfeng
 */
public class ScenarioRunner {

  private final TestScenario iScenario;

  private final FacadeClient iClient;

  private final Logger iLogger = LoggerFactory.getLogger(getClass());

  private final Runnable iOnFinished;

  private ScenarioRunner(TestScenario scenario, FacadeClient client, Runnable onFinished) {
    iScenario = scenario;
    iClient = client;
    iOnFinished = onFinished;
  }

  /**
   * Create an instance of the ScenarioRunner
   *
   * @param scenario
   *         the scenario definition
   * @param client
   *         the FacadeClient instance to communicate with server
   * @param onFinished
   *         the callback which is called when the scenario is finished
   *
   * @return the instance
   */
  public static ScenarioRunner create(TestScenario scenario, FacadeClient client, Runnable onFinished) {
    return new ScenarioRunner(scenario, client, onFinished);
  }

  /**
   * Start running
   */
  public void run() {
    determineSingleStrategyAverageSpeed();
  }

  private void determineSingleStrategyAverageSpeed() {
    iLogger.info("Determining single strategy average execution...");

    StrategyPlayer player = StrategyPlayer.create(iScenario.playParams(), iClient);
    player.play(iScenario.timeoutToKillMillis(),
            SingleStrategySpeedDecider.getResultAcceptor(iScenario.numInitialSpeedTestRuns(), resultAcceptor -> {
              StrategyPlayer newPlayer = StrategyPlayer.create(iScenario.playParams(), iClient);
              newPlayer.play(iScenario.timeoutToKillMillis(), resultAcceptor);
            }, averageTime -> {
              if (averageTime < 0) {
                iLogger.error("Failed to determine the initial speed of the strategy, stop scenario: " + iScenario);
                iOnFinished.run();
              }
              else {
                iLogger.info("Single strategy average execution time is: " + averageTime + " milliseconds.");
                doStressTest(averageTime, iScenario.initialParallels(), 0, StressTestStatus.initialStatus());
              }
            }));
  }

  private void doStressTest(double unstressedExecutionTime, int numExecutors, int runCount,
          StressTestStatus bestSuccessfulStatus) {
    iLogger.info("Starting stress test pass with: numExecutors=" + numExecutors);

    if (runCount >= iScenario.maxRefineRuns()) {
      iLogger.info("maxRefineRuns reached, stopping...");

      iLogger.info("Scenario <" + iScenario.toString() + ">" + System.lineSeparator() + " Finished: " + System
              .lineSeparator() + "================================" + System.lineSeparator()
              + "\tNumber of strategies ran: " + bestSuccessfulStatus.numExecutors + System.lineSeparator()
              + "\taverage time taken for each run: " + bestSuccessfulStatus.averageRuntime + " milliseconds");

      iOnFinished.run();
      return;
    }

    BiConsumer<String, Long> resultCollector = MultiStrategyResultCollector
            .getResultAcceptor((long) (unstressedExecutionTime * iScenario.overtimeToleranceMultiplier()), numExecutors,
                    iScenario.successTolerancePercentage(), (success, time) -> {
                      iLogger.info("Pass finished, number of executors=" + numExecutors);
                      if (success) {
                        int newSize =
                                (int) (numExecutors * (bestSuccessfulStatus.hasFailedRuns ? iScenario.refineGrowFactor()
                                        : iScenario.expandGrowFactor()));

                        if (newSize <= numExecutors + 1)
                          newSize += 2;

                        if (newSize > iScenario.theoreticalUpperBound())
                          newSize = iScenario.theoreticalUpperBound();

                        iLogger.info("Pass success, grow number of executors from " + numExecutors + " to " + newSize
                                + ". Average time: " + time);
                        // this is an incremental run, does not count
                        doStressTest(unstressedExecutionTime, newSize,
                                bestSuccessfulStatus.hasFailedRuns ? runCount + 1 : runCount, StressTestStatus
                                        .newStatus(bestSuccessfulStatus, numExecutors, time,
                                                bestSuccessfulStatus.hasFailedRuns));
                      }
                      else {
                        int newSize = (int) (numExecutors * iScenario.refineShrinkFactor());
                        if (newSize == numExecutors)
                          newSize -= 1;

                        iLogger.info("Pass failed, shrink number of executors from " + numExecutors + " to " + newSize
                                + " for the next pass if there is one. Average time: " + time);

                        doStressTest(unstressedExecutionTime, newSize, runCount + 1,
                                StressTestStatus.newFailedStatus(bestSuccessfulStatus));
                      }
                    });
    for (int i = 0; i < numExecutors; i++)
      StrategyPlayer.create(iScenario.playParams(), iClient).play(iScenario.timeoutToKillMillis(), resultCollector);
  }

  private static class StressTestStatus {

    int numExecutors;

    double averageRuntime;

    boolean hasFailedRuns = false;

    private StressTestStatus(int numExecutors, double averageRuntime, boolean hasFailedRuns) {
      this.numExecutors = numExecutors;
      this.averageRuntime = averageRuntime;
      this.hasFailedRuns = hasFailedRuns;
    }

    public static StressTestStatus initialStatus() {
      return new StressTestStatus(0, 0, false);
    }

    public static StressTestStatus newStatus(StressTestStatus bestSuccessfulStatus, int numExecutors,
            double averageRuntime, boolean hasFailedRuns) {
      if (bestSuccessfulStatus.numExecutors < numExecutors)
        return new StressTestStatus(numExecutors, averageRuntime, hasFailedRuns);
      else if (bestSuccessfulStatus.numExecutors == numExecutors) {
        if (bestSuccessfulStatus.averageRuntime > averageRuntime)
          return new StressTestStatus(numExecutors, averageRuntime, hasFailedRuns);
      }

      return new StressTestStatus(bestSuccessfulStatus.numExecutors, bestSuccessfulStatus.averageRuntime,
              hasFailedRuns);
    }

    public static StressTestStatus newFailedStatus(StressTestStatus status) {
      return new StressTestStatus(status.numExecutors, status.averageRuntime, true);
    }
  }
}
