# Stress Test Client Readme
This project is only for testing purpose. It consists of a library to start and monitor the status of strategy runs, and a testing tool to do the stress test. The logic will be explained below.
##Introduction
###The Library - Facade Restful Client
This library communicates with the Ricequant Facade component to start strategy runs and pull feeds periodically. To check the usage, please refer to the stress test executor project.
### The Testing Tool - Stress Test Executor
The whole execution consists of two parts: the initial-speed-test and the stress-test parts. In stress-test part, there are two phases: the expansion phase and the refining phase. Each phase is consisted of several passes, which parameters are adjusted automatically to find out the true capacity of the system.

The executor calls the library in the following way to figure out the system capacity. 

 1. Clean up the target environment by stopping all running strategies
 2. Run testing strategy several times and make sure there is only one running instance at a time. Figure out the average elapsed time T<sub>1</sub>
 3. Run the same strategy of N instances at the same time, record each running time T<sub>n</suB>. If T<sub>n</sub> > T<sub>1</sub> * tolerance_multiplier, mark overtime, otherwise mark success. tolerance_multipler can be configured in range [1, +infinity). So we get number of successful runs N<sub>s</sub>.
 4. N<sub>s</sub>/N > overtime_tolerance, the pass in step 3 is considered passed, increase N to N*expand_grow_factor.
 5. If N<sub>s</sub>/N <= overtime_tolerance, the pass in step 3 is considered failed. It enters refining phase and reduce N to N*refine_shrink_factor.
 6. If step 5 is successful, we increase N to N*refine_grow_factor. Usually refine_grow_factor is smaller than the expand_grow_factor.

All variables can be configured.

##Build
To build the project after cloning to your local machine, run *mvn clean install* from the top level. It requires internet connection to build properly.

To build javadoc, use *mvn compile site*, you will find it under the target/site folder of each submodule.

##Configuration Guide for Stress Test Executor
Sample configuration, can be found in the source code of stress-test-executor project under src/main/config.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<stressTest xmlns="http://www.ricequant.com/generated-config/apps/stress-test"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.ricequant.com/generated-config/apps/stress-test facade-client.xsd">
	
	<server url="http://stress.ricequant.com/backend-restful" 
		username="huatai@ricequant.com" 
		password="V!/>FW9@n'[ks7*W" />
	
	<scenarios expandGrowFactor="2" 
		initialParallelRuns="4" 
		overtimeToleranceMultiplier="2" 
		refineGrowFactor="1.1" 
		refineShrinkFactor="0.8" 
		successTolerancePercentage="0.8"
		maxRefineRuns="5">

		<scenario enabled="true" 
			title="Scene1"
			startDate="20140109"
			endDate="20140110"
			numInitialSpeedTestRuns="5"
			timeoutMillis="300000"
			barType="Minute"
			strategy="empty_loop.py" />

	</scenarios>

</stressTest>

```
* **stressTest**: the root node, don't change it
  * **server**: configure endpoint and credentials to connect to the Ricequant Facade
   --**url**: endpoint for testing, distributed by Ricequant
   --**username** & **password**: circulated by Ricequant periodically via Email, usually per week
  * **scenarios**: parent node for all scenarios
   --**expandGrowFactor**: grow factor in expansion phase
   --**initialParallelRuns**: the number of runs to begin with in the stress test part
   --**overtimeToleranceMultiplier**: runs are considered pass if the time elapsed is shorter than this number multiplies the load-free time
   --**refineGrowFactor**: grow actor in the refining phase
   --**refineShrinkFactor**: shrink factor in the refining phase
   --**successTolerancePercentage**: the percentage of number of successful runs during a pass, either in expansion or refining phase. If set to 0.8, it means if 0.8 * N runs are successful, the pass is considered successful
   --**maxRefineRuns**: how many refine passes to be run to avoid oscillation.
	   * **scenario**: define strategy parameters for each scenario
	   --**enabled**: the scenario only runs when enabled is set to true
	   --**title**: title of the strategy
	   --**startDate**: start date of the backtest
	   --**endDate**: end date of the backtest
		--**timeoutMillis**: the kill time for a strategy. If a strategy runs over this limit, it will be killed from the server side with AbnormalExit, rather than killing from the client side with CancelExit
	   --**barType**: Minute or Day
	   --**strategy**: file path relative to the configuration xml, or to the working directory
	   
Most of the configuration options are optional, please refer to the xsd file in src/main/resources under stress-test-executor project.

##Execute
After ```mvn clean install```, you can find an executable jar under the stress-test-executor project's target directory. It should take the name *stress-test-executor-1.0-SNAPSHOT.jar*. It can be executed with command ```java -jar stress-test-executor-1.0-SNAPSHOT.jar -c stress-test.xml```, where stress-test.xml needs to be written by yourself (or just copy from here). There is a benchmark strategy we have been using for stress testing called "stress-bench.py", which is a minute bar strategy, and it is recommended to use it directly.
