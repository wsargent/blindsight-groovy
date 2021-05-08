package com.tersesystems.blindsight.scripting
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.{CoreLogger, CorePredicate, SourceInfoBehavior}
import com.tersesystems.blindsight.flow.FlowLogger
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.logstash.LogstashLoggerFactory
import com.tersesystems.blindsight.scripting.tweakflow.TweakFlowConditionManager
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j.{SLF4JLogger, SLF4JLoggerAPI, StrictSLF4JMethod, UncheckedSLF4JMethod}
import com.tersesystems.securitybuilder.MacBuilder
import org.slf4j.event.Level
import org.slf4j.event.Level.{DEBUG, ERROR, INFO, TRACE, WARN}
import sourcecode.{Enclosing, File, Line}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest

/**
 *
 */
class ScriptingLoggerFactory extends LoggerFactory {

  // Secret passphrase that is never passed around in the clear, so an attacker
  // can't generate a valid signature :-)
  private val privateString = "very secret key"

  private val scriptFile: Path = Paths.get("src/main/tweakflow/condition.tf")

  // Uncomment this to start signing the script on program start
  // sign()

  //val cm = new ScriptConditionManager(Paths.get("src/main/groovy/condition.groovy"), "groovy")
  // you can disable the verifier by setting input => true
  private val cm = new TweakFlowConditionManager(scriptFile, input => true)

  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new ScriptAwareLogger(CoreLogger(underlying, sourceInfoBehavior(underlying)), cm)
  }

  private def sourceInfoBehavior(underlying: org.slf4j.Logger): Option[SourceInfoBehavior] = {
    if (sourceInfoEnabled(underlying)) {
      Some(sourceInfoAsMarker(underlying))
    } else {
      None
    }
  }

  private def sourceInfoEnabled(underlying: org.slf4j.Logger): Boolean = {
    val enabled = property(underlying, LogstashLoggerFactory.SourceEnabledProperty)
    java.lang.Boolean.parseBoolean(enabled.getOrElse(java.lang.Boolean.FALSE.toString))
  }

  private def property(underlying: org.slf4j.Logger, propertyName: String): Option[String] = {
    val logbackLogger = underlying.asInstanceOf[ch.qos.logback.classic.Logger]
    Option(logbackLogger.getLoggerContext.getProperty(propertyName))
  }

  def sourceInfoAsMarker(underlying: org.slf4j.Logger): SourceInfoBehavior = {
    import com.tersesystems.blindsight.logstash.LogstashLoggerFactory._
    val fileLabel      = property(underlying, SourceFileProperty).getOrElse("source.file")
    val lineLabel      = property(underlying, SourceLineProperty).getOrElse("source.line")
    val enclosingLabel = property(underlying, SourceEnclosingProperty).getOrElse("source.enclosing")
    new SourceInfoBehavior.Impl(fileLabel, lineLabel, enclosingLabel)
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

}

/**
 * "top level" logger that lets us call logger.fluent, logger.flow etc.
 */
class ScriptAwareLogger(core: CoreLogger, cm: TweakFlowConditionManager)
  extends Logger
    with SLF4JLoggerAPI.Proxy[CorePredicate, StrictSLF4JMethod] {

  override type Parent = SLF4JLogger[StrictSLF4JMethod]
  override type Self   = Logger

  override protected val logger = new ScriptAwareSLF4JLogger(core, cm)

  override def strict: SLF4JLogger[StrictSLF4JMethod] = logger

  override def markers: Markers = core.markers

  override def underlying: org.slf4j.Logger = core.underlying

  override lazy val unchecked: SLF4JLogger[UncheckedSLF4JMethod] = {
    new SLF4JLogger.Unchecked(core)
  }

  override lazy val flow: FlowLogger = {
    new FlowLogger.Impl(core)
  }

  override lazy val fluent: FluentLogger = {
    new FluentLogger.Impl(core)
  }

  override def semantic[StatementType: NotNothing]: SemanticLogger[StatementType] = {
    new SemanticLogger.Impl[StatementType](core)
  }

  override def withCondition(condition: Condition): Self = {
    new ScriptAwareLogger(core.withCondition(condition), cm)
  }

  override def withMarker[T: ToMarkers](markerInstance: T): Self = {
    new ScriptAwareLogger(core.withMarker(markerInstance), cm)
  }

  override def withEntryTransform(
                                   level: Level,
                                   f: Entry => Entry
                                 ): Self = {
    new ScriptAwareLogger(core.withEntryTransform(level, f), cm)
  }

  override def withEntryTransform(f: Entry => Entry): Self =
    new ScriptAwareLogger(core.withEntryTransform(f), cm)

  override def withEventBuffer(buffer: EventBuffer): Self =
    new ScriptAwareLogger(core.withEventBuffer(buffer), cm)

  override def withEventBuffer(level: Level, buffer: EventBuffer): Self =
    new ScriptAwareLogger(core.withEventBuffer(level, buffer), cm)
}

/**
 * Logger that implements the SLF4J API with trace/error/level/warn etc.
 *
 * @param core
 */
class ScriptAwareSLF4JLogger(private val core: CoreLogger, cm: TweakFlowConditionManager) extends SLF4JLogger.Strict(core) {
  override def withEntryTransform(level: Level, f: Entry => Entry): Self =
    new ScriptAwareSLF4JLogger(core.withEntryTransform(level, f), cm)

