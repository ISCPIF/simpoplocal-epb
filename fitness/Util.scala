/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.geocite.simpoplocal.exploration

import scala.collection.JavaConversions._

object Util {

  def convertIterableToSeq(value: Iterable[_]): IndexedSeq[_] = {
    return value.toIndexedSeq
  }

  def convertArrayToSeq(value: Array[_]): IndexedSeq[_] = {
    return value.toIndexedSeq
  }

  def mediane(s: Array[Double]): Double = {
    val (lower, upper) = s.sortWith(_ < _).splitAt(s.size / 2)
    if (s.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
  }

  def medianeAbsolutDeviation(s: Array[Double], medianeValue: Double) = {
    mediane(s.map {
      _ - medianeValue
    })
  }

  def evalMediane(
    evalKs: Array[Array[Boolean]],
    evalDValue: Array[Double],
    evalDeltaPop: Array[Double],
    evalDeltaTime: Array[Double]): Array[Double] = {

    //Minimize median value only for Dvalue, DeltaPop, DeltaTime
    val nbSimulFalse: Double = evalKs.map {
      e => !e(0) || !e(1)
    }.count(e => e).toDouble
    val medDValue: Double = mediane(evalDValue)
    val medDeltaPop: Double = mediane(evalDeltaPop)
    val medDeltaTime: Double = mediane(evalDeltaTime)
    val medAbsDeviationPop: Double = medianeAbsolutDeviation(evalDeltaPop, medDeltaPop)
    val medAbsDeviationTime: Double = medianeAbsolutDeviation(evalDeltaTime, medDeltaTime)

    //Pour le moment on ne met pas medDvalue ...
    return Array(nbSimulFalse, medDeltaPop, medDeltaTime, medAbsDeviationPop, medAbsDeviationTime)
  }

}
  