# Stress Test Client
This project consists of a library to start and monitor the status of strategy runs, and a testing tool to do the stress test. The logic will be explained below. The purpose of this client is to find out that in an acceptable performance degeneration situation, how many concurrent strategy runs can be executed.
##Introduction
The library handles communications at the low level with the Ricequant's Facade, the testing tool calls the library to execute strategies following certain rules to find out the system capacity. As we know the system bottleneck is at the server side, not network or client machine, we made the testing tool to allow only one instance at a time for simplicity.
###The Library - Facade Restful Client
This library communicates with the Ricequant Facade component to start strategy runs and pull feeds periodically. To check the usage, please refer to the stress test executor project.
### The Testing Tool - Stress Test Executor
This project finds out the capacity of the system defined by: how many of the strategies can be run concurrently with a tolerable performance decay.

>**Example**
>If we can run strategy in 30 seconds when the system is zero-loaded (no other activities but only running this strategy), by given tolerance factor equals to 2.0, 50 strategies can the system finish within 60 seconds, we say the capacity is 50. The tolerance factor can be configured.

The whole execution consists of two parts: the initial-speed-test and the stress-test. In stress-test part, there are two phases: the expansion phase and the refining phase. Each phase is consisted of several passes, which parameters are adjusted automatically to find out the true capacity of the system.

The executor calls the library in the following way to figure out the system capacity. 

 1. Clean up the target environment by stopping all running strategies
 2. Run testing strategy several times and make sure there is only one running instance at a time. Figure out the average elapsed time T<sub>1</sub>
 3. Run the same strategy of N instances at the same time, record each running time T<sub>n</sub>. If T<sub>n</sub> > T<sub>1</sub> * overtime_tolerance_multiplier, mark overtime, otherwise mark success. overtime_tolerance_multiplier can be configured in range [1, +infinity). So we get number of successful runs N<sub>s</sub>.
 4. N<sub>s</sub>/N > overtime_tolerance, the pass in step 3 is considered passed, increase N to N*expand_grow_factor.
 5. If N<sub>s</sub>/N <= overtime_tolerance, the pass in step 3 is considered failed. It enters refining phase and reduce N to N*refine_shrink_factor.
 6. If step 5 is successful, we increase N to N*refine_grow_factor. Usually refine_grow_factor is smaller than the expand_grow_factor.
 7. Repeating step 5. and 6. until max_refine_runs reached, output statistics of the last successful pass including: how many strategies were run successfully, and the average running time.

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
		username="test@ricequant.com" 
		password="pass" />
	
	<scenarios theoreticalUpperBound="50"
	    expandGrowFactor="2" 
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
  * **server**: configure endpoint and credentials to connect to the Ricequant Facade<br/>
   --**url**: endpoint for testing, distributed by Ricequant<br/>
   --**username** & **password**: circulated by Ricequant periodically via Email, usually per week<br/>
  * **scenarios**: parent node for all scenarios<br/>
   --**theoreticalUpperBound**: the number of parallel strategies can run bounded by the memory installed on the server. This number is provided by Ricequant, or can be requested when needed.<br/>
   --**expandGrowFactor**: grow factor in expansion phase<br/>
   --**initialParallelRuns**: the number of runs to begin with in the stress test part<br/>
   --**overtimeToleranceMultiplier**: runs are considered pass if the time elapsed is shorter than this number multiplies the load-free time<br/>
   --**refineGrowFactor**: grow factor in the refining phase<br/>
   --**refineShrinkFactor**: shrink factor in the refining phase<br/>
   --**successTolerancePercentage**: the percentage of number of successful runs during a pass, either in expansion or refining phase. If set to 0.8, it means if 0.8 * N runs are successful, the pass is considered successful<br/>
   --**maxRefineRuns**: how many refine passes to be run to avoid oscillation.<br/>
	   * **scenario**: define strategy parameters for each scenario<br/>
	   --**enabled**: the scenario only runs when enabled is set to true<br/>
	   --**title**: title of the strategy<br/>
	   --**startDate**: start date of the backtest<br/>
	   --**endDate**: end date of the backtest<br/>
	   --**numInitialSpeedTestRuns**: how many times running the strategy in load-free environment to determine the average running time
		--**timeoutMillis**: the kill time for a strategy. If a strategy runs over this limit, it will be killed from the server side with AbnormalExit, rather than killing from the client side with CancelExit<br/>
	   --**barType**: Minute or Day<br/>
	   --**strategy**: file path relative to the configuration xml, or to the working directory
	   
