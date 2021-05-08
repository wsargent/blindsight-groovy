package com.tersesystems.blindsight.scripting
package groovy

import com.tersesystems.blindsight.{Condition, Markers}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File}

import java.nio.file.Path
import javax.script.{Invocable, ScriptEngine, ScriptEngineManager}

class ScriptConditionManager(path: Path, engineName: String) {
  val factory = new ScriptEngineManager
  val engine: ScriptEngine = Option(factory.getEngineByName(engineName)) match {
    case Some(engine) => engine
    case None => throw new IllegalStateException(s"No engine found for $engineName")
  }

  private val source = new FileConditionSource(path, _ => true)

  def condition()(implicit enclosing: Enclosing, file: sourcecode.File): Condition = {
    new ScriptDynamicCondition(enclosing, file)
  }

  class ScriptDynamicCondition(enclosing: Enclosing, file: File) extends Condition {
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
}

