# Dynamic Conditions with Groovy

This project demonstrates how to change logging conditions in a running JVM, using Blindsight.

A condition has to return `Boolean` but is intentionally left open so that anything can be a condition.  This means that we can tie a condition to a
[JSR 223 script](https://docs.oracle.com/en/java/javase/12/scripting/java-scripting-api.html#GUID-C4A6EB7C-0AEA-45EC-8662-099BDEFC361A).
In this example, we'll use [Groovy](http://docs.groovy-lang.org/docs/latest/html/documentation/#jsr223) to evaluate a condition and return a boolean.  If the groovy script changes, then the JVM picks it up and evaluates it without having to restart the JVM.

## Main

The main program runs a loop that conditionally logs various statements:

```scala
object Main {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  val cm = new ConditionManager(Paths.get("src/main/groovy/condition.groovy"), "groovy")

  def main(args: Array[String]): Unit = {
    // Run from a loop
    while (true) {
      logInfo()
      logDebug()
      logInfoSpecial()
      logDebugSpecial()

      Thread.sleep(1000L)
    }
  }

  def logInfo(): Unit = {
    logger.info.when(cm.condition()) { info =>
      info("Logging at a info level")
    }
  }

  def logInfoSpecial(): Unit = {
    logger.info.when(cm.condition()) { info =>
      info("Logging at a info level from special method")
    }
  }

  def logDebugSpecial(): Unit = {
    logger.debug.when(cm.condition()) { info =>
      info("Logging at a debug level from special method")
    }
  }

  def logDebug(): Unit = {
    logger.debug.when(cm.condition()) { handle =>
      handle("Logging at a debug level")
    }
  }

}
```

## Groovy Script

There's a single Groovy script, `condition.groovy`.  It runs a single method, which returns a boolean indicating whether logging should happen or not. 

```groovy
import com.tersesystems.blindsight.Markers
import org.slf4j.event.Level
import sourcecode.Enclosing
import sourcecode.File

boolean evaluate(Level level, Markers markers, Enclosing enclosing, File file) {
    // We like this debug message so we want it to show up
    var enc = enclosing.value()
    if (enc == "com.tersesystems.blindsight.groovy.Main.logDebugSpecial") {
        return true;
    }

    // We don't like this info message
    if (enc == "com.tersesystems.blindsight.groovy.Main.logInfoSpecial") {
        return false;
    }

    // Otherwise we'll just use info level.
    return (level.toInt() >= Level.INFO.toInt())
}
```

The `ConditionManager` and the `FileConditionSource` keep track of the file's last modification time.  If the file's been modified since last scene, then the script is evaluated again, and the new script is used.

The `FileConditionSource` is a trivial example for the purposes of demonstration, and a JDBC `ConditionSource` or Redis `ConditionSource` could also be used to pull updated script information.

## Running

```
sbt run
```

And then edit `condition.groovy` to your preference.  

Hit Control-C to cancel the app.