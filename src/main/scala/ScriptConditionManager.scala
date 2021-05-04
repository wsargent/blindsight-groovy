package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.Condition
import sourcecode.Enclosing

import java.nio.file.{Path, Paths}
import javax.script.{ScriptEngine, ScriptEngineManager}

class ScriptConditionManager(path: Path, engineName: String) {

  val factory = new ScriptEngineManager
  val engine: ScriptEngine = Option(factory.getEngineByName(engineName)) match {
    case Some(engine) => engine
    case None => throw new IllegalStateException(s"No engine found for $engineName")
  }

  private val fileConditionSource = new FileConditionSource(path, _ => true)

  def condition()(implicit enclosing: Enclosing, file: sourcecode.File): Condition = {
    new ScriptDynamicCondition(engine, fileConditionSource, enclosing, file)
  }
}
