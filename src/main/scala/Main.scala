package com.tersesystems.blindsight.groovy

import com.tersesystems.blindsight.{Logger, LoggerFactory}

import java.nio.file.Paths

object Main {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  val cm = new ConditionManager(Paths.get("src/main/groovy/condition.groovy"), "groovy")

  def main(args: Array[String]): Unit = {
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
    logger.info.when(cm.condition()) { info =>
      info("Logging at a info level")
    }
  }

  def logInfoSpecial(): Unit = {
    logger.info.when(cm.condition()) { info =>
      info("Logging at a info level from special method")
    }
  }

  def logDebugSpecial(): Unit = {
    logger.debug.when(cm.condition()) { info =>
      info("Logging at a debug level from special method")
    }
  }

  def logDebug(): Unit = {
    logger.debug.when(cm.condition()) { handle =>
      handle("Logging at a debug level")
    }
  }

}
