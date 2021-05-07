package com.tersesystems.blindsight.scripting
package tweakflow

import com.tersesystems.blindsight.{Condition, Markers}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

class TweakFlowDynamicCondition(manager: TweakFlowConditionManager, line: Line, enclosing: Enclosing, file: File) extends Condition {

  override def apply(level: Level, markers: Markers): Boolean = {
    manager.execute(level, enclosing, line, file)
  }
}
