package eu.spyros.koukas.ros.examples;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ros.RosCore;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Main Executable class
 * Created at 2020-05-18
 *
 * @author Spyros Koukas
 */
public final class Main {

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

        final String publisherName = "/spyros/test/publisher/";
        final String subscriberName = "/spyros/test/subscriber/";
        final String topicName = "/spyros/test/topic/";
        final String rosHostIp = "127.0.0.1";
        final int rosHostPort = 11311;


        //Create a publically available roscore in port rosHostPort.
        final RosCore rosCore = RosCore.newPublic(rosHostPort);
        //This will start the created java ROS Core.
        rosCore.start();


        try {
            final URI rosMasterUri = new URI("http://127.0.0.1:11311");

            //Before proceeding any further we need to make sure that the roscore is already started.
            //The following line will wait for the roscore to start for a maximum of 2 seconds
            final boolean started = rosCore.awaitStart(2_000, TimeUnit.MILLISECONDS);

            if (started) {
                //An executor is needed to spawn ROS nodes from Java.
                final NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();

                // Create a publisher for the specified topic name and publisher name
                final ROSJavaPublisherNodeMain rosJavaPublisherNodeMain = new ROSJavaPublisherNodeMain(topicName, publisherName);
                final NodeConfiguration publisherNodeConfiguration = getNodeConfiguration(rosHostIp, publisherName, rosMasterUri);
                nodeMainExecutor.execute(rosJavaPublisherNodeMain, publisherNodeConfiguration);

                // Create a subscriber for the specified topic name and publisher name
                final ROSJavaSubscriberNodeMain rosJavaSubscriberNodeMain = new ROSJavaSubscriberNodeMain(topicName, subscriberName);
                final NodeConfiguration subscriberNodeConfiguration = getNodeConfiguration(rosHostIp, subscriberName, rosMasterUri);
                nodeMainExecutor.execute(rosJavaSubscriberNodeMain, subscriberNodeConfiguration);

                //Let the main thread sleep for 30 seconds
                try {
                    Thread.sleep(30_000);
                } catch (final InterruptedException interruptedException) {
                    //In this example any interruptedException is ignored
                }
                //Shut down subscriber
                nodeMainExecutor.shutdownNodeMain(rosJavaSubscriberNodeMain);
                //Shut down publisher
                nodeMainExecutor.shutdownNodeMain(rosJavaPublisherNodeMain);
                //Shut down the executor
                nodeMainExecutor.shutdown();
            }
        } catch (final Exception exception) {
            //in case of an exception print the stacktrace and exit with 1 value
            System.err.println(ExceptionUtils.getStackTrace(exception));
            System.exit(1);
        } finally {
            //In this example the roscore is shutdown after the predefined duration.
            rosCore.shutdown();
        }


    }
}
