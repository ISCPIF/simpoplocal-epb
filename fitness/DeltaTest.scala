/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.geocite.simpoplocal.exploration
import scala.util.Sorting._
class DeltaTest {

  def getResultTest(simulValue:Array[Double],theoricalValue:Double):Double= math.abs(simulValue.max - theoricalValue)
  
  def getResultTest(simulValue:Double,theoricalValue:Double):Double= math.abs(simulValue - theoricalValue )

}
