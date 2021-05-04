package com.tersesystems.blindsight.groovy

trait ConditionSource {

  def isInvalid: Boolean

  def script: String
}

