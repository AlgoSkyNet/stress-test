package com.ricequant.apps.stress_test.executor;

import com.ricequant.apps.stress_test.client.ExecutionStatusHelper;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/**
 * Decide the running time of a strategy
 *
 * @author chenfeng
 */
public class SingleStrategySpeedDecider {

  private final long iTotalTimeSpent;

  private final int iNumRuns;

  private final Consumer<BiConsumer<String, Long>> iPlayNext;

  private final int iTrialLimit;

  private final DoubleConsumer iOnResolved;

  private final Decider decider;

  private SingleStrategySpeedDecider(final int trialLimit, long totalTimeSpent, int numRuns,
          Consumer<BiConsumer<String, Long>> playNext, DoubleConsumer onResolved) {
    iTrialLimit = trialLimit;
    iTotalTimeSpent = totalTimeSpent;
    iNumRuns = numRuns;
    iPlayNext = playNext;
    iOnResolved = onResolved;

    decider = new Decider();
  }

  /**
   * Creates a result acceptor instance for the StrategyPlayer, who will decide the speed when there is only one
   * strategy running on the machine
   *
   * @param trialLimit
   *         how many runs to do before calculating the average time
   * @param playNext
   *         callback which accepts the next result acceptor to be passed into the StrategyPlayer as the parameter when
   *         the decider needs to run another strategy
   * @param onResolved
   *         callback which accepts the average running time when the speed is decided
   *
   * @return the result acceptor implements the BiConsumer interface
   */
  static BiConsumer<String, Long> getResultAcceptor(int trialLimit, Consumer<BiConsumer<String, Long>> playNext,
          DoubleConsumer onResolved) {
    return new SingleStrategySpeedDecider(trialLimit, 0, 0, playNext, onResolved).decider;
  }

  private class Decider implements BiConsumer<String, Long> {

    @Override
    public void accept(String resultStatus, Long millisElapsed) {
      if (ExecutionStatusHelper.isSuccess(resultStatus)) {
        if (iTrialLimit == iNumRuns + 1)
          iOnResolved.accept((double) (iTotalTimeSpent + millisElapsed) / (iNumRuns + 1));
        else
          iPlayNext.accept(new SingleStrategySpeedDecider(iTrialLimit, iTotalTimeSpent + millisElapsed, iNumRuns + 1,
                  iPlayNext, iOnResolved).decider);
      }
      else {
        if (iNumRuns == 0)
          iOnResolved.accept(-1);
        else
          iOnResolved.accept((double) iTotalTimeSpent / iNumRuns);
      }
    }
  }
}
