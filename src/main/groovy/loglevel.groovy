import com.tersesystems.logback.classic.ChangeLogLevel
import org.slf4j.event.Level

def changeLogger(String loggerName, Level level) {
    ChangeLogLevel changer = new ChangeLogLevel()
    changer.changeLogLevel(loggerName, level.toString())
}

def trace(String loggerName) {
    changeLogger(loggerName, Level.TRACE)
}

def debug(String loggerName) {
    changeLogger(loggerName, Level.DEBUG)
}

def info(String loggerName) {
    changeLogger(loggerName, Level.INFO)
}

def warn(String loggerName) {
    changeLogger(loggerName, Level.WARN)
}

def error(String loggerName) {
    changeLogger(loggerName, Level.ERROR)
}

// Easier to set log levels from groovy rather than XML
// because you can choose when to re-run it and can do things
// like enable debugging for 10 minutes
debug("com.tersesystems.blindsight.scripting")