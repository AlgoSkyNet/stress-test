package com.ricequant.apps.stress_test.executor.config;

import com.ricequant.generated_config.apps.stress_test.BarTypeEnum;
import com.ricequant.generated_config.apps.stress_test.ScenarioType;
import com.ricequant.generated_config.apps.stress_test.ScenariosType;
import com.ricequant.generated_config.apps.stress_test.StressTest;
import com.ricequant.apps.stress_test.client.PlayParams;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenfeng
 */
public class StressTesterConfig {

  private static final String cSchemaFile = "stress-test.xsd";

  private final StressTest iXml;

  private final String iConfigPath;

  StressTesterConfig(String path) {
    iConfigPath = path;
    iXml = new JaxbHelper<>(StressTest.class).loadXml(false, path, cSchemaFile);
  }

  private static StressTesterConfig load(String path) {
    return new StressTesterConfig(path);
  }

  private static void printUsage() {
    System.out.println("Usage: -c path/to/config/file.xml");
  }

  public static StressTesterConfig init(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("-h")) {
        printUsage();
        return null;
      }

      if (arg.equals("-c")) {
        if (i + 1 >= args.length) {
          printUsage();
          return null;
        }

        return load(args[i + 1]);
      }
    }

    printUsage();
    return null;
  }

  public URL url() {
    try {
      return new URL(iXml.getServer().getUrl());
    }
    catch (MalformedURLException e) {
      throw new IllegalArgumentException("Wrong url format", e);
    }
  }

  public List<TestScenario> listScenarios() {
    List<TestScenario> ret = new ArrayList<>();

    ScenariosType scenarios = iXml.getScenarios();

    for (ScenarioType scenario : scenarios.getScenario()) {
      if (!scenario.isEnabled())
        continue;

      String strategyFileString = scenario.getStrategy();
      File strategyFile = new File(strategyFileString);
      if (!strategyFile.exists()) {
        String strategyAbsolutePath =
                iConfigPath.substring(0, iConfigPath.lastIndexOf(File.separator)) + File.separator +
                        strategyFileString;
        strategyFile = new File(strategyAbsolutePath);
        if (!strategyFile.exists())
          throw new IllegalArgumentException("Unable to find strategy file:" + strategyFileString);
      }

      PlayParams param = new PlayParams();
      try {
        String code = FileUtils.readFileToString(strategyFile);
        param.code(code).startDate(scenario.getStartDate()).endDate(scenario.getEndDate())
                .initialCash(scenario.getInitialCash()).runType("backtest")
                .language(strategyFileString.endsWith("py") ? "python" : "java").owner(iXml.getServer().getUsername())
                .timeUnit(scenario.getBarType() == BarTypeEnum.DAY ? "d" : "m").title(scenario.getTitle())
                .portfolioName("p").benchmarkName("b");
      }
      catch (IOException e) {
        e.printStackTrace();
        continue;
      }

      ret.add(TestScenario.create(param, scenarios, scenario));
    }

    return ret;
  }

  public String username() {
    return iXml.getServer().getUsername();
  }

  public String password() {
    return iXml.getServer().getPassword();
  }
}
