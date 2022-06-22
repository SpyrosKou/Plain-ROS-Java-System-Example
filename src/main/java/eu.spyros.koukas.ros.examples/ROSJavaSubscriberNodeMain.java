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
import org.apache.commons.logging.Log;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import std_msgs.String;


/**
 * An example subscriber
 * An object of this class is a ROS Node with a specific,  given rosNodeName.
 * This object subscribes to the specified rosTopicName . For each message received it prints and logs the messages at info level.
 * Created at 2020-05-16
 *
 * @author Spyros Koukas
 */
public final class ROSJavaSubscriberNodeMain extends AbstractNodeMain {
    private final java.lang.String rosTopicName;
    private final java.lang.String rosNodeName;

    /**
     * @param rosTopicName the name of the topic to subscribe
     * @param rosNodeName the name of the ROS node
     */
    public ROSJavaSubscriberNodeMain(final java.lang.String rosTopicName, final java.lang.String rosNodeName) {
        //Let's require that rosNodeName and rosTopicName are not null to eagerly identify this error
        //These checks are completely optional
        Preconditions.checkArgument(StringUtils.isNotBlank(rosNodeName));
        Preconditions.checkArgument(StringUtils.isNotBlank(rosTopicName));

        this.rosTopicName = rosTopicName;
        this.rosNodeName = rosNodeName;
    }

    /**
     * @return
     */
    @Override
    public final GraphName getDefaultNodeName() {
        return GraphName.of(this.rosNodeName);
    }

    /**
     * Is executed once after node is connected.
     *
     * @param connectedNode a {@link ConnectedNode} that will be provided as an argument
     *
     * @see AbstractNodeMain#onStart(ConnectedNode)
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {

        final Log log = connectedNode.getLog();

        //create a subscriber, for the rosTopicName, that is of type std_msgs.String._TYPE.
        //ROS Java messages will always have a public static String _TYPE field where the ros type is stored.
        //ROS Java messages will always also have a public static String _DEFINITION filed where the source of the message will be saved.
        final Subscriber<String> subscriber = connectedNode.newSubscriber(this.rosTopicName, std_msgs.String._TYPE);

        //The subscriber needs a messageListener that defines what the subscriber will do when receiving  a message
        //This example creates a subscriber that logs the message using the java lamda syntax
        subscriber.addMessageListener(message -> log.info("Subscriber - Received: [" + message.getData() + "]"));
    }
}