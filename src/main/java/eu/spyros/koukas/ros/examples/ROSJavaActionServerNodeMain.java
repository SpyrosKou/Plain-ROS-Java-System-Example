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

import actionlib_msgs.GoalID;
import actionlib_msgs.GoalStatus;
import actionlib_tutorials.FibonacciActionFeedback;
import actionlib_tutorials.FibonacciActionGoal;
import actionlib_tutorials.FibonacciActionResult;
import com.github.rosjava_actionlib.ActionServer;
import com.github.rosjava_actionlib.ActionServerListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.RosLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A documented ActionLib server example based on {@code actionlib_tutorials/FibonacciAction}.
 *
 * <p>The purpose of this class is to show the minimum rosjava ActionLib server flow:
 * create the {@link ActionServer}, accept an incoming goal, publish feedback while the goal
 * is running, and publish a terminal result when the work is done or when the client cancels it.
 *
 * <p>This version stays intentionally small so it works as a hello-world example, but the comments
 * explain the key ActionLib concepts that the code is exercising.
 *
 * @author Spyros Koukas
 */
public final class ROSJavaActionServerNodeMain extends AbstractNodeMain implements ActionServerListener<FibonacciActionGoal> {
    /**
     * Delay between successive feedback messages so the client can observe the action progressing.
     */
    private static final long FEEDBACK_INTERVAL_MILLIS = 500L;

    /**
     * Shared action graph name. The server and the client must use the same graph name.
     */
    private final String rosActionName;

    /**
     * ROS node name for the action server node itself.
     */
    private final String rosNodeName;

    /**
     * The rosjava ActionLib server helper.
     * It owns the internal ActionLib publishers and subscribers once the node starts.
     */
    private ActionServer<FibonacciActionGoal, FibonacciActionFeedback, FibonacciActionResult> actionServer;

    /**
     * Cached ROS logger so the cancel and result paths can explain what the server is doing.
     */
    private RosLog log;

