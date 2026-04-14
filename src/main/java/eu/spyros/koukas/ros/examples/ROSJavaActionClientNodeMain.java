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

import actionlib_msgs.GoalStatus;
import actionlib_msgs.GoalStatusArray;
import actionlib_tutorials.FibonacciActionFeedback;
import actionlib_tutorials.FibonacciActionGoal;
import actionlib_tutorials.FibonacciActionResult;
import com.github.rosjava_actionlib.ActionClient;
import com.github.rosjava_actionlib.ActionFuture;
import com.github.rosjava_actionlib.ActionClientListener;
import com.github.rosjava_actionlib.GoalStatusToString;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.RosLog;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
     * A documented ActionLib client example based on {@code actionlib_tutorials/FibonacciAction}.
     *
     * <p>The purpose of this class is to show the minimum rosjava ActionLib client flow:
     * create the {@link ActionClient}, wait until the ActionLib protocol topics are connected,
     * create a goal message, send the goal, and then observe feedback and result callbacks.
     * It also includes one small advanced example that uses {@link ActionFuture} for cancellation.
 *
 * <p>The implementation keeps the behavior small, but the comments remain explicit so the reader
 * can understand which lines are plain Java and which lines are the actual ROS ActionLib API.
 *
 * @author Spyros Koukas
 */
public final class ROSJavaActionClientNodeMain extends AbstractNodeMain implements ActionClientListener<FibonacciActionFeedback, FibonacciActionResult> {
    /**
     * ActionLib uses several publishers and subscribers internally.
     * This timeout gives the client a short period to discover and connect to them before sending a goal.
     */
    private static final long SERVER_CONNECTION_TIMEOUT_SECONDS = 15L;

    /**
     * This timeout is used when waiting for the terminal result of the tutorial goals.
     */
    private static final long ACTION_RESULT_TIMEOUT_SECONDS = 20L;

    /**
     * The advanced ActionFuture example uses a larger order so the goal is still running
     * when the cancellation request is sent.
     */
    private static final int ADVANCED_ORDER_INCREMENT = 16;

    /**
     * How long the advanced example waits before cancelling the running goal.
     * This small delay gives the server enough time to publish some feedback first.
     */
    private static final long ADVANCED_CANCEL_DELAY_MILLIS = 1_500L;

    /**
     * rosjava_actionlib already ships a helper that maps the numeric goal status byte
     * to the standard ActionLib status label.
     */
    private static final GoalStatusToString GOAL_STATUS_TO_STRING = new GoalStatusToString();

    /**
     * Shared action graph name, for example {@code /spyros/test/action/fibonacci}.
     * The client and the server must use exactly the same graph name.
     */
    private final String rosActionName;

    /**
     * ROS node name for this client node.
     */
    private final String rosNodeName;

    /**
     * Fibonacci order that will be sent in the demo goal.
     */
    private final int fibonacciOrder;

    /**
     * ActionLib client instance created when the node starts.
     */
    private ActionClient<FibonacciActionGoal, FibonacciActionFeedback, FibonacciActionResult> actionClient;

    /**
     * Cached ROS logger so the callback methods can log without receiving the {@link ConnectedNode}.
     */
    private RosLog log;

    /**
     * The status callback receives a heartbeat stream.
     * These fields keep the logs readable by printing only status changes for the currently tracked goal.
     */
    private String lastLoggedGoalId;
    private String lastLoggedStatusLabel;

    /**
     * @param rosActionName  the shared ROS action graph name
     * @param rosNodeName    the ROS node name used for the client node itself
     * @param fibonacciOrder the Fibonacci order that the demo goal will request
     */
    public ROSJavaActionClientNodeMain(final String rosActionName, final String rosNodeName, final int fibonacciOrder) {
        this.rosActionName = rosActionName;
        this.rosNodeName = rosNodeName;
        this.fibonacciOrder = fibonacciOrder;
    }

    /**
     * rosjava asks every {@link org.ros.node.NodeMain} for its default ROS graph name.
     */
    @Override
    public final GraphName getDefaultNodeName() {
        return GraphName.of(this.rosNodeName);
    }

