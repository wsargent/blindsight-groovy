package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.{Condition, Markers}
import com.twineworks.tweakflow.lang.TweakFlow
import com.twineworks.tweakflow.lang.load.loadpath.{LoadPath, MemoryLocation}
import com.twineworks.tweakflow.lang.runtime.Runtime
import com.twineworks.tweakflow.lang.values.Values
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import java.util.concurrent.atomic.AtomicReference

class TweakFlowDynamicCondition(source: ConditionSource, line: Line, enclosing: Enclosing, file: File) extends Condition {

  private val m: AtomicReference[Runtime.Module] = new AtomicReference[Runtime.Module]()

  eval()

  private def compileModule(script: String): Runtime.Module = {
    val memLocation = new MemoryLocation.Builder().add("condition.tf", script).build
    val loadPath = new LoadPath.Builder().addStdLocation().add(memLocation).build()
    val runtime = TweakFlow.compile(loadPath, "condition.tf")
    runtime.getModules.get(runtime.unitKey("condition.tf"))
  }

  def eval(): Unit = {
    val module = compileModule(source.script)
    module.evaluate()
    m.set(module)
  }

  override def apply(level: Level, markers: Markers): Boolean = {
    if (source.isInvalid) {
      eval()
    }

    try {
      val callSite = m.get().getLibrary("condition").getVar("evaluate")
      val result = callSite.call(
        Values.make(level.toInt),
        Values.make(enclosing.value),
        Values.make(line.value),
        Values.make(file.value)
      ).bool()
      result
    } catch {
      case e: Exception =>
        //e.printStackTrace()
        false
    }
  }
}
