package com.tersesystems.blindsight.groovy

import com.tersesystems.blindsight.Condition
import sourcecode.{Enclosing, Line}

import java.nio.file.Path

class TweakFlowConditionManager(path: Path) {

  private val fileConditionSource = new FileConditionSource(path)

  def condition()(implicit line: Line, enclosing: Enclosing, file: sourcecode.File): Condition = {
    new TweakFlowDynamicCondition(fileConditionSource, line, enclosing, file)
  }
}
