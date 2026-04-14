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
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * A documented ROS publisher example.
 *
 * <p>This class shows the core rosjava topic-publisher flow:
 * create a {@link Publisher}, create a message with {@code newMessage()},
 * fill the message payload, and publish it periodically.
 *
 * @author Spyros Koukas
 */
public final class ROSJavaPublisherNodeMain extends AbstractNodeMain {
    /**
     * Delay between successive example publishes.
     */
    private static final long PUBLISH_INTERVAL_MILLIS = 1_000;

    /**
     * ROS topic graph name where messages will be published.
     */
    private final String rosTopicName;

    /**
     * ROS node name for this publisher node.
     */
    private final String rosNodeName;

    /**
     * @param rosTopicName the graph name of the topic to publish to
     * @param rosNodeName  the graph name of the ROS node itself
     */
    public ROSJavaPublisherNodeMain(final String rosTopicName, final String rosNodeName) {
        this.rosTopicName = rosTopicName;
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
     * Create the publisher and start sending one example message per second.
     *
     * <p>The key rosjava call is {@link ConnectedNode#newPublisher(String, String)}.
     * The publisher then creates messages with {@link Publisher#newMessage()}.
     *
     * @param connectedNode the connected rosjava node handle
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        // Create the publisher for the standard std_msgs/String topic type.
        final Publisher<std_msgs.String> publisher = connectedNode.newPublisher(this.rosTopicName, std_msgs.String._TYPE);
        final var log = connectedNode.getLog();

        // Publish one message repeatedly so the subscriber has a steady stream to receive.
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            private int counter;

            @Override
            protected final void loop() throws InterruptedException {
                // Create a blank ROS message from the publisher.
                final std_msgs.String message = publisher.newMessage();

                // Fill the message payload.
                message.setData("Hello " + counter++);

                // Publish the message to the ROS topic.
                publisher.publish(message);
                log.info("Publisher: " + message.getData());

                // Slow the loop down so the example logs stay readable.
                Thread.sleep(PUBLISH_INTERVAL_MILLIS);
            }
        });
    }
}
