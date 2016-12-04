package ru.vektory79.memoryagent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by vektor on 04.12.16.
 */
public class MemoryTool {
    public static final String AGENT_LABEL_PROP_NAME = "me.vektory79.memory.agent.activated";

    public static void main(String[] args) throws IOException, URISyntaxException, AgentLoadException, AgentInitializationException {
        if (args.length > 0) {
            VirtualMachine machine = null;
            try {
                machine = VirtualMachine.attach(args[0]);
            } catch (AttachNotSupportedException e) {
                System.out.println("Attach not supported to the given process.");
                return;
            } catch (IOException e) {
                System.out.println("Error attaching to the given process.");
                return;
            }

            String agentLabel = machine.getSystemProperties().getProperty(AGENT_LABEL_PROP_NAME, "false");
            if ("true".equals(agentLabel)) {
                System.out.println("Agent already attached to the given process.");
                return;
            }

            StringBuilder params = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) {
                    params.append(',');
                }
                params.append(args[i].trim());
            }
            Path agentPath = Paths.get(MemoryAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            System.out.println("Agent path: " + agentPath.toString());
            machine.loadAgent(agentPath.toString(), params.toString());

            machine.detach();
        } else {
            System.out.println("Usage:");
            System.out.println("memory-agent.sh <pid> [agent params]");
        }
    }
}
