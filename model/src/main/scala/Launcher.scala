import scala.util.Random

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

object Launcher extends App {

  val m = new SimpopLocal {
    def distanceDecay: Double = 0.695
    def innovationImpact: Double = 0.0085
    def maxInnovation: Double = 10000
    def pCreation: Double = 8.67E-07
    def pDiffusion: Double = 8.67E-07
    def rMax: Double = 10586
  }

  implicit val rng = new Random(42)

  println(m.run)

}
