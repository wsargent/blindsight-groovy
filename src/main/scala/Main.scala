package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.LoggerFactory

import java.nio.file.{Files, Paths}
import javax.script.{ScriptEngine, ScriptEngineManager}

object Main {

  val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    setLogLevelsFromScript()

    // Run from a loop
    while (true) {
      logInfo()
      logDebug()
      logInfoSpecial()
      logDebugSpecial()

      Thread.sleep(1000L)
    }
  }

  def logInfo(): Unit = {
    logger.info("Logging at a info level")
  }

  def logInfoSpecial(): Unit = {
    logger.info("Logging at a info level from special method")
  }

  def logDebugSpecial(): Unit = {
    logger.debug("Logging at a debug level from special method")
  }

  def logDebug(): Unit = {
    logger.debug("Logging at a debug level")
  }

  private def setLogLevelsFromScript(): Unit = {
    val groovyScriptEngine = {
      val engineName = "groovy"
      val factory = new ScriptEngineManager
      val engine: ScriptEngine = Option(factory.getEngineByName(engineName)) match {
        case Some(engine) => engine
        case None => throw new IllegalStateException(s"No engine found for $engineName")
      }
      engine
    }

    val path = Paths.get("src/main/groovy/loglevel.groovy")
    val value = Files.readString(path)
    groovyScriptEngine.eval(value)
  }

}
