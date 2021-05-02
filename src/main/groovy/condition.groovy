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
