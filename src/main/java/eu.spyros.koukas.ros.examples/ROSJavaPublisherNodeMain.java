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
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * An object of this class will be a ROS Node with the given rosNodeName that will publish a series of messages to the specified rosTopicName.
 * Created at 2020-05-16
 *
 * @author Spyros Koukas
 */
public final class ROSJavaPublisherNodeMain extends AbstractNodeMain {
    private final java.lang.String rosTopicName;
    private final java.lang.String rosNodeName;
    private final long SLEEP_DURATION_MILLIS = 1000;

    /**
     * @param rosTopicName the name of the topic to publish
     * @param rosNodeName  the name of the node
     */
    public ROSJavaPublisherNodeMain(final java.lang.String rosTopicName, final java.lang.String rosNodeName) {
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
     * @param connectedNode
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        //Create a new publisher for the rosTopicName, that is of type std_msgs.String._TYPE.
        //ROS Java messages will always have a public static String _TYPE field where the ros type is stored.
        //ROS Java messages will always also have a public static String _DEFINITION filed where the source of the message will be saved.
        //The publisher object can be used to create messages and also publish them.
        //Note that it is the responsibility of the programmer to provide the correct type to the publisher
        final Publisher<std_msgs.String> publisher = connectedNode.newPublisher(this.rosTopicName, std_msgs.String._TYPE);

        final Log log = connectedNode.getLog();

        //The CancellableLoop will run again and again until the node is stopped.
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            //Take a note of the system time when started. Note that we do not use ROS time here.
            private final long startMillis = System.currentTimeMillis();

            /**
             *
             * @throws InterruptedException
             */
            @Override
            protected void loop() throws InterruptedException {
                //Create a new blank message
                final std_msgs.String message = publisher.newMessage();
                //Add some information to the message
                message.setData((System.currentTimeMillis() - startMillis) + " milliseconds");
                //publish the message to the topic
                publisher.publish(message);

                //log the published message
                log.info("Publisher - Published: [" + message.getData() + "]");

                //wait for SLEEP_DURATION_MILLIS to throttle the rate of the  published messages
                Thread.sleep(SLEEP_DURATION_MILLIS);
            }
        });
    }
}
