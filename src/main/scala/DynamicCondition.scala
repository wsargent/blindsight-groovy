package com.tersesystems.blindsight.groovy

import com.tersesystems.blindsight.{Condition, Markers}
import org.slf4j.event.Level
import sourcecode.Enclosing
import sourcecode.File

import javax.script.{Invocable, ScriptEngine}

class DynamicCondition(engine: ScriptEngine, source: ConditionSource, enclosing: Enclosing, file: sourcecode.File) extends Condition {
  private val evaluationName = "evaluate"

  eval()

  protected def evaluate(level: Level, markers: Markers, enclosing: Enclosing, file: sourcecode.File): Boolean = try {
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

  private def eval(): Unit = {
    engine.eval(source.getReader)
  }

  override def apply(level: Level, markers: Markers): Boolean = evaluate(level, markers, enclosing, file)
}
