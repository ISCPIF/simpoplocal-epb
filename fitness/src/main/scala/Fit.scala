/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.geocite.simpoplocal.exploration

object Fit {

  def apply(state: SimpopLocal#SimpopLocalState): Fit = apply(ModelResult(state))

  def apply(result: ModelResult, populationObj: Int = 10000, timeObj: Int = 4000): Fit = {
    val lognorm = new LogNormalKSTest
    val deltaTest = new DeltaTest
    Fit(
      ks = lognorm.getResultTest(result.population).count(_ == false).toDouble,
      deltaPop = deltaTest.getResultTest(result.population, populationObj),
      deltaTime = deltaTest.getResultTest(result.time, timeObj)
    )
  }

}

case class Fit(ks: Double, deltaPop: Double, deltaTime: Double)