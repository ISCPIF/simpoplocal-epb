
This project contains the source files to reproduce the results of experiments on the Simpop Local model. This experiment is described in the paper:

  C. Schmitt, S. Rey,  R. Reuillon, D. Pumain, Half a billion simulations: Evolutionary algorithms and distributed computing for calibrating the SimpopLocal geographical model, submitted to EPB, under review, 2013.

Page project website : http://iscpif.github.io/simpoplocal-epb/

Licence
-------

This software is licenced under the GNU Affero GPLv3 free software licence. 

Usage (simulation)
------------------

To compile and run this project you need sbt 0.12 (http://www.scala-sbt.org/).

Go to the fitness directory.

`cd fitness`

To execute a single run: 

`sbt run`

To build and publish the OpenMoLE plugin:

`sbt publish-local`

Get the plugin in your local `~/.ivy2/` repository, for instance:

`/home/reuillon/.ivy2/local/fr.geocite.simpoplocal/exploration_2.10/1.0.0/bundles/exploration_2.10.jar`

We use OpenMoLE to describe and launch our experimentation.

> OpenMOLE (Open MOdeL Experiment) is a workflow engine designed to leverage the computing power of parallel execution environments for naturally parallel processes. A process is told naturally parallel if the same computation runs many times for a set of different inputs. OpenMOLE workflows are suitable for many types of naturally parallel processes such as model experiment, image processing, text analysis… It is distributed under the AGPLv3 free software license.

Description of OpenMoLE installation is described on www.openmole.org website.

You can find multiple other great tutorials and examples of other applications on same website.

To launch OpenMoLE in console mode and load the exploration jar : 

`openmole -c -p /path/to/exploration_2.10.jar`

Then you can use the workflows avialable in the openmole directory (it is compatible with OpenMoLE 0.9). Those workflows are configured to run on the biomed VO of the grid EGI, however switching the execution environment in OpenMoLE is easy so you can use this workflow on you own multi-core machine, cluster or grid virtual organisation (you can find examples of workflows in the tutorial section on the openmole website).

Usage (graphics)
----------------

You can find graphics scripts into R folder. 
You need R with `ggplot2` installed to run correctly this two scripts.

Graphics use csv file generated by `WriteResultGraph1.scala` and `WriteResultGraph2.scala`

If you want to add more replication or change the default output for result in the graphic 1, you need to modify directly the two lines into scala file `WriteResultGraph1.scala` : 

```scala
  val replications = 5
  val folderPath = "/tmp/"
```

If you want to add more replications, change the frequency of data writer, or change the default output for result in the graphic 2, you need to modify directly the two lines into scala file `WriteResultGraph2.scala` : 

```scala
  val replications = 5
  val folderPath = "/tmp/"
  val each = 10
```

To generate csv files, you need to `run sbt` and choose the correct option in the menu. After that, you can locate the csv file and modify into R folder the two `.sh` scripts :

To generate graphics 1 you need to modify the `graph1.sh` file  

To generate graphics 2 you need to modify the `graph2.sh` file  



