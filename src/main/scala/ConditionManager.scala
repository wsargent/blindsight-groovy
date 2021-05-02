package com.tersesystems.blindsight.groovy

import com.tersesystems.blindsight.Condition
import sourcecode.Enclosing

import java.nio.file.{Path, Paths}
import javax.script.{ScriptEngine, ScriptEngineManager}

class ConditionManager(path: Path, engineName: String) {

  val factory = new ScriptEngineManager
  val engine: ScriptEngine = Option(factory.getEngineByName(engineName)) match {
    case Some(engine) => engine
    case None => throw new IllegalStateException(s"No engine found for $engineName")
  }

  private val fileConditionSource = new FileConditionSource(path)

  def condition()(implicit enclosing: Enclosing, file: sourcecode.File): Condition = {
    new DynamicCondition(engine, fileConditionSource, enclosing, file)
  }
}
