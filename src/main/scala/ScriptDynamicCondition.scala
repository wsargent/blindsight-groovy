package com.tersesystems.blindsight.groovy

import com.tersesystems.blindsight.{Condition, Markers}
import org.slf4j.event.Level
import sourcecode.Enclosing
import sourcecode.File

import javax.script.{Invocable, ScriptEngine}

class ScriptDynamicCondition(engine: ScriptEngine, source: ConditionSource, enclosing: Enclosing, file: File) extends Condition {
  private val evaluationName = "evaluate"

  eval()

  private def eval(): Unit = {
    engine.eval(source.script)
  }

  override def apply(level: Level, markers: Markers): Boolean = try {
    if (source.isInvalid) {
      eval()
    }
    val result = engine.asInstanceOf[Invocable].invokeFunction(evaluationName, level, markers, enclosing, file)
    result.asInstanceOf[Boolean]
  } catch {
    case e: Exception =>
      //e.printStackTrace()
      false
  }
}
