organization := "fr.geocite.simpoplocal"

name := "exploration"

version := "1.0.0"

scalaVersion := "2.10.2"

libraryDependencies += "ca.umontreal.iro" % "ssj" % "2.5" excludeAll(
  ExclusionRule(organization = "dsol"), 
  ExclusionRule(organization = "jfree"), 
  ExclusionRule(organization = "org.apache.mahout"))

resolvers += "ISC-PIF" at "http://maven.iscpif.fr/public"

osgiSettings

OsgiKeys.exportPackage := Seq("fr.geocite.simpoplocal.*")

OsgiKeys.importPackage := Seq("*;resolution:=optional")

OsgiKeys.privatePackage := Seq("!scala.*", "*")

