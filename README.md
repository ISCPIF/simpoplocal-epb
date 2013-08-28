This project contains the source files to reproduce the results of experiments on the Simpop Local model.

Licence
-------

This software is licenced under the GNU Affero GPLv3 software licence. 

Usage
-----

To compile and run this project you need sbt 0.12 (http://www.scala-sbt.org/).

Go to the fitness directory.

`cd fitness`

To execute a single run: 

`sbt run`

To build and publish the OpenMoLE plugin:

`sbt publish-local`

Get the plugin in your local `~/.ivy2/` repository, for instance:

`/home/reuillon/.ivy2/local/fr.geocite.simpoplocal/exploration_2.10/1.0.0/bundles/exploration_2.10.jar.`

To use it in OpenMoLE (www.openmole.org) launch OpenMoLE with: 

`openmole -c -p /path/to/exploration_2.10.jar. `

Then you can use the workflow available in the workflow directory (it is compatible with OpenMoLE 0.9). 
This workflow is configured to run on the biomed VO of the grid EGI, however switching the execution environment in OpenMoLE is easy so you can use this workflow on you own multi-core machine, cluster or grid virtual organisation.

