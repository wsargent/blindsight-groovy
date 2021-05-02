package com.tersesystems.blindsight.groovy

import java.io.Reader

trait ConditionSource {

  def isInvalid: Boolean

  def getReader: Reader

}

