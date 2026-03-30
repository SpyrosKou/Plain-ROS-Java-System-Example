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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ros.RosCore;
import org.ros.exception.RosRuntimeException;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Main Executable class that starts a rosjava roscore.
 * An example that uses an external roscore is avalailable in {@link MainExternalRos}
 * Created at 2020-05-18
 *
 * @author Spyros Koukas
 */
public final class Main {
    private static final int EXIT_ERROR = 1;
    private static final int EXIT_OK = 0;
    private static final long ROSCORE_START_TIMEOUT_MILLIS = 2_000;
    private static final long SERVICE_REGISTRATION_TIMEOUT_MILLIS = 15_000;
    private static final long DEMO_DURATION_MILLIS = 30_000;

    /**
     * Create a {@link NodeConfiguration}
     *
     * @param rosHostIp    the ip of the computer running the node
     * @param nodeName     the name of the node
     * @param rosMasterUri the uri of the rosMaster that this node should connect to
     *
     * @return the completed {@link NodeConfiguration} with the arguments provided
     */
    private static final NodeConfiguration getNodeConfiguration(final String rosHostIp, final String nodeName, final URI rosMasterUri) {
        //Create a node configuration
        final NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(rosHostIp);
        nodeConfiguration.setNodeName(nodeName);
        nodeConfiguration.setMasterUri(rosMasterUri);
        return nodeConfiguration;
    }

    /**
     * Main executable method
     *
     * @param args
     */
    public static void main(final String[] args) {
        //Services
        final String serverName = "/spyros/test/server/";
        final String clientName = "/spyros/test/client/";
        final String serviceName = "/spyros/test/service/sum";

        //Topics
        final String publisherName = "/spyros/test/publisher/";
        final String subscriberName = "/spyros/test/subscriber/";
        final String topicName = "/spyros/test/topic/";

        //Common
        final String rosHostIp = "127.0.0.1";
        final int rosHostPort = 11311;


        //Create a publically available roscore in port rosHostPort.
        final RosCore rosCore = RosCore.newPublic(rosHostPort);
        //This will start the created java ROS Core.
        rosCore.start();
        try {
            final URI rosMasterUri = new URI("http://127.0.0.1:11311");

            // Before proceeding any further we need to make sure that the roscore is already started.
            final boolean started = rosCore.awaitStart(ROSCORE_START_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

            if (started) {
                //An executor is needed to spawn ROS nodes from Java.
                final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
                try {
                    // Note that all the nodes below run in parallel once executed by the executor.

                    // Create a ROS Service Server for the specified service and node name.
                    final ROSJavaServerNodeMain serviceServerNodeMain = new ROSJavaServerNodeMain(serviceName, serverName);
                    final NodeConfiguration serviceServerNodeConfiguration = getNodeConfiguration(rosHostIp, serverName, rosMasterUri);
                    nodeMainExecutor.execute(serviceServerNodeMain, serviceServerNodeConfiguration);

                    // For tutorial purposes we wait explicitly for the service to be visible to the ROS master.
                    // This keeps the client example simple and avoids hiding the startup race inside the client node.
                    final boolean serviceRegistered = serviceServerNodeMain.awaitRegistration(SERVICE_REGISTRATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                    if (!serviceRegistered) {
                        throw new RosRuntimeException("Timed out while waiting for service server registration.");
                    }

                    // Create a ROS Service Client for the specified service and node name.
                    final ROSJavaClientNodeMain serviceClientNodeMain = new ROSJavaClientNodeMain(serviceName, clientName);
                    final NodeConfiguration serviceClientNodeConfiguration = getNodeConfiguration(rosHostIp, clientName, rosMasterUri);
                    nodeMainExecutor.execute(serviceClientNodeMain, serviceClientNodeConfiguration);

                    // Create a publisher for the specified topic name and publisher name.
                    final ROSJavaPublisherNodeMain topicPublisherNodeMain = new ROSJavaPublisherNodeMain(topicName, publisherName);
                    final NodeConfiguration topicPublisherNodeConfiguration = getNodeConfiguration(rosHostIp, publisherName, rosMasterUri);
                    nodeMainExecutor.execute(topicPublisherNodeMain, topicPublisherNodeConfiguration);

                    // Create a subscriber for the specified topic name and subscriber name.
                    final ROSJavaSubscriberNodeMain topicSubscriberNodeMain = new ROSJavaSubscriberNodeMain(topicName, subscriberName);
                    final NodeConfiguration topicSubscriberNodeConfiguration = getNodeConfiguration(rosHostIp, subscriberName, rosMasterUri);
                    nodeMainExecutor.execute(topicSubscriberNodeMain, topicSubscriberNodeConfiguration);

                    // Keep the demo alive long enough to observe service calls and topic traffic.
                    try {
                        Thread.sleep(DEMO_DURATION_MILLIS);
                    } catch (final InterruptedException interruptedException) {
                        //In this example any interruptedException is ignored
                    }

                    // For demonstration purposes we explicitly shut down each node main.
                    // This is not strictly required because nodeMainExecutor.shutdown() below would also stop them.
                    nodeMainExecutor.shutdownNodeMain(serviceClientNodeMain);
                    nodeMainExecutor.shutdownNodeMain(topicSubscriberNodeMain);
                    nodeMainExecutor.shutdownNodeMain(topicPublisherNodeMain);
                    nodeMainExecutor.shutdownNodeMain(serviceServerNodeMain);
                } finally {
                    // Shutting down the executor is still useful as the final cleanup step.
                    nodeMainExecutor.shutdown();
                }

            } else {
                throw new RosRuntimeException("Timed out while waiting for roscore to start.");
            }
        } catch (final Exception exception) {
            //in case of an exception print the stacktrace and exit with EXIT_ERROR(1) value
            System.err.println(ExceptionUtils.getStackTrace(exception));
            System.exit(EXIT_ERROR);
        } finally {
            //In this example the roscore is shutdown after the predefined duration.
            rosCore.shutdown();
        }
        //Exit with value EXIT_OK(0).
        System.exit(EXIT_OK);

    }
}
