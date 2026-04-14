/**
 * Copyright 2026 Spyros Koukas
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

import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Shared launcher for the example ROS nodes used by both {@link Main} and {@link MainExternal}.
 *
 * <p>The goal of this helper is to keep the entrypoints small while still making the startup order
 * explicit and deterministic for the tutorial.
 *
 * <p>All example nodes are started here so the standalone and external-roscore entrypoints always
 * run the same topic, service, and action demonstrations.
 */
final class ExampleSystemNodes {
    /**
     * Service example graph names.
     */
    private static final String SERVICE_SERVER_NODE_NAME = "/spyros/test/server/";
    private static final String SERVICE_CLIENT_NODE_NAME = "/spyros/test/client/";
    private static final String SERVICE_NAME = "/spyros/test/service/sum";

    /**
     * Action example graph names.
     */
    private static final String ACTION_SERVER_NODE_NAME = "/spyros/test/action/server/";
    private static final String ACTION_CLIENT_NODE_NAME = "/spyros/test/action/client/";
    private static final String ACTION_NAME = "/spyros/test/action/fibonacci";

    /**
     * Topic example graph names.
     */
    private static final String PUBLISHER_NODE_NAME = "/spyros/test/publisher/";
    private static final String SUBSCRIBER_NODE_NAME = "/spyros/test/subscriber/";
    private static final String TOPIC_NAME = "/spyros/test/topic/";

    /**
     * Utility class. No instances are needed.
     */
    private ExampleSystemNodes() {
    }

    /**
     * Start all example nodes with the provided ROS master configuration.
     *
     * <p>The service server starts first and is allowed to register before the client starts.
     * This avoids an unnecessary startup race in the service example.
     *
     * @param nodeMainExecutor            the rosjava executor used to run all nodes
     * @param rosHostIp                   the local IP address advertised by the nodes
     * @param rosMasterUri                the URI of the ROS master
     * @param actionFibonacciOrder        the Fibonacci order used by the ActionLib client
     * @param serviceRegistrationTimeout  how long to wait for service registration
     * @param timeUnit                    unit of the registration timeout
     * @throws InterruptedException if the waiting thread is interrupted
     */
    static final void start(
            final NodeMainExecutor nodeMainExecutor,
            final String rosHostIp,
            final URI rosMasterUri,
            final int actionFibonacciOrder,
            final long serviceRegistrationTimeout,
            final TimeUnit timeUnit) throws InterruptedException {
        // Start the service server first so the service client can find it reliably.
        final ROSJavaServerNodeMain serviceServerNodeMain = new ROSJavaServerNodeMain(SERVICE_NAME, SERVICE_SERVER_NODE_NAME);
        execute(nodeMainExecutor, serviceServerNodeMain, rosHostIp, SERVICE_SERVER_NODE_NAME, rosMasterUri);

        // Wait until the ROS master confirms service registration.
        if (!serviceServerNodeMain.awaitRegistration(serviceRegistrationTimeout, timeUnit)) {
            throw new RosRuntimeException("Timed out while waiting for service server registration.");
        }

        // Start the remaining examples. They all share the same ROS master and advertised host.
        execute(nodeMainExecutor, new ROSJavaClientNodeMain(SERVICE_NAME, SERVICE_CLIENT_NODE_NAME), rosHostIp, SERVICE_CLIENT_NODE_NAME, rosMasterUri);
        execute(nodeMainExecutor, new ROSJavaActionServerNodeMain(ACTION_NAME, ACTION_SERVER_NODE_NAME), rosHostIp, ACTION_SERVER_NODE_NAME, rosMasterUri);
        execute(nodeMainExecutor, new ROSJavaActionClientNodeMain(ACTION_NAME, ACTION_CLIENT_NODE_NAME, actionFibonacciOrder), rosHostIp, ACTION_CLIENT_NODE_NAME, rosMasterUri);
        execute(nodeMainExecutor, new ROSJavaPublisherNodeMain(TOPIC_NAME, PUBLISHER_NODE_NAME), rosHostIp, PUBLISHER_NODE_NAME, rosMasterUri);
        execute(nodeMainExecutor, new ROSJavaSubscriberNodeMain(TOPIC_NAME, SUBSCRIBER_NODE_NAME), rosHostIp, SUBSCRIBER_NODE_NAME, rosMasterUri);
    }

    /**
     * Execute one {@link NodeMain} with a fresh {@link NodeConfiguration}.
     *
     * <p>The configuration binds together the node name, advertised host IP, and ROS master URI.
     *
     * @param nodeMainExecutor the rosjava executor
     * @param nodeMain         the node to execute
     * @param rosHostIp        the advertised host IP
     * @param nodeName         the ROS graph name of the node
     * @param rosMasterUri     the URI of the ROS master
     */
    private static final void execute(
            final NodeMainExecutor nodeMainExecutor,
            final NodeMain nodeMain,
            final String rosHostIp,
            final String nodeName,
            final URI rosMasterUri) {
        // Create the node configuration visible to the ROS master and peer nodes.
        final NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(rosHostIp);
        nodeConfiguration.setNodeName(nodeName);
        nodeConfiguration.setMasterUri(rosMasterUri);

        // Ask rosjava to start the node with this configuration.
        nodeMainExecutor.execute(nodeMain, nodeConfiguration);
    }
}
