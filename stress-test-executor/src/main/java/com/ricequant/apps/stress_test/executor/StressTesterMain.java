package com.ricequant.apps.stress_test.executor;

import com.ricequant.apps.stress_test.client.FacadeClient;
import com.ricequant.apps.stress_test.executor.config.StressTesterConfig;
import com.ricequant.apps.stress_test.executor.config.TestScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author chenfeng
 */
public class StressTesterMain {

  private static final Logger cLogger = LoggerFactory.getLogger(StressTesterMain.class);

  public static void main(String[] args) throws Exception {
    StressTesterConfig config = StressTesterConfig.init(args);
    if (config == null)
      System.exit(0);

    FacadeClient client = FacadeClient.create(config.url(), config.username(), config.password());

    CountDownLatch cleanEnvLatch = new CountDownLatch(1);
    client.stopAllStrategies(cleanEnvLatch::countDown);

    cLogger.info("Waiting for 30 seconds to cleanup environment...");
    cleanEnvLatch.await(30, TimeUnit.SECONDS);

    if (cleanEnvLatch.getCount() == 0)
      cLogger.info("Environment cleaned, start testing...");
    else {
      System.err.println("Unable to clean environment, please contact Ricequant");
      cLogger.error("Unable to clean environment, please contact Ricequant");
      System.exit(-1);
    }

    Queue<TestScenario> scenarios = new LinkedList<>();
    scenarios.addAll(config.listScenarios());

    runNextScenario(scenarios, client);
  }

  private static void runNextScenario(Queue<TestScenario> scenarios, FacadeClient client) {
    TestScenario scenario = scenarios.poll();
    if (scenario == null) {
      client.shutdown();
      System.exit(0);
      return;
    }
    ScenarioRunner runner = ScenarioRunner.create(scenario, client, () -> runNextScenario(scenarios, client));
    runner.run();
  }
}
