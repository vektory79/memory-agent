# Memory Agent

This agent for those cases in which the memory consumption of the Java programs is more 
undesirable, than performance of the application.

## Recommended JVM parameters

```
-Xminf0.1
-Xmaxf0.2
-XX:NewRatio=20
-XX:-UseAdaptiveSizePolicy
```

## To start javaagent with application
```
-javaagent:/path/to/agent/memory-agent.jar=warmLevel=8.0,gcPeriod=4
```

## To attach javaagent to the running application

```bash
memory-agent.sh <pid> [parameters]
```

Example:

```bash
memory-agent.sh 21987 warmLevel=8.0,gcPeriod=4
```

## Agent options

1. warmTimeout - timeout in seconds after the last seen CPU warm.
2. warmLevel - level in percents of the process CPU load, which accepted as the CPU warm.
3. gcPeriod - period in seconds for the consecutive GC invocation during the memory shrinking.
4. gcStopCounter - count of the GC after that GC is stop if the committed memory is not 
                   changed after the GC.
                   
All parameters are optional.
