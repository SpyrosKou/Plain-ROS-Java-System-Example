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

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.CountDownServiceServerListener;
import org.ros.node.service.ServiceServer;
import rosjava_test_msgs.AddTwoInts;
import rosjava_test_msgs.AddTwoIntsRequest;
import rosjava_test_msgs.AddTwoIntsResponse;

import java.util.concurrent.TimeUnit;

/**
 * A documented ROS service server example.
 *
 * <p>This class shows the core rosjava service-server flow:
 * choose a service graph name, create the service server with {@code newServiceServer(...)},
 * implement the response callback, and optionally wait until the ROS master reports the service
 * as registered before starting a client.
 *
 * @author Spyros Koukas
 */
public final class ROSJavaServerNodeMain extends AbstractNodeMain {
    /**
     * ROS graph name where the service will be advertised.
     */
    private final String rosServiceName;

    /**
     * ROS node name for this service server node.
     */
    private final String rosNodeName;

    /**
     * Listener used by the demo launcher to block until the service is fully registered.
     * This avoids a race where the client starts before the ROS master knows about the service.
     */
    private final CountDownServiceServerListener<AddTwoIntsRequest, AddTwoIntsResponse> registrationListener =
            CountDownServiceServerListener.newDefault();

    /**
     * @param rosServiceName the graph name of the service to advertise
     * @param rosNodeName    the graph name of the ROS node itself
     */
    public ROSJavaServerNodeMain(final String rosServiceName, final String rosNodeName) {
        this.rosServiceName = rosServiceName;
        this.rosNodeName = rosNodeName;
    }

    /**
     * rosjava asks every {@link org.ros.node.NodeMain} for its default ROS graph name.
     */
    @Override
    public final GraphName getDefaultNodeName() {
        return GraphName.of(this.rosNodeName);
    }

    /**
     * Create the service server after the rosjava node has connected.
     *
     * <p>The important rosjava call here is {@link ConnectedNode#newServiceServer(String, String, org.ros.node.service.ServiceResponseBuilder)}.
     * It binds a Java callback to a ROS service graph name and message type.
     *
     * @param connectedNode the connected rosjava node handle
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        // Create the service server for the AddTwoInts ROS service type.
        final ServiceServer<AddTwoIntsRequest, AddTwoIntsResponse> serviceServer = connectedNode.newServiceServer(
                this.rosServiceName,
                AddTwoInts._TYPE,
                (request, response) -> {
                    // The generated ROS request object already contains the incoming values.
                    response.setSum(request.getA() + request.getB());

                    // Log the request and the produced response for demonstration purposes.
                    connectedNode.getLog().info("Service server: " + request.getA() + " + " + request.getB() + " = " + response.getSum());
                }
        );

        // Register the listener so the launcher can wait until the ROS master sees the service.
        serviceServer.addListener(this.registrationListener);
        connectedNode.getLog().info("Created service server [" + serviceServer.getName() + "]");
    }

    /**
     * Wait until the ROS master confirms that the service has been registered.
     *
     * <p>This helper is not part of rosjava itself; it is only used by the demo launcher to keep
     * startup ordering deterministic.
     *
     * @param timeout  how long to wait
     * @param timeUnit unit of the timeout
     * @return {@code true} if registration succeeded before the timeout
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public final boolean awaitRegistration(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        return this.registrationListener.awaitMasterRegistrationSuccess(timeout, timeUnit);
    }
}