Most of the configuration options are optional, please refer to the xsd file in src/main/resources under stress-test-executor project.

##Execute
After ```mvn clean install```, you can find an executable jar under the stress-test-executor project's target directory. It should take the name *stress-test-executor-1.0-SNAPSHOT.jar*. It can be executed with command ```java -jar stress-test-executor-1.0-SNAPSHOT.jar -c stress-test.xml```, where stress-test.xml needs to be written by yourself (or just copy from here). There is a benchmark strategy we have been using for stress testing called "stress-bench.py", which is a minute bar strategy, and it is recommended to use it directly. We don't support fundamentals currently in the stress test environment. All feeds will be written under logs/stress-test.debug along with other run information. Please make sure the current user has write permission.

The whoe project can be imported as a maven project into IDEs like Intellij IDEA. After importing, you can run the main method in StressTesterMain class.


---------------------
# Stress Test 客户端
本客户端包含一个底层连接库和一个压测工具。压测时我们主要关注的是策略并行数及性能损失率。目标是要找出在能容忍的性能退化范围内，最多能并行多少个策略运行。
##简介
底层连接库主要用于和Ricequant的Facade前端通过Restful API进行交互，压测工具则管理一些可配置的压测场景来测试系统容量。我们已经发现系统容量的瓶颈在负责策略运行的机器上，所以压测工具中没有提供并行能力。如果需要，可以通过调用底层连接库来实现。

###底层连接库
它只负责与Ricequant的Facade前端进行交互，具体可以实现1）启动策略；2）停止策略；3）停止所有正在运行的策略；4）拉取运行中或运行完毕策略的Feed及实时状态
### 压测工具
这个工具的目的是找到当前系统的承载量。具体来说，它测量了在一定的性能牺牲之下，最多能并行运行多少个策略。

----------

>**举例**
>假设当系统完全无压力时（全系统只运行一个策略），一个策略的运行时间是30秒。我们给定一个两倍的性能容忍度，以及一个稳定性容忍度0.8，此时并行运行了50个策略，其中40个在30＊2＝60秒之内完成，另外10个策略超过了60秒，但也正常运行完毕。那我们就说此时的系统容量为50。

整个测试过程分为两部分：**初速度测试**及**压测**。在压测部分，又分为两个阶段：扩张阶段和精分阶段。每个阶段中再分批次执行，目的是更精确地判断出系统容量。