  override def withEntryTransform(f: Entry => Entry): Self =
    new ScriptAwareSLF4JLogger(core.withEntryTransform(f), cm)

  override def withEventBuffer(buffer: EventBuffer): Self =
    new ScriptAwareSLF4JLogger(core.withEventBuffer(buffer), cm)

  override def withEventBuffer(level: Level, buffer: EventBuffer): Self =
    new ScriptAwareSLF4JLogger(core.withEventBuffer(level, buffer), cm)

  override def withCondition(condition: Condition): Self =
    new ScriptAwareSLF4JLogger(core.withCondition(condition), cm)

  override def withMarker[T: ToMarkers](instance: T): Self =
    new ScriptAwareSLF4JLogger(core.withMarker(instance), cm)

  override val trace: Method = new ScriptAwareStrictSLF4JMethod.Impl(TRACE, core, cm)
  override val debug: Method = new ScriptAwareStrictSLF4JMethod.Impl(DEBUG, core, cm)
  override val info: Method = new ScriptAwareStrictSLF4JMethod.Impl(INFO, core, cm)
  override val warn: Method = new ScriptAwareStrictSLF4JMethod.Impl(WARN, core, cm)
  override val error: Method = new ScriptAwareStrictSLF4JMethod.Impl(ERROR, core, cm)
}

/**
 * Method parameters of the SLF4JLogger.
 *
 * This is where we put source code level script modifications.
 */
object ScriptAwareStrictSLF4JMethod {

  class Impl(level: Level, core: CoreLogger, cm: TweakFlowConditionManager) extends StrictSLF4JMethod.Impl(level, core) {
    import parameterList._

    def isEnabled(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
      cm.execute(level, enclosing, line, file)
    }

    override def apply(
                        st: Statement
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate() && isEnabled) parameterList.executeStatement(st)
    }

    override def apply(
                        msg: Message
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate()&& isEnabled) {
        message(msg.toString)
      }
    }

    override def apply(
                        throwable: Throwable
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate() && isEnabled) {
        messageArg1("", throwable)
      }
    }

    override def apply[A: ToArgument](
                                       message: Message,
                                       arg: A
                                     )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate() && isEnabled) {
        messageArg1(message.toString, Argument(arg).value)
      }
    }

    override def apply(
                        message: Message,
                        throwable: Throwable
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate() && isEnabled) {
        messageArg1(message.toString, throwable)
      }
    }

    override def apply[A: ToArgument](
                                       message: Message,
                                       arg: A,
                                       throwable: Throwable
                                     )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate() && isEnabled) {
        messageArg1Arg2(
          message.toString,
          Argument(arg).value,
          throwable
        )
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
                                                        message: Message,
                                                        arg1: A1,
                                                        arg2: A2
                                                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate()  && isEnabled) {
        messageArg1Arg2(
          message.toString,
          Argument(arg1).value,
          Argument(arg2).value
        )
      }
    }

    override def apply(
                        message: Message,
                        args: Arguments
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate() && isEnabled) {
        messageArgs(message.toString, args.toArray)
      }
    }

    override def apply(
                        message: Message,
                        args: Arguments,
                        throwable: Throwable
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate()  && isEnabled) {
        messageArgs(message.toString, args.toArray :+ throwable)
      }
    }

    override def apply(
                        markers: Markers
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker)  && isEnabled) {
        markerMessage(markers.marker, "")
      }
    }

    override def apply(
                        markers: Markers,
                        throwable: Throwable
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker)  && isEnabled) {
        markerMessageArg1(markers.marker, "", throwable)
      }
    }

    override def apply(
                        markers: Markers,
                        message1: Message
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker) && isEnabled) {
        markerMessage(markers.marker, message1.toString)
      }
    }

    override def apply[A: ToArgument](
                                       markers: Markers,
                                       message1: Message,
                                       arg: A
                                     )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker) && isEnabled) {
        markerMessageArg1(markers.marker, message1.toString, Argument(arg).value)
      }
    }

    override def apply(
                        markers: Markers,
                        message: Message,
                        throwable: Throwable
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker) && isEnabled) {
        markerMessageArg1(markers.marker, message.toString, throwable)
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
                                                        markers: Markers,
                                                        message: Message,
                                                        arg1: A1,
                                                        arg2: A2
                                                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker)  && isEnabled) {
        markerMessageArg1Arg2(
          markers.marker,
          message.toString,
          Argument(arg1).value,
          Argument(arg2).value
        )
      }
    }

    override def apply[A: ToArgument](
                                       markers: Markers,
                                       message: Message,
                                       arg: A,
                                       throwable: Throwable
                                     )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker) && isEnabled) {
        markerMessageArg1Arg2(
          markers.marker,
          message.toString,
          Argument(arg).value,
          throwable
        )
      }
    }

    override def apply(
                        markers: Markers,
                        message: Message,
                        args: Arguments
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker) && isEnabled) {
        markerMessageArgs(markers.marker, message.toString, args.toArray)
      }
    }

    override def apply(
                        markers: Markers,
                        message: Message,
                        args: Arguments,
                        throwable: Throwable
                      )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (executePredicate(markers.marker) && isEnabled) {
        markerMessageArgs(markers.marker, message.toString, args.toArray :+ throwable)
      }
    }

  }

}