    /**
     * Create the ActionLib client once the ROS node is connected.
     *
     * <p>The constructor of {@link ActionClient} wires the client to the five ActionLib topics:
     * {@code /goal}, {@code /cancel}, {@code /status}, {@code /feedback}, and {@code /result}.
     * After construction we register this class as the listener for feedback and result callbacks.
     *
     * <p>The actual wait for the server connection is moved to a worker thread so the rosjava
     * callback thread remains free and the example code stays readable.
     *
     * @param connectedNode the connected rosjava node handle
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        // Once connected, store the log for future usage inside callback methods.
        this.log = connectedNode.getLog();

        // Create the ActionLib client for the shared action graph name.
        this.actionClient = new ActionClient<>(
                connectedNode,
                this.rosActionName,
                FibonacciActionGoal._TYPE,
                FibonacciActionFeedback._TYPE,
                FibonacciActionResult._TYPE
        );

        // Register this node as the listener that will receive feedback and result callbacks.
        this.actionClient.addActionClientListener(this);
        this.log.info("Created ROS Action Client [" + this.rosActionName + "]. Waiting for the action server topics to connect.");

        // The wait can block for several seconds, so keep it off the rosjava callback thread.
        final Thread worker = new Thread(() -> this.waitForServerAndRunExamples(), "rosjava-action-client");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Disconnect the ActionLib client when the node is shutting down.
     *
     * <p>This tears down the internal ActionLib publishers and subscribers created above.
     *
     * @param node the rosjava node being shut down
     */
    @Override
    public final void onShutdown(final Node node) {
        if (this.actionClient != null) {
            this.actionClient.disconnect();
            this.actionClient = null;
        }
    }

    /**
     * ActionLib publishes feedback while the goal is still running.
     * In the Fibonacci tutorial this contains the partial sequence computed so far.
     *
     * @param feedback the ActionLib feedback message
     */
    @Override
    public final void feedbackReceived(final FibonacciActionFeedback feedback) {
        if (this.log != null) {
            this.log.info("Action client feedback: " + Arrays.toString(feedback.getFeedback().getSequence()));
        }
    }

    /**
     * ActionLib publishes a terminal result once the goal has completed.
     *
     * @param result the ActionLib result message
     */
    @Override
    public final void resultReceived(final FibonacciActionResult result) {
        if (this.log != null) {
            this.log.info("Action client callback result (" + this.toStatusLabel(result.getStatus().getStatus()) + "): "
                    + Arrays.toString(result.getResult().getSequence()));
        }
    }

    /**
     * ActionLib publishes periodic status heartbeat messages on the {@code /status} topic.
     *
     * <p>This example keeps the logs readable by printing only status changes for the goal currently
     * tracked by the client.
     *
     * @param statusArray the current ActionLib status array
     */
    @Override
    public final void statusReceived(final GoalStatusArray statusArray) {
        if (this.log == null || this.actionClient == null) {
            return;
        }

        // Ask rosjava_actionlib to find the status entry that belongs to the goal currently tracked by this client.
        final GoalStatus goalStatus = this.actionClient.findStatus(statusArray);
        if (goalStatus == null || goalStatus.getGoalId() == null) {
            return;
        }

        final String goalId = goalStatus.getGoalId().getId();
        final String statusLabel = this.toStatusLabel(goalStatus.getStatus());
        final boolean goalChanged = goalId == null ? this.lastLoggedGoalId != null : !goalId.equals(this.lastLoggedGoalId);
        if (!statusLabel.equals(this.lastLoggedStatusLabel) || goalChanged) {
            this.lastLoggedGoalId = goalId;
            this.lastLoggedStatusLabel = statusLabel;

            final String statusText = goalStatus.getText();
            if (statusText == null || statusText.isBlank()) {
                this.log.info("Action client status [" + goalId + "]: " + statusLabel);
            } else {
                this.log.info("Action client status [" + goalId + "]: " + statusLabel + " (" + statusText + ")");
            }
        }
    }

