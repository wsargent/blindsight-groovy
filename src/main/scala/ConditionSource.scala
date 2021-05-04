package com.tersesystems.blindsight.scripting

trait ConditionSource {

  def isInvalid: Boolean

  def script: String
}

