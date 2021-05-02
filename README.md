# Dynamic Conditions with Groovy

This project demonstrates how to change logging conditions in a running JVM.

A groovy script is evaluated, then run every time.  If the groovy script changes, then the JVM picks it up and evaluates it without having to restart the JVM.

## Running

```
sbt run
```

And then edit `condition.groovy` to your preference.  

Hit Control-C to cancel the app.