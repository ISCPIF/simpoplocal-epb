/*
 * Copyright (C) 25/04/13 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import fr.geocite.simpoplocal.exploration.writer.CSVWriter
import fr.geocite.simpoplocal.exploration.SimpopLocal
import scala.util.Random
import java.io.{File, FileWriter, BufferedWriter, Writer}

object WriteResultGraph1 extends App {

  val replications = 5
  val folderPath = "/tmp/"
  val file = folderPath + "variabilityRankSize_" + replications.toString() + ".csv"

  val writer = new BufferedWriter(new FileWriter(new File(file)))

  val m = new SimpopLocal {
    def distanceDecay: Double = 0.6948037684879382

    def innovationImpact: Double = 0.008514548363730353

    def maxInnovation: Double = 10000

    def pCreation: Double = 8.672482701792705E-7

    def pDiffusion: Double = 8.672482701792705E-7

    def rMax: Double = 10586
  }

  val rng = new Random(42)

  case class Q1(state: SimpopLocal#SimpopLocalState, s: Long) {
    val final_step = state.step
    val seed = s
    val pop = state.settlements.map {
      s => (s.id, s.population)
    }
  }

  val simulation = Iterator.continually(rng.nextLong).take(replications).toSeq.par.map {
    seed =>
      implicit val threadRng = new Random(seed)
      new Q1(m.run, seed)
  }.seq


  println("Q1 max > " + simulation.map {
    _.final_step
  }.max)

  try {
      writer.append("v_idn, v_ticks, v_seed, v_pop" + "\n")

      simulation.map {
        s =>
          println("seed = " + s.seed)
          s.pop.map {
            case (id, pop) =>
              //println(" / -> id = " + id +  " / pop = " + pop)
              writer.append(List[Any](id, s.final_step, s.seed, pop).map { _.toString }.mkString(",") + "\n")
          }
      }
  } finally writer.close

}



