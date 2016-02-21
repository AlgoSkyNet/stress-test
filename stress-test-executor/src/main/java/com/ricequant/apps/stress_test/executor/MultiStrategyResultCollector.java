package com.ricequant.apps.stress_test.executor;

import com.ricequant.apps.stress_test.client.ExecutionStatusHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * This class collects run results of all runs in the scenario and output the final results
 *
 * @author chenfeng
 */
public class MultiStrategyResultCollector {

  private final int iNumExpected;

  private final AtomicInteger iResultsReceived = new AtomicInteger(0);

  private final AtomicInteger iResultsOvertime = new AtomicInteger(0);

  private final double iNormalRatio;

  private final BiConsumer<Boolean, Double> iResultAcceptor;

  private final long iOvertimeThreshold;

  private final AtomicLong iTotalNormalRunTime = new AtomicLong(0);

  private final Logger iLogger = LoggerFactory.getLogger(getClass());

  private final Collector collector;

  private MultiStrategyResultCollector(long overtimeThreshold, int numExpected, double normalRatio,
          BiConsumer<Boolean, Double> resultAcceptor) {
    iNumExpected = numExpected;
    iNormalRatio = normalRatio;
    iResultAcceptor = resultAcceptor;
    iOvertimeThreshold = overtimeThreshold;

    collector = new Collector();
  }

  private class Collector implements BiConsumer<String, Long> {

    @Override
    public void accept(String resultStatus, Long millisElapsed) {
      if (ExecutionStatusHelper.isCanceled(resultStatus)) {
        iLogger.info("Strategy execution canceled, running time is: " + millisElapsed);
        iResultsOvertime.incrementAndGet();
      }
      else if (ExecutionStatusHelper.isError(resultStatus)) {
        iLogger.info("Strategy execution error, running time is: " + millisElapsed);
        iResultsOvertime.incrementAndGet();
      }
      else if (ExecutionStatusHelper.isSuccess(resultStatus)) {
        if (millisElapsed > iOvertimeThreshold) {
          iLogger.info("Strategy execution normally exited but overdue. Threshold is: " + iOvertimeThreshold
                  + ", running time is: " + millisElapsed);
          iResultsOvertime.incrementAndGet();
        }
        else {
          iLogger.info("Strategy execution normally exited, running time is: " + millisElapsed);
          iTotalNormalRunTime.addAndGet(millisElapsed);
        }
      }

      if (ExecutionStatusHelper.isExit(resultStatus)) {
        if (iNumExpected == iResultsReceived.incrementAndGet()) {
          double numNormals = iResultsReceived.get() - iResultsOvertime.get();
          iResultAcceptor.accept(numNormals / iResultsReceived.get() >= iNormalRatio,
                  numNormals == 0 ? 0 : iTotalNormalRunTime.get() / numNormals);
        }
      }
    }
  }

  /**
   * Creates an instance of the collector
   *
   * @param overtimeThreshold
   *         number of milliseconds that if a strategy running time is beyond this threshold will be treated as
   *         overtime
   * @param numResultsExpected
   *         it usually equals to number of strategies started, means the collector will expect such number of
   *         strategies to finish running before finalizing the results
   * @param lowestNormalRatio
   *         a number in range (0, 1] such that: if number-of-successful-runs / number-of-results-expected is greator
   *         than the ratio, the whole pass is considered successful
   * @param resultAcceptor
   *         callback to receive the final results. The first parameter is a boolean, true if the pass is successful,
   *         false otherwise; the second parameter is the average running time for all successfully finished strategy
   *         runs.
   *
   * @return the acceptor to be passed into each StrategyPlayer
   */
  static BiConsumer<String, Long> getResultAcceptor(long overtimeThreshold, int numResultsExpected,
          double lowestNormalRatio, BiConsumer<Boolean, Double> resultAcceptor) {
    return new MultiStrategyResultCollector(overtimeThreshold, numResultsExpected, lowestNormalRatio,
            resultAcceptor).collector;
  }
}
