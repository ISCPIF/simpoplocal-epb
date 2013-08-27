logger.level("FINE")

import org.openmole.plugin.domain.distribution._
import org.openmole.plugin.domain.modifier._
import org.openmole.plugin.task.groovy._
import org.openmole.plugin.domain.bounded._
import org.openmole.plugin.hook.file._
import org.openmole.plugin.hook.display._
import org.openmole.plugin.builder.stochastic._
import org.openmole.plugin.grouping.batch._
import org.openmole.plugin.environment.glite._
import org.openmole.plugin.environment.desktopgrid._
import org.openmole.plugin.profiler.csvprofiler._

import fr.geocite.simpoplocal._

val resPath = "/iscpif/users/reuillon/work/Slocal/front_6_constraint/"
//val resPath = "/tmp/"

val seed = Prototype[Long]("seed")
val maxAbondance = Prototype[Double]("maxAbondance")
val distanceF = Prototype[Double]("distanceF")
val pSuccessInteraction = Prototype[Double]("pSuccessInteraction")
val pSuccessAdoption = Prototype[Double]("pSuccessAdoption")
//val inovationLife = Prototype[Double]("inovationLife")
val innovationFactor = Prototype[Double]("innovationFactor")
val modelResult = Prototype[ModelResult]("modelResult")

val deltaTime = Prototype[Double]("deltaTime")
val deltaPop = Prototype[Double]("deltaPop")
val ksValue = Prototype[Double]("ksValue")

val sumKsFailValue = Prototype[Double]("sumKsFailValue")
val medPop = Prototype[Double]("medPop")
val medTime = Prototype[Double]("medTime")

val medADDeltaPop = Prototype[Double]("medADDeltaPop")
val medADDeltaTime = Prototype[Double]("medADDeltaTime")
   
val modelTask = 
  GroovyTask(
    "modelTask", "modelResult = Model.run(maxAbondance,innovationFactor,distanceF,pSuccessInteraction,pSuccessAdoption, 10000, 4000, newRNG(seed)) \n")

modelTask.addImport("fr.geocite.simpoplocal.*")

modelTask.addInput(maxAbondance)
modelTask.addInput(distanceF)
modelTask.addInput(seed)
modelTask.addInput(pSuccessInteraction)
modelTask.addInput(pSuccessAdoption)
//modelTask.addInput(inovationLife)
modelTask.addInput(innovationFactor)
modelTask.addOutput(modelResult)

val evalTask = 
  GroovyTask("EvalTask",
    "lognorm = new LogNormalKSTest() \n" + 
    "ksValue =  new Double(lognorm.getResultTest(modelResult.population).count(false)) \n" + 
    "deltaTest = new DeltaTest() \n" +  
    "deltaPop = deltaTest.getResultTest(modelResult.population, 10000) \n" + 
    "deltaTime = deltaTest.getResultTest(modelResult.time, 4000) \n"
  )

evalTask.addImport("fr.geocite.simpoplocal.*")
evalTask.addImport("org.apache.commons.math.random.*")
evalTask.addImport("umontreal.iro.lecuyer.probdist.*")

evalTask.addInput(modelResult)
evalTask.addOutput(ksValue)
evalTask.addOutput(deltaPop)
evalTask.addOutput(deltaTime)
    
val modelCapsule = Capsule(modelTask)
val evalCapsule = Capsule(evalTask) 

val eval = modelCapsule -- evalCapsule

// STOCHASTICITY OUTPUT ///////////////////////////////////////////////

val stat = new Statistics
stat.addSum(ksValue, sumKsFailValue)
stat.addMedian(deltaPop, medPop)
stat.addMedian(deltaTime, medTime)
//stat.addMedianAbsoluteDeviation(deltaPop, medADDeltaPop)
//stat.addMedianAbsoluteDeviation(deltaTime, medADDeltaTime)

val seedFactor = Factor(seed, new UniformLongDistribution take 100)
val replicateModel = statistics("replicateModel", eval, seedFactor, stat)

// SCALING //////////////////////////////////////////////////

import org.openmole.plugin.builder.evolution._
import org.openmole.plugin.method.evolution._
 
val evolution = 
  GA (
    algorithm = GA.optimization(200),
    lambda = 1,
    dominance = GA.strictEpsilon(0.0, 10.0, 10.0),
    termination = GA.timed(60 * 120 * 1000),
    diversityMetric = GA.hypervolume(500, 100000, 10000),
    cloneProbability = 0.01
  )

val nsga2  = 
  steadyGA(evolution)(
    "calibrateModel",
    replicateModel, 
    List(maxAbondance -> ("2.0", "1000.0"), distanceF -> ("0.0", "4.0"), pSuccessInteraction -> ("0.0" -> "0.01"), pSuccessAdoption -> ("pSuccessInteraction", "0.01"),  innovationFactor -> ("0.0", "2.0")),
    List(sumKsFailValue -> "0", medPop -> "0", medTime -> "0")
  )

val islandModel = islandGA(nsga2)("island", 5000, GA.counter(100000), 50)

val mole = islandModel

val env = GliteEnvironment("biomed", openMOLEMemory = 1400, wallTime = "PT4H")

val path = resPath
val saveParetoHook = AppendToCSVFileHook(path + "pareto${" + islandModel.generation.name + "}.csv", islandModel.generation, islandModel.state, maxAbondance.toArray, distanceF.toArray, pSuccessInteraction.toArray, pSuccessAdoption.toArray, innovationFactor.toArray, sumKsFailValue.toArray, medPop.toArray, medTime.toArray)

val display = DisplayHook("Generation ${" + islandModel.generation.name + "}, convergence ${" + islandModel.state.name + "}")

val ex = MoleExecution(
      mole,
      selection = Map(islandModel.island -> env),
      hooks = List(islandModel.outputCapsule -> saveParetoHook, islandModel.outputCapsule -> display)
    )


