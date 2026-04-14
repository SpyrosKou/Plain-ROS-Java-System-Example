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
import org.ros.node.topic.Subscriber;

/**
 * A documented ROS subscriber example.
 *
 * <p>This class shows the core rosjava topic-subscriber flow:
 * create a {@link Subscriber} for a topic and register a message listener that is called
 * every time a new message arrives.
 *
 * @author Spyros Koukas
 */
public final class ROSJavaSubscriberNodeMain extends AbstractNodeMain {
    /**
     * ROS topic graph name to subscribe to.
     */
    private final String rosTopicName;

    /**
     * ROS node name for this subscriber node.
     */
    private final String rosNodeName;

    /**
     * @param rosTopicName the graph name of the topic to subscribe to
     * @param rosNodeName  the graph name of the ROS node itself
     */
    public ROSJavaSubscriberNodeMain(final String rosTopicName, final String rosNodeName) {
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
     * Create the subscriber and register a callback that logs each received message.
     *
     * <p>The key rosjava call is {@link ConnectedNode#newSubscriber(String, String)}.
     * The returned subscriber then accepts a message listener implemented here as a lambda.
     *
     * @param connectedNode the connected rosjava node handle
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        // Create a subscriber for the standard std_msgs/String topic type.
        final Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(this.rosTopicName, std_msgs.String._TYPE);

        // Register the callback that runs every time a new ROS message arrives.
        subscriber.addMessageListener(message -> connectedNode.getLog().info("Subscriber: " + message.getData()));
    }
}
