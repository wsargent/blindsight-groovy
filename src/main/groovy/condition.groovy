import com.tersesystems.blindsight.Markers
import org.slf4j.event.Level
import sourcecode.Enclosing
import sourcecode.File

boolean evaluate(Level level, Markers markers, Enclosing enclosing, File file) {
    if (enclosing.value() == "com.tersesystems.blindsight.groovy.Main.logDebugSpecial") {
        return true;
    }

    if (enclosing.value() == "com.tersesystems.blindsight.groovy.Main.logInfoSpecial") {
        return false;
    }

    return (level.toInt() >= Level.INFO.toInt())
}
