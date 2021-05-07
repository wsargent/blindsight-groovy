package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.LoggerFactory
import com.tersesystems.blindsight.scripting.tweakflow.TweakFlowConditionManager
import com.tersesystems.securitybuilder.MacBuilder

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import javax.script.{ScriptEngine, ScriptEngineManager}

object Main {

  // Secret passphrase that is never passed around in the clear, so an attacker
  // can't generate a valid signature :-)
  private val privateString = "very secret key"

  val logger: ScriptAwareLogger = LoggerFactory.getLogger(getClass).asInstanceOf[ScriptAwareLogger]

  val scriptFile: Path = Paths.get("src/main/tweakflow/condition.tf")

  //val cm = new ScriptConditionManager(Paths.get("src/main/groovy/condition.groovy"), "groovy")
  // you can disable the verifier by setting input => true
  val cm = new TweakFlowConditionManager(scriptFile, input => true)
  //val cm = new TweakFlowConditionManager(scriptFile, input => verify(input))

  def main(args: Array[String]): Unit = {
    setLogLevelsFromScript()

    // Uncomment this to start signing the script on program start
    // sign()

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
    logger.debug("Logging at a debug level from special method")
  }

  def logDebug(): Unit = {
    logger.debug.when(cm.condition()) { debug =>
      debug("Logging at a debug level")
    }
  }

  def sign(): Unit = {
    val signatureFile = scriptFile.getParent.resolve("condition.tf.asc")

    // Clean out any preexisting file
    Files.deleteIfExists(signatureFile)

    // Regenerate the MAC (you would put this somewhere that people can't touch it usually, so an
    // attacker can't swap out the script and the MAC at once)
    val contents = Files.readString(scriptFile)
    val sha256Mac = MacBuilder.builder.withHmacSHA256().withString(privateString).build
    val signature = byteArrayToHex(sha256Mac.doFinal(contents.getBytes(StandardCharsets.UTF_8)))
    Files.writeString(signatureFile, signature)
  }

  // This method is called on file modification to verify the contents match the signature
  def verify(contents: String): Boolean = {
    val sha256Mac = MacBuilder.builder.withHmacSHA256().withString(privateString).build
    val actual = sha256Mac.doFinal(contents.getBytes(StandardCharsets.UTF_8))

    val path = Paths.get("src/main/tweakflow/condition.tf")
    val signatureFile = path.getParent.resolve("condition.tf.asc")
    val signature = hexToByteArray(Files.readString(signatureFile))

    MessageDigest.isEqual(actual, signature)
  }

  def byteArrayToHex(a: Array[Byte]): String = {
    val sb = new StringBuilder(a.length * 2)
    for (b <- a) {
      sb.append(String.format("%02x", b))
    }
    sb.toString
  }

  def hexToByteArray(s: String): Array[Byte] = {
    val len = s.length
    val data = new Array[Byte](len / 2)
    var i = 0
    while (i < len) {
      data(i / 2) = ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)).toByte
      i += 2
    }
    data
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
