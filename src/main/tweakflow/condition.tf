# https://twineworks.github.io/tweakflow/reference.html
#

library condition {

  # level: the result of org.slf4j.event.Level.toInt()
  # enclosing: <class>.<method> i.e. com.tersesystems.blindsight.groovy.Main.logDebugSpecial
  # line: line number of the source code where condition was created
  # file: absolute path of the file containing the condition
  #
  doc 'Evaluates a condition'
  function evaluate: (long level, string enclosing, long line, string file) ->
    false;
}
