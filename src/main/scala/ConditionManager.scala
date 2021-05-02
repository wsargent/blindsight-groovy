package com.tersesystems.blindsight.groovy

import com.tersesystems.blindsight.Condition
import sourcecode.Enclosing

import java.nio.file.Paths
import javax.script.{ScriptEngine, ScriptEngineManager}

class ConditionManager {
  val factory = new ScriptEngineManager
  val engine: ScriptEngine = factory.getEngineByName("groovy")

  private val path = Paths.get("src/main/groovy/condition.groovy")
  private val fileConditionSource = new FileConditionSource(path)

  def condition()(implicit enclosing: Enclosing, file: sourcecode.File): Condition = {
    new DynamicCondition(engine, fileConditionSource, enclosing, file)
  }
}
