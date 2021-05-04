package com.tersesystems.blindsight.groovy

import com.twineworks.tweakflow.lang.TweakFlow
import com.twineworks.tweakflow.lang.load.loadpath.{LoadPath, MemoryLocation}
import com.twineworks.tweakflow.lang.values.{DateTimeValue, Values}

import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime

class TweakFlowConditionManager {


}

object TweakFlowConditionManager {
  private def compileModule(moduleText: String) = {
    val memLocation = new MemoryLocation.Builder().add("condition.tf", moduleText).build
    val loadPath = new LoadPath.Builder().addStdLocation().add(memLocation).build()
    val runtime = TweakFlow.compile(loadPath, "condition.tf")
    runtime.getModules.get(runtime.unitKey("condition.tf"))
  }

  def main(args: Array[String]) = {
    val path = Paths.get("src/main/tweakflow/condition.tf")
    val inputString = Files.readString(path)
    val m = compileModule(inputString)
    val format = m.getLibrary("condition").getVar("evaluate")
    m.evaluate()
    val level = Values.make(1)
    val enclosing = Values.make("")
    val file = Values.make("")
    val line = Values.make(1)
    val result = format.call(level, enclosing, line, file)
    System.out.println("var call: " + result.bool())
  }
}