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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ros.exception.RosRuntimeException;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Main Executable class to run with an external roscore
 * Created at 2022-06-24
 *
 * @author Spyros Koukas
 */
public final class MainExternal {
    private static final int EXIT_ERROR = 1;
    private static final int EXIT_OK = 0;
    private static final String ROS_MASTER_URI = "ROS_MASTER_URI";
    private static final String ROS_IP = "ROS_IP";
    private static final long SERVICE_REGISTRATION_TIMEOUT_MILLIS = 15_000;
    private static final long DEMO_DURATION_MILLIS = 30_000;

    /**
     * Create a {@link NodeConfiguration}
     *
     * @param rosHostIp    the ip of the computer running the node
     * @param nodeName     the name of the node
     * @param rosMasterUri the uri of the rosMaster that this node should connect to
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
        final String rosMasterUriEnv = System.getenv(ROS_MASTER_URI);
        final String rosHostIp = System.getenv(ROS_IP);
        Preconditions.checkState(StringUtils.isNotBlank(rosMasterUriEnv), ROS_MASTER_URI + " environment variable needs to be set.");
        Preconditions.checkState(StringUtils.isNotBlank(rosHostIp), ROS_IP + " environment variable needs to be set.");
        //Services
        final String serverName = "/spyros/test/server/";
        final String clientName = "/spyros/test/client/";
        final String serviceName = "/spyros/test/service/sum";

        //Topics
        final String publisherName = "/spyros/test/publisher/";
        final String subscriberName = "/spyros/test/subscriber/";
        final String topicName = "/spyros/test/topic/";

        //No need to start Roscore, since we assume an external roscore is running.
        try {
            final URI rosMasterUri = new URI(rosMasterUriEnv);

            // An executor is needed to spawn ROS nodes from Java.
            final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
            try {
                // Note that all the nodes below run in parallel once executed by the executor.

                // Create a ROS Service Server for the specified service name and node name.
                final ROSJavaServerNodeMain serviceServerNodeMain = new ROSJavaServerNodeMain(serviceName, serverName);
                final NodeConfiguration serviceServerNodeConfiguration = getNodeConfiguration(rosHostIp, serverName, rosMasterUri);
                nodeMainExecutor.execute(serviceServerNodeMain, serviceServerNodeConfiguration);

                // Wait until the service is registered before starting the client.
                final boolean serviceRegistered = serviceServerNodeMain.awaitRegistration(SERVICE_REGISTRATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                if (!serviceRegistered) {
                    throw new RosRuntimeException("Timed out while waiting for service server registration.");
                }

                // Create a ROS Service Client for the specified service name and node name.
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


        } catch (final Exception exception) {
            //in case of an exception print the stacktrace and exit with EXIT_ERROR(1) value
            System.err.println(ExceptionUtils.getStackTrace(exception));
            System.exit(EXIT_ERROR);
        }
        //Exit with value EXIT_OK(0).
        System.exit(EXIT_OK);

    }
}