    /**
     * @param rosActionName the shared ROS action graph name
     * @param rosNodeName   the ROS node name used for the server node itself
     */
    public ROSJavaActionServerNodeMain(final String rosActionName, final String rosNodeName) {
        this.rosActionName = rosActionName;
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
     * Create the ActionLib server when the rosjava node starts.
     *
     * <p>The {@link ActionServer} sets up the standard ActionLib topics for this action:
     * {@code /goal}, {@code /cancel}, {@code /status}, {@code /feedback}, and {@code /result}.
     * This class then receives goal and cancel callbacks through {@link ActionServerListener}.
     *
     * @param connectedNode the connected rosjava node handle
     */
    @Override
    public final void onStart(final ConnectedNode connectedNode) {
        this.log = connectedNode.getLog();
        this.actionServer = new ActionServer<>(
                connectedNode,
                this,
                this.rosActionName,
                FibonacciActionGoal._TYPE,
                FibonacciActionFeedback._TYPE,
                FibonacciActionResult._TYPE
        );
        connectedNode.getLog().info("Created action server [" + this.rosActionName + "]");
    }

    /**
     * Disconnect the ActionLib server when the node shuts down.
     *
     * @param node the rosjava node being shut down
     */
    @Override
    public final void onShutdown(final Node node) {
        if (this.actionServer != null) {
            this.actionServer.finish();
            this.actionServer = null;
        }
    }

    /**
     * Informational callback required by {@link ActionServerListener}.
     *
     * <p>The minimal tutorial does not need extra work here because the actual decision to accept
     * the goal is performed in {@link #acceptGoal(FibonacciActionGoal)}.
     *
     * @param goal the received ActionLib goal message
     */
    @Override
    public final void goalReceived(final FibonacciActionGoal goal) {
        // Intentionally left empty in the simplified tutorial example.
    }

    /**
     * Informational callback required by {@link ActionServerListener}.
     *
     * <p>The real cancellation handling happens inside {@link #runGoal(FibonacciActionGoal)},
     * but this callback is still useful for logging when the client requests it.
     *
     * @param id the goal id that is being cancelled
     */
    @Override
    public final void cancelReceived(final GoalID id) {
        if (this.log != null) {
            this.log.info("Action server: Received cancel request for goal [" + id.getId() + "]");
        }
    }

    /**
     * Accept the incoming Fibonacci goal and start processing it asynchronously.
     *
     * <p>The server returns {@link Optional#empty()} because this example manages the goal lifecycle
     * explicitly: it calls {@code setAccepted(...)}, publishes feedback, and later publishes the
     * terminal result itself.
     *
     * @param goal the received Fibonacci action goal
     * @return whether the goal is rejected immediately or managed manually
     */
    @Override
    public final Optional<Boolean> acceptGoal(final FibonacciActionGoal goal) {
        // Reject invalid requests to keep the example behavior explicit.
        if (goal.getGoal().getOrder() < 0) {
            return Optional.of(Boolean.FALSE);
        }

        final String goalId = goal.getGoalId().getId();

        // Tell ActionLib that this goal has moved from PENDING to ACTIVE.
        this.actionServer.setAccepted(goalId);

        // Execute the goal on a worker thread so feedback publication does not block callbacks.
        final Thread worker = new Thread(() -> this.runGoal(goal), "rosjava-action-server-" + goalId);
        worker.setDaemon(true);
        worker.start();
        return Optional.empty();
    }

    /**
     * Compute the Fibonacci sequence requested by the goal and publish feedback along the way.
     *
     * @param goal the accepted Fibonacci goal
     */
    private final void runGoal(final FibonacciActionGoal goal) {
        final String goalId = goal.getGoalId().getId();

        // Start with the standard Fibonacci seed values.
        final List<Integer> sequence = new ArrayList<>();
        sequence.add(0);
        sequence.add(1);

        try {
            // Publish the initial feedback immediately so the client sees the action start.
            this.sendFeedback(goal, sequence);

            for (int index = 0; index < goal.getGoal().getOrder(); index++) {
                // If the client cancelled an active goal, actionlib moves it into a preempting state.
                if (this.isCancelRequested(goalId)) {
                    this.sendResult(goal, sequence, GoalStatus.PREEMPTED, "Cancelled by client");
                    return;
                }

                // Compute the next Fibonacci number.
                sequence.add(sequence.get(sequence.size() - 1) + sequence.get(sequence.size() - 2));

                // Publish the partial sequence as ActionLib feedback.
                this.sendFeedback(goal, sequence);

                // Slow the loop down so the feedback stream is visible in the logs.
                Thread.sleep(FEEDBACK_INTERVAL_MILLIS);
            }

            if (this.isCancelRequested(goalId)) {
                this.sendResult(goal, sequence, GoalStatus.PREEMPTED, "Cancelled by client");
                return;
            }

            // Publish the final successful result.
            this.sendResult(goal, sequence, GoalStatus.SUCCEEDED, "Done");
        } catch (final InterruptedException exception) {
            // Preserve the interrupted flag if shutdown happens during the example.
            Thread.currentThread().interrupt();
        } catch (final Exception exception) {
            // Publish an aborted result if anything unexpected happens while computing.
            this.sendResult(goal, sequence, GoalStatus.ABORTED, exception.getMessage());
        }
    }

    /**
     * Publish one ActionLib feedback message containing the current partial sequence.
     *
     * @param goal     the active goal
     * @param sequence the sequence computed so far
     */
    private final void sendFeedback(final FibonacciActionGoal goal, final List<Integer> sequence) {
        if (this.actionServer == null) {
            return;
        }

        // Create the feedback message from the ActionLib server helper.
        final FibonacciActionFeedback feedback = this.actionServer.newFeedbackMessage();

        // ActionLib feedback messages carry the goal id inside the embedded status field.
        copyGoalId(goal.getGoalId(), feedback.getStatus().getGoalId());

        // Mark the status as ACTIVE while feedback is being published.
        feedback.getStatus().setStatus(GoalStatus.ACTIVE);

        // Copy the Java list into the ROS int[] field expected by the generated message class.
        feedback.getFeedback().setSequence(toIntArray(sequence));

        // Publish to the action's /feedback topic.
        this.actionServer.sendFeedback(feedback);
    }

    /**
     * Publish the terminal ActionLib result for the goal.
     *
     * @param goal     the goal that is finishing
     * @param sequence the final computed sequence
     * @param status   the terminal ActionLib status
     * @param text     the human-readable result text
     */
    private final void sendResult(final FibonacciActionGoal goal, final List<Integer> sequence, final byte status, final String text) {
        if (this.actionServer == null) {
            return;
        }

        // Create the result message from the ActionLib server helper.
        final FibonacciActionResult result = this.actionServer.newResultMessage();

        // Copy the goal id into the embedded status field so the client can correlate the result.
        copyGoalId(goal.getGoalId(), result.getStatus().getGoalId());

        // Fill the ActionLib status information.
        result.getStatus().setStatus(status);
        result.getStatus().setText(text);

        // Fill the action-specific result payload.
        result.getResult().setSequence(toIntArray(sequence));

        // Update the ActionLib goal state before publishing the result.
        final String goalId = goal.getGoalId().getId();
        switch (status) {
            case GoalStatus.SUCCEEDED -> this.actionServer.setSucceed(goalId);
            case GoalStatus.PREEMPTED -> {
                final byte currentGoalStatus = this.actionServer.getGoalStatus(goalId);
                if (currentGoalStatus == GoalStatus.PREEMPTING) {
                    this.actionServer.setCancel(goalId);
                } else {
                    this.actionServer.setPreempt(goalId);
                }
            }
            case GoalStatus.REJECTED -> this.actionServer.setRejected(goalId);
            default -> this.actionServer.setAbort(goalId);
        }

        // Publish to the action's /result topic.
        this.actionServer.sendResult(result);

        if (this.log != null) {
            this.log.info("Action server result (" + status + "): " + sequence);
        }
    }

    /**
     * Check whether actionlib has received a cancel request for the running goal.
     *
     * <p>For an active goal, the cancel request is represented by the PREEMPTING state.
     *
     * @param goalId the running goal id
     * @return {@code true} if the goal should stop and publish a preempted result
     */
    private final boolean isCancelRequested(final String goalId) {
        if (this.actionServer == null) {
            return false;
        }

        final byte goalStatus = this.actionServer.getGoalStatus(goalId);
        return goalStatus == GoalStatus.PREEMPTING || goalStatus == GoalStatus.RECALLING;
    }

    /**
     * Copy the ROS goal id fields between two generated message objects.
     *
     * @param source the source goal id
     * @param target the target goal id
     */
    private static final void copyGoalId(final GoalID source, final GoalID target) {
        target.setId(source.getId());
        target.setStamp(source.getStamp());
    }

    /**
     * Convert a Java {@link List} of integers to the primitive {@code int[]} required by the
     * generated ROS message class.
     *
     * @param sequence the Java list representation of the sequence
     * @return the primitive array expected by the ROS message
     */
    private static final int[] toIntArray(final List<Integer> sequence) {
        return sequence.stream().mapToInt(Integer::intValue).toArray();
    }
}
