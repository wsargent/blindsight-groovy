name := "blindsight-scripting"

version := "0.1"

scalaVersion := "2.13.5"

idePackagePrefix := Some("com.tersesystems.blindsight.scripting")

libraryDependencies += "com.tersesystems.blindsight" %% "blindsight-logstash" % "1.4.1"

libraryDependencies += "com.tersesystems.securitybuilder" % "securitybuilder" % "1.0.0"

// https://github.com/twineworks/tweakflow
libraryDependencies += "com.twineworks" % "tweakflow" % "1.3.2"

// The groovy script engine is only in groovy-jsr223 and we can't use the "indy" classifier
libraryDependencies += "org.codehaus.groovy" % "groovy-jsr223" % "3.0.8"
