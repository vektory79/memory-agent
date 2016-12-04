package ru.vektory79.memoryagent;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The memory management agent for forcing the Java heap memory compaction.
 *
 * Created by vektor on 03.12.16.
 */
public class MemoryAgent {
    private static final Logger LOG = Logger.getLogger(MemoryAgent.class.getName());

    private static int warmTimeout = 10;
    private static double warmLevel = 10.0;
    private static int gcPeriod = 2;
    private static int gcStopCounter = 5;

    private MemoryAgent() {

    }

    @SuppressWarnings("WeakerAccess")
    public static void premain(String args) {
        analyseArgs(args);

        Thread monitoringThread = new Thread(MemoryAgent::monitoring);
        monitoringThread.setDaemon(true);
        monitoringThread.setName("Memoty Management Agent");
        monitoringThread.start();
    }

    @SuppressWarnings({"squid:S2189", "squid:S1215", "InfiniteLoopStatement"})
    private static void monitoring() {
        try {
            System.setProperty(MemoryTool.AGENT_LABEL_PROP_NAME, "true");

            OperatingSystemMXBean operatingSystemBean = ManagementFactory.getOperatingSystemMXBean();
            final Method processLoadMethod = operatingSystemBean.getClass().getMethod("getProcessCpuLoad");
            processLoadMethod.setAccessible(true);

            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            boolean gcOn = true;
            int waitCounter = warmTimeout;
            int stopCounter = gcStopCounter;
            while (true) {
                Thread.sleep(1000);
                double value = (double) processLoadMethod.invoke(operatingSystemBean) * 100.0;
                if (value < warmLevel) {
                    waitCounter = waitCounter > 0 ? waitCounter - 1 : 0;
                } else {
                    waitCounter = warmTimeout;
                    gcOn = true;
                    stopCounter = gcStopCounter;
                }

                if (gcOn && waitCounter == 0) {
                    long oldCommited = memoryBean.getHeapMemoryUsage().getCommitted();
                    System.gc();
                    waitCounter = gcPeriod;

                    if (oldCommited == memoryBean.getHeapMemoryUsage().getCommitted()) {
                        stopCounter = stopCounter > 0 ? stopCounter - 1 : 0;
                    } else {
                        stopCounter = gcStopCounter;
                    }

                    if (stopCounter == 0) {
                        gcOn = false;
                    }
                }
            }
        } catch (IllegalArgumentException | SecurityException | NoSuchMethodException e) {
            LOG.log(Level.WARNING, "Error initialize the Memory Agent", e);
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Interrapting the Memory Agent monitoring thread.");
            Thread.currentThread().interrupt();
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.log(Level.WARNING, "Error accessing the CPU JMX metrics. Do exit monitoring.", e);
        } finally {
            System.setProperty(MemoryTool.AGENT_LABEL_PROP_NAME, "false");
        }
    }

    @SuppressWarnings("unused")
    public static void agentmain(String args) {
        premain(args);
    }

    private static void analyseArgs(String args) {
        if (args == null) {
            return;
        }
        String[] argsArray = args.split(",");
        for (String arg : argsArray) {
            String[] argPair = arg.split("=");
            if (argPair.length == 2) {
                setParam(argPair[0], argPair[1]);
            }
        }
    }

    private static void setParam(String name, String value) {
        switch(name) {
            case "warmTimeout":
                warmTimeout = Integer.parseInt(value);
                break;
            case "warmLevel":
                warmLevel = Double.parseDouble(value);
                break;
            case "gcPeriod":
                gcPeriod = Integer.parseInt(value);
                break;
            case "gcStopCounter":
                gcStopCounter = Integer.parseInt(value);
                break;
            default:
        }
    }
}
