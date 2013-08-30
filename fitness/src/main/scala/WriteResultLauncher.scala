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

package fr.geocite.simpoplocal.exploration

import fr.geocite.simpoplocal.exploration.writer.CSVWriter
import scala.util.Random
import java.io.{File, FileWriter, BufferedWriter}

object WriteResultLauncher extends App {

  val replications = 100
  val folderPath = "/tmp/"

  val m = new SimpopLocal {
    def distanceDecay: Double = 0.6948037684879382
    def innovationImpact: Double = 0.008514548363730353
    def maxInnovation: Double = 10000
    def pCreation: Double = 8.672482701792705E-7
    def pDiffusion: Double = 8.672482701792705E-7
    def rMax: Double = 10586
  }

  val rng = new Random(42)

  val fitnesses = Iterator.continually(rng.nextLong).take(replications).toSeq.par.map {
    seed =>
      val file = folderPath + "slocal_" + seed.toString() + ".csv"
      implicit val threadRng = new Random(seed)
      new CSVWriter(file,0,seed,50).apply(m)(threadRng)

  }

}
