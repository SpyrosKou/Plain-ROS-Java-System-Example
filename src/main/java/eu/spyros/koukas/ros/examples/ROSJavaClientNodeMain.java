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

import org.ros.concurrent.CancellableLoop;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import rosjava_test_msgs.AddTwoInts;
import rosjava_test_msgs.AddTwoIntsRequest;
import rosjava_test_msgs.AddTwoIntsResponse;

/**
 * A documented ROS service client example.
 *
 * <p>This class shows the core rosjava service-client flow:
 * create a {@link ServiceClient}, create request messages with {@code newMessage()},
 * submit requests with {@code call(...)}, and handle the asynchronous response callback.
 *
 * @author Spyros Koukas
 */
public final class ROSJavaClientNodeMain extends AbstractNodeMain {
    /**
     * Delay between successive example service calls.
     */
    private static final long CALL_INTERVAL_MILLIS = 1_000;

    /**
     * ROS graph name of the remote service that this client will call.
     */
    private final String rosServiceName;

    /**
     * ROS node name for this service client node.
     */
    private final String rosNodeName;

    /**
     * @param rosServiceName the graph name of the service to call
     * @param rosNodeName    the graph name of the ROS node itself
     */
    public ROSJavaClientNodeMain(final String rosServiceName, final String rosNodeName) {
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
     * Create the service client and start making periodic example calls.
     *
     * <p>The key rosjava call is {@link ConnectedNode#newServiceClient(String, String)}.
     * After the client is created, every call uses a new request message produced by
     * {@link ServiceClient#newMessage()}.
     *
     * @param connectedNode the connected rosjava node handle
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        final var log = connectedNode.getLog();

        try {
            // Create a client for the AddTwoInts ROS service type.
            final ServiceClient<AddTwoIntsRequest, AddTwoIntsResponse> serviceClient =
                    connectedNode.newServiceClient(this.rosServiceName, AddTwoInts._TYPE);

            // The response arrives asynchronously, so rosjava requires a response listener.
            final ServiceResponseListener<AddTwoIntsResponse> responseListener = new ServiceResponseListener<>() {
                @Override
                public final void onSuccess(final AddTwoIntsResponse response) {
                    log.info("Service client: Sum = " + response.getSum());
                }

                @Override
                public final void onFailure(final RemoteException exception) {
                    log.error(exception.getMessage());
                }
            };

            // Run the example call repeatedly so the interaction stays visible in the logs.

            connectedNode.executeCancellableLoop(new CancellableLoop() {
                @Override
                protected final void loop() throws InterruptedException {
                    // Create a fresh request message for the service call.
                    final AddTwoIntsRequest request = serviceClient.newMessage();

                    // Fill the request payload.
                    request.setA(1);
                    request.setB(2);

                    // Invoke the ROS service asynchronously if connected, otherwise skip the call
                    log.info("Service client: Calling 1 + 2");
                    if (serviceClient.isConnected()) {
                        serviceClient.call(request, responseListener);
                    } else {
                        log.debug("Service client: Not Connected");
                    }

                    // Slow the loop down so the logs remain readable.
                    Thread.sleep(CALL_INTERVAL_MILLIS);
                }
            });
        } catch (final ServiceNotFoundException exception) {
            throw new RosRuntimeException(exception);
        }
    }
}
