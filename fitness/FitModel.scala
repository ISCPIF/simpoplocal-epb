/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.geocite.simpoplocal.exploration

import fr.geocite.simpoplocal._

object FitModel {

  trait Fit {
    def ks: Double
    def deltaPop: Double
    def deltaTime: Double
  }

  def apply(state: SimpopLocalState#STATE): Fit = apply(ModelResult(state))

  def apply(result: ModelResult, populationObj: Int = 10000, timeObj: Int = 4000): Fit = {
    val lognorm = new LogNormalKSTest
    val deltaTest = new DeltaTest
    new Fit{
      val ks = lognorm.getResultTest(result.population).count(_ == false).toDouble
      val deltaPop = deltaTest.getResultTest(result.population, populationObj)
      val deltaTime = deltaTest.getResultTest(result.time, timeObj)
    }
  }
  
}
