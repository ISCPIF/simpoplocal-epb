// This workflow calibrate the simpop local model using a multi-objective
// genetic algorithm

// Import namespaces of openmole plugins
import org.openmole.plugin.domain.distribution._
import org.openmole.plugin.domain.modifier._
import org.openmole.plugin.task.groovy._
import org.openmole.plugin.domain.bounded._
import org.openmole.plugin.hook.file._
import org.openmole.plugin.hook.display._
import org.openmole.plugin.method.stochastic._
import org.openmole.plugin.grouping.batch._
import org.openmole.plugin.environment.glite._

// Import model namespace
import fr.geocite.simpoplocal.exploration._

// Path where to store the results
val resPath = "/iscpif/users/reuillon/work/Slocal/published_model/front/"

// Define the variables
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
   
// Define the task which runs the model
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

// Define the task which evaluate a single replication
val evalTask = 
  GroovyTask("EvalTask",
    "ksValue =  new Double(LogNormalKSTest.test(modelResult.population).count(false)) \n" + 
    "deltaPop = DeltaTest.delta(modelResult.population, 10000) \n" + 
    "deltaTime = DeltaTest.delta(modelResult.time, 4000) \n"
  )

evalTask.addImport("fr.geocite.simpoplocal.exploration.*")
evalTask.addImport("org.apache.commons.math.random.*")
evalTask.addImport("umontreal.iro.lecuyer.probdist.*")

evalTask.addInput(modelResult)
evalTask.addOutput(ksValue)
evalTask.addOutput(deltaPop)
evalTask.addOutput(deltaTime)
    
// Define the workflow that run the model and evaluate it
val modelCapsule = Capsule(modelTask)
val evalCapsule = Capsule(evalTask) 

val eval = modelCapsule -- evalCapsule

// Define the workflow that replicate the evaluation and aggregate the results
val stat = Statistics()
stat.addSum(ksValue, sumKsFailValue)
stat.addMedian(deltaPop, medPop)
stat.addMedian(deltaTime, medTime)

val seedFactor = Factor(seed, UniformLongDistribution() take 100)
val replicateModel = statistics("replicateModel", eval, seedFactor, stat)

// Define an island
import org.openmole.plugin.method.evolution._
import ga._

val scales = 
  Seq(
    rMax -> ("2.0", "50000.0"),
    distanceDecay -> ("0.0", "4.0"),
    pCreation -> ("0.0" -> "0.01"),
    pDiffusion -> ("0.0", "0.01"),
    innovationImpact -> ("0.0", "2.0")
  )

val evolution = 
  Optimisation (
    mu = 200,
    dominance = StrictEpsilon(0.0, 10.0, 10.0)
    termination = Timed(1 hour),
    ranking = Pareto,
    inputs = scales,
    objectives = Seq(sumKsFailValue, medPop, medTime),
    cloneProbability = 0.0
  )

val nsga2  = 
  steadyGA(evolution)(
    "calibrateModel",
    replicateModel
  )

// Define the island model
val islandModel = islandGA(nsga2)("island", 5000, Counter(200000), 50)

val mole = islandModel

// Define the execution environment
val env = GliteEnvironment("biomed", openMOLEMemory = 1400, wallTime = 4 hours)

// Define the hook to save the results
val path = resPath
val savePopulation = SavePopulationHook(islandModel, path + "/populations/")

// Define the hook to display the generation in the console
val display = DisplayHook("Generation ${" + islandModel.generation.name + "}")

// Define the execution
val ex = 
  (mole + 
   (islandModel.island on env) + 
   (islandModel.outputCapsule hook savePopulation hook display)) toExecution

// Lauch the execution
ex.start

