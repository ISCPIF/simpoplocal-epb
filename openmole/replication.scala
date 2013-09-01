logger.level("FINE")

import org.openmole.plugin.sampling.combine._
import org.openmole.plugin.domain.distribution._
import org.openmole.plugin.domain.modifier._
import org.openmole.plugin.domain.collection._
import org.openmole.plugin.task.groovy._
import org.openmole.plugin.domain.bounded._
import org.openmole.plugin.hook.file._
import org.openmole.plugin.hook.display._
import org.openmole.plugin.builder.stochastic._
import org.openmole.plugin.grouping.batch._
import org.openmole.plugin.environment.glite._
import org.openmole.plugin.environment.desktopgrid._
import org.openmole.plugin.source.file._

import fr.geocite.simpoplocal.exploration._

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

val sampling = 
  maxAbondance.toArray.toFactor zip distanceF.toArray.toFactor zip pSuccessInteraction.toArray.toFactor zip pSuccessAdoption.toArray.toFactor zip innovationFactor.toArray.toFactor

val explo = ExplorationTask("explo", sampling)
val exploCapsule = Capsule(explo)

val modelTask = 
  GroovyTask(
    "modelTask", "modelResult = Model.run(maxAbondance,innovationFactor,distanceF,pSuccessInteraction,pSuccessAdoption, 10000, 4000, newRNG(seed)) \n")

modelTask.addImport("fr.geocite.simpoplocal.exploration.*")

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

val seedFactor = Factor(seed, new UniformLongDistribution take 10000)
val replicateModel = statistics("replicateModel", eval, seedFactor, stat)

val env = GliteEnvironment("biomed", openMOLEMemory = 1400, wallTime = "PT4H")

val saveHook = AppendToCSVFileHook(resPath + "replication10000.csv", maxAbondance, distanceF, pSuccessInteraction, pSuccessAdoption, innovationFactor, sumKsFailValue, medPop, medTime)

val readCSV = CSVSource(resPath + "pareto100001.csv")
readCSV addColumn maxAbondance
readCSV addColumn distanceF
readCSV addColumn pSuccessInteraction
readCSV addColumn pSuccessAdoption
readCSV addColumn innovationFactor

val ex = 
  ((exploCapsule source readCSV) -< replicateModel).toExecution(
      sources = Map(exploCapsule -> readCSV),
      hooks = List(replicateModel.last -> saveHook),
      environments = Map(modelCapsule -> env),
      grouping = Map(modelCapsule -> new ByGrouping(500))
  ) 

ex.start