    /**
     * Wait until the server-side ActionLib publishers and subscribers are connected, then run the
     * simple and advanced ActionLib examples.
     *
     * <p>Calling {@code sendGoal(...)} before the ActionLib handshake completes can race in small demos,
     * so the example waits explicitly before creating and publishing any goals.
     */
    private final void waitForServerAndRunExamples() {
        if (this.actionClient == null) {
            return;
        }

        try {
            // Wait until the ActionLib topic handshake is complete.
            if (!this.actionClient.waitForServerConnection(SERVER_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while waiting for action server [" + this.rosActionName + "]");
            }

            this.runSimpleGoalExample();
            this.runAdvancedActionFutureExample();
        } catch (final InterruptedException exception) {
            // Preserve the interrupted flag if the demo is shutting down while we wait.
            Thread.currentThread().interrupt();
        } catch (final Exception exception) {
            if (this.log != null) {
                this.log.error("Action client example failed: " + exception.getMessage());
            }
        }
    }

    /**
     * Send one ordinary Fibonacci goal and wait for the terminal result.
     *
     * <p>This keeps the original hello-world action flow in the example:
     * create a goal message, send it, and observe the normal success path.
     *
     * @throws Exception if the goal does not finish in time
     */
    private final void runSimpleGoalExample() throws Exception {
        final ActionFuture<FibonacciActionGoal, FibonacciActionFeedback, FibonacciActionResult> goalFuture =
                this.sendGoal(this.fibonacciOrder, "simple");

        // Wait until the goal finishes so the next advanced example starts from a clean client state.
        goalFuture.get(ACTION_RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (this.log != null) {
            this.log.info("Action client ActionFuture state after the simple goal: " + goalFuture.getCurrentState());
        }
    }

    /**
     * Demonstrate the more advanced ActionFuture API.
     *
     * <p>The flow is:
     * send a larger goal, inspect the current ActionFuture state, read the latest feedback captured
     * by the future, cancel the running goal through the future itself, and then wait for the
     * terminal preempted result.
     *
     * @throws Exception if the advanced goal does not finish in time
     */
    private final void runAdvancedActionFutureExample() throws Exception {
        final int advancedOrder = this.fibonacciOrder + ADVANCED_ORDER_INCREMENT;
        final ActionFuture<FibonacciActionGoal, FibonacciActionFeedback, FibonacciActionResult> actionFuture =
                this.sendGoal(advancedOrder, "advanced");

        if (this.log != null) {
            this.log.info("Action client ActionFuture state right after send: " + actionFuture.getCurrentState());
        }

        // Let the advanced goal run briefly so the server can publish a few feedback messages first.
        Thread.sleep(ADVANCED_CANCEL_DELAY_MILLIS);

        this.logLatestFeedback(actionFuture);

        // Cancel the active goal through ActionFuture. In actionlib terms this preempts the running goal.
        final boolean cancelRequestPublished = actionFuture.cancel(true);
        if (this.log != null) {
            this.log.info("Action client ActionFuture cancel request published = "
                    + cancelRequestPublished + ", current state = " + actionFuture.getCurrentState());
        }

        // The advanced goal should now complete with a preempted terminal result.
        final FibonacciActionResult result = actionFuture.get(ACTION_RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (this.log != null) {
            this.log.info("Action client ActionFuture terminal state: " + actionFuture.getCurrentState());
            this.log.info("Action client ActionFuture final result (" + this.toStatusLabel(result.getStatus().getStatus()) + "): "
                    + Arrays.toString(result.getResult().getSequence()));
        }
    }

    /**
     * Build and send one Fibonacci goal.
     *
     * <p>{@link ActionClient#sendGoal(org.ros.internal.message.Message)} returns an
     * {@link ActionFuture}, which is the advanced API used later for cancellation and result waiting.
     *
     * @param order       the requested Fibonacci order
     * @param description short label used in the logs
     * @return the ActionFuture tracking the goal lifecycle
     */
    private final ActionFuture<FibonacciActionGoal, FibonacciActionFeedback, FibonacciActionResult> sendGoal(
            final int order,
            final String description) {
        // Create a new ActionLib goal message.
        final FibonacciActionGoal goal = this.actionClient.newGoalMessage();

        // Fill the action-specific payload.
        goal.getGoal().setOrder(order);

        // Publish the goal to the action's /goal topic and keep the returned ActionFuture.
        final ActionFuture<FibonacciActionGoal, FibonacciActionFeedback, FibonacciActionResult> actionFuture =
                this.actionClient.sendGoal(goal);

        if (this.log != null) {
            this.log.info("Action client: Sent " + description + " Fibonacci goal with order " + order);
        }
        return actionFuture;
    }

    /**
     * Log the latest feedback currently stored inside the ActionFuture.
     *
     * @param actionFuture the future tracking the advanced goal
     */
    private final void logLatestFeedback(
            final ActionFuture<FibonacciActionGoal, FibonacciActionFeedback, FibonacciActionResult> actionFuture) {
        if (this.log == null) {
            return;
        }

        final FibonacciActionFeedback latestFeedback = actionFuture.getLatestFeedback();
        if (latestFeedback == null) {
            this.log.info("Action client ActionFuture latest feedback: None received yet.");
        } else {
            this.log.info("Action client ActionFuture latest feedback: "
                    + Arrays.toString(latestFeedback.getFeedback().getSequence()));
        }
    }

    /**
     * Convert the raw ActionLib status byte to the standard text label.
     *
     * <p>The helper already exists in the library; we keep this wrapper only to provide a fallback
     * if an unknown status value is ever received.
     *
     * @param status the raw ActionLib status byte
     * @return a readable label such as {@code SUCCEEDED}
     */
    private static final String toStatusLabel(final byte status) {
        final String label = GOAL_STATUS_TO_STRING.getStatus(status);
        return label != null ? label : "UNKNOWN(" + status + ")";
    }
}
