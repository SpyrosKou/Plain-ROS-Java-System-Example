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

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Main executable class to run the examples against an already-running external roscore.
 *
 * <p>This entrypoint is useful when the reader already has a ROS master running outside Java and
 * only wants to connect the example topic, service, and action nodes to it.
 *
 * @author Spyros Koukas
 */
public final class MainExternal {
    /**
     * Environment variable that points to the external ROS master URI.
     */
    private static final String ROS_MASTER_URI = "ROS_MASTER_URI";

    /**
     * Environment variable that defines the local IP advertised by the rosjava nodes.
     */
    private static final String ROS_IP = "ROS_IP";

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
    private MainExternal() {
    }

    /**
     * Connect to the external ROS master described by {@code ROS_MASTER_URI} and {@code ROS_IP},
     * launch all examples, wait for the demo to run, and then shut down.
     *
     * @param args ignored command-line arguments
     * @throws Exception if startup or shutdown fails
     */
    public static final void main(final String[] args) throws Exception {
        // An executor is needed to spawn rosjava nodes from Java.
        final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        try {
            // Start the topic, service, and action examples against the external master.
            ExampleSystemNodes.start(
                    nodeMainExecutor,
                    requiredEnv(ROS_IP),
                    new URI(requiredEnv(ROS_MASTER_URI)),
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
    }

    /**
     * Read one required environment variable and fail fast if it is missing.
     *
     * @param name the environment variable name
     * @return the non-blank environment variable value
     */
    private static final String requiredEnv(final String name) {
        final String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " environment variable needs to be set.");
        }
        return value;
    }
}
