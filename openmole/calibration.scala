// This workflow calibrate the simpop local model using a multi-objective
// genetic algorithm

logger.level("INFO")

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

import fr.geocite.simpoplocal.exploration._

val resPath = "/iscpif/users/reuillon/work/Slocal/published_model/front/"
//val resPath = "/tmp/"

val seed = Prototype[Long]("seed")
val rMax = Prototype[Double]("rMax")
val distanceDecay = Prototype[Double]("distanceDecay")
val pCreation = Prototype[Double]("pCreation")
val pDiffusion = Prototype[Double]("pDiffusion")
val innovationImpact = Prototype[Double]("innovationImpact")
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
    "modelTask", "modelResult = Model.run(rMax,innovationImpact,distanceDecay,pCreation,pDiffusion, 10000, 4000, newRNG(seed)) \n")

modelTask.addImport("fr.geocite.simpoplocal.exploration.*")

modelTask.addInput(rMax)
modelTask.addInput(distanceDecay)
modelTask.addInput(seed)
modelTask.addInput(pCreation)
modelTask.addInput(pDiffusion)
modelTask.addInput(innovationImpact)
modelTask.addOutput(modelResult)

val evalTask = 
  GroovyTask("EvalTask",
    "lognorm = new LogNormalKSTest() \n" + 
    "ksValue =  new Double(lognorm.getResultTest(modelResult.population).count(false)) \n" + 
    "deltaTest = new DeltaTest() \n" +  
    "deltaPop = deltaTest.getResultTest(modelResult.population, 10000) \n" + 
    "deltaTime = deltaTest.getResultTest(modelResult.time, 4000) \n"
  )

evalTask.addImport("fr.geocite.simpoplocal.exploration.*")
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
 
//, diversityMetric = GA.hypervolume(500, 100000, 10000)

val evolution = 
  GA (
    algorithm = GA.optimization(200, dominance = GA.strictEpsilon(0.0, 10.0, 10.0), diversityMetric = GA.hypervolume(500, 100000, 10000)),
    lambda = 1,
    termination = GA.timed(60 * 60 * 1000),
    cloneProbability = 0.01
  )

val nsga2  = 
  steadyGA(evolution)(
    "calibrateModel",
    replicateModel, 
    List(rMax -> ("2.0", "1000.0"), distanceDecay -> ("0.0", "4.0"), pCreation -> ("0.0" -> "0.01"), pDiffusion -> ("0.0", "0.01"),  innovationImpact -> ("0.0", "2.0")),
    List(sumKsFailValue -> "0", medPop -> "0", medTime -> "0")
  )

val islandModel = islandGA(nsga2)("island", 5000, GA.counter(200000), 50)

val mole = islandModel

val env = GliteEnvironment("biomed", openMOLEMemory = 1400, wallTime = "PT4H")

val path = resPath
val saveParetoHook = AppendToCSVFileHook(path + "pareto${" + islandModel.generation.name + "}.csv", islandModel.generation, islandModel.state, rMax.toArray, distanceDecay.toArray, pCreation.toArray, pDiffusion.toArray, innovationImpact.toArray, sumKsFailValue.toArray, medPop.toArray, medTime.toArray)

val display = DisplayHook("Generation ${" + islandModel.generation.name + "}, convergence ${" + islandModel.state.name + "}")

val ex = MoleExecution(
      mole,
      environments = Map(islandModel.island -> env),
      hooks = List(islandModel.outputCapsule -> saveParetoHook, islandModel.outputCapsule -> display)
    )