具体来说，压测工具会进行以下步骤：

 1. 清空环境中已运行的策略（如有）
 2. 进行初速度测试部分。在确保环境中只有一个策略运行的情况下，运行若干次某一策略取平均运行时间，记为 T<sub>1</sub>
 3. 进行压测部分的扩张阶段。同时启动N个上述策略的运行实例，并记录每个策略各自的运行时间 T<sub>n</sub>。这个运行时间是指客户端下载完所有的Feeds，而非仅是服务器端完成回测。如果 T<sub>n</sub> > T<sub>1</sub> * overtime_tolerance_multiplier变量，则记为超时，否则记为成功。 overtime_tolerance_multiplier 可以在范围 [1, +infinity)内任意配置。记总成功数量为 N<sub>s</sub>.
 4. 如果N<sub>s</sub>/N > overtime_tolerance，则认为第三步成功，将N扩大至 N*expand_grow_factor，重复第三步，扩张阶段未结束。
 5. 如果 N<sub>s</sub>/N <= overtime_tolerance，则认为第三步执行失败，进入**精分阶段**，同时将N设为N*refine_shrink_factor (refine_shrink_factor是一个小于1的正数，可配置）.
 6. 如果第五步成功了，N会被设为 N*refine_grow_factor，继续精分阶段。refine_grow_factor 通常会小于 expand_grow_factor，示例中exapnd_grow_factor是2，而refine_grow_factor是1.1。
 7. 重复第5和第6步，直到精分阶段的运行次数达到max_refine_runs所规定的上限，然后输出结果。输出的结果包括最后一次成功运行的精分批次中同时运行的策略数量以及平均运行时间。

上面提到的所有变量都可以配置。

##构建
将源码clone至本地后在根目录执行```mvn clean install```即可。构建过程需要连接互联网。

如果希望直接使用底层通信库，可使用```mvn compile site```来构建JavaDoc文档。构建出的成品可以在各子项目的 target/site 中找到。

##压测工具的配置文件示例
该文件可以在stress-test-executor 项目的 src/main/config 目录中找到。其中&lt;server&gt;的password配置项会不定期更改，Ricequant届时会以邮件等方式通知用户。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<stressTest xmlns="http://www.ricequant.com/generated-config/apps/stress-test"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.ricequant.com/generated-config/apps/stress-test facade-client.xsd">
	
	<server url="http://stress.ricequant.com/backend-restful" 
		username="test@ricequant.com" 
		password="pass" />
	
	<scenarios theoreticalUpperBound="50"
	    expandGrowFactor="2" 
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
* **stressTest**: 根节点，无需配置
  * **server**: 配置连接和权限信息<br/>
   --**url**: 由Ricequant指定的测试接口url<br/>
   --**username** & **password**: 由Ricequant分发的用户名和密码，通常每周一次<br/>
  * **scenarios**: 所有场景定义的父节点<br/>
   --**theoreticalUpperBound**：服务器能支撑的并行运行理论上限，由内存决定。这个数字由Ricequant提供或可应邀更改<br />
   --**expandGrowFactor**: 扩张阶段的增长系数<br/>
   --**initialParallelRuns**: 压测部分增长阶段的第一个批次中的并行数量<br/>
   --**overtimeToleranceMultiplier**: 用这个数乘以初速度来决定在压测部分的性能容忍性（见前面的举例）<br/>
   --**refineGrowFactor**: 在压测部分的精分阶段如果前一个批次成功，下一个批次的增长系数。在实际压测中，增长的绝对值永远大于等于1<br/>
   --**refineShrinkFactor**: 在压测部分的精分阶段中如果前一个批次失败，下一个批次的收缩系数。在实际压测中，收缩的绝对值永远大于等于2<br/>
   --**successTolerancePercentage**: 在某一批次中成功运行的策略与失败的策略的比值如果大于该数字，则认为批次成功，否则认为失败。<br/>
   --**maxRefineRuns**: 为避免震荡效应（重复-2, +1, +1, -2, +1 +1的过程），一旦精分阶段的运行批次总数达到这个数，则停止压测<br/>
	   * **scenario**: 定义每一场景的属性<br/>
	   --**enabled**: 当此属行为true时场景被执行，否则被跳过<br/>
	   --**title**: 策略/场景标题<br/>
	   --**startDate**: 回测开始时间<br/>
	   --**endDate**: 回测结束时间<br/>
	   --**numInitialSpeedTestRuns**：测量初速度时运行策略的次数（之后会去平均值）
		--**timeoutMillis**: 策略硬超时时间。一旦策略运行超过此时间限制，策略将会被强行取消，以应对可能出现的没有响应的意外情况来确保程序执行完毕。通常这个时间会设置得较长。<br/>
	   --**barType**: 可取"Minute"或"Day"的值来决定回测类型<br/>
	   --**strategy**: 定义策略代码的路径，可相对于xml配置文件位置，也可相对于进程的工作目录
	   
事实上大部分的配置项是可选的，请参考在stress-test-executor项目下 src/main/resources under 的xsd定义文件。

##运行
执行完构建步骤后，可执行jar将会出现在 stress-test-executor项目的 target 目录下。它应该会被命名为 *stress-test-executor-1.0-SNAPSHOT.jar*. 用以下命令执行： ```java -jar stress-test-executor-1.0-SNAPSHOT.jar -c stress-test.xml```，注意stress-test.xml的文件是由用户自己创建的（当然也可以从项目的src/main/config目录下复制。我们已经提供了一个叫stress-test.py的测试算法，推荐用这个来测。在测试环境中我们暂时不支持财务数据查询。所有的Feeds会被记录在logs目录下的stress-test.debug中（该目录和文件会被自动创建，请确保写权限）。

另外，整个项目可以作为普通的maven项目导入IDE中，如Intellij。可以直接在IDE中运行StressTesterMain的main函数来做测试。
