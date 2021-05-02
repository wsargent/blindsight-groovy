name := "blindsight-groovy"

version := "0.1"

scalaVersion := "2.13.5"

idePackagePrefix := Some("com.tersesystems.blindsight.groovy")

sourceDirectories in Compile := Seq(file("groovy"))

libraryDependencies += "com.tersesystems.blindsight" %% "blindsight-logstash" % "1.4.1"
libraryDependencies += "org.codehaus.groovy" % "groovy-all" % "3.0.5"
