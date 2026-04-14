/**
 * Copyright 2020 Spyros Koukas
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.spyros.koukas.ros.examples;

import org.ros.RosCore;
import org.ros.exception.RosRuntimeException;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Main executable class that starts an embedded rosjava roscore and then launches all examples.
 *
 * <p>This is the standalone entrypoint of the tutorial. It is useful when the reader wants one
 * command that starts the ROS master and the topic, service, and action examples together.
 *
 * <p>An example that uses an external roscore is available in {@link MainExternal}.
 *
 * @author Spyros Koukas
 */
public final class Main {
    /**
     * Local host IP used by the embedded example setup.
     */
    private static final String ROS_HOST_IP = "127.0.0.1";

    /**
     * Standard ROS master port used by the embedded roscore.
     */
    private static final int ROS_MASTER_PORT = 11311;

    /**
     * How long to wait for the embedded roscore to start.
     */
    private static final long ROSCORE_START_TIMEOUT_MILLIS = 2_000;

    /**
     * How long to wait for the service server to register before starting the service client.
     */
    private static final long SERVICE_REGISTRATION_TIMEOUT_MILLIS = 15_000;

    /**
     * How long to keep the example nodes alive so the logs show topic, service, and action traffic.
     */
    private static final long DEMO_DURATION_MILLIS = 30_000;

    /**
     * Fibonacci order sent by the ActionLib client example.
     */
    private static final int ACTION_FIBONACCI_ORDER = 8;

    /**
     * Utility class. No instances are needed.
     */
    private Main() {
    }

    /**
     * Start an embedded roscore, launch all example nodes, wait for the demo to run, and then shut down.
     *
     * @param args ignored command-line arguments
     * @throws Exception if startup or shutdown fails
     */
    public static final void main(final String[] args) throws Exception {
        // Create a publicly visible rosjava ROS master bound to the standard ROS port.
        final RosCore rosCore = RosCore.newPublic(ROS_MASTER_PORT);

        // Start the embedded roscore.
        rosCore.start();
        try {
            // Before proceeding any further we need to make sure that the roscore is already started.
            if (!rosCore.awaitStart(ROSCORE_START_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                throw new RosRuntimeException("Timed out while waiting for roscore to start.");
            }

            // An executor is needed to spawn rosjava nodes from Java.
            final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
            try {
                // Start the topic, service, and action examples against the embedded master.
                ExampleSystemNodes.start(
                        nodeMainExecutor,
                        ROS_HOST_IP,
                        new URI("http://" + ROS_HOST_IP + ":" + ROS_MASTER_PORT),
                        ACTION_FIBONACCI_ORDER,
                        SERVICE_REGISTRATION_TIMEOUT_MILLIS,
                        TimeUnit.MILLISECONDS
                );

                // Keep the demo alive long enough to observe the node interaction in the logs.
                Thread.sleep(DEMO_DURATION_MILLIS);
            } finally {
                // Shutting down the executor stops all example nodes.
                nodeMainExecutor.shutdown();
            }
        } finally {
            // Shut down the embedded roscore as the final cleanup step.
            rosCore.shutdown();
        }
    }
}
