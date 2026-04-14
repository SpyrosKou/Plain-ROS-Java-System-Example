# [Plain ROS Java System Example Project](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git)

ROS Java contains a plain java, standalone version of roscore that does not require any ROS instalation to be present.
This is an example ROS system running completely on Java with no ROS instalation required.
It is an example that demonstrates running a roscore and six rosjava ROS nodes.
A Topic Publisher, a ROS Topic Subscriber, a ROS Service Server, a Service Client, an ActionLib Server, and an ActionLib Client.

This example project demonstrates how to do the following **programmatically** through the ROS Java API:
1. Create, start, and shutdown the Java roscore 
2. Create, start, and shutdown ROS nodes
3. Publish and subscribe to a ROS topic
4. Create a ROS Service Server and call it from a ROS Service Client
5. Create a ROS ActionLib Server and call it from a ROS ActionLib client

This repository also provides an example that demonstrates how to run rosjava with another roscore. E.g. cpp roscore from **ROS noetic**.    
A version of this repository that uses **custom ROS messages** is available [here](https://github.com/SpyrosKou/Custom-Messages-ROS-Java-System-Example.git).

## Requirements

In order to compile and run this project only Java needs to be installed. [GIT](https://git-scm.com/downloads) also makes getting the source easier. Links are provided for convenience.
- [GIT](https://git-scm.com/downloads) , in order to clone the [project repository](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git)
- JDK, the [project](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git) has been developed and tested using [JDK 21](https://jdk.java.net/21/) or later
- [Gradle](https://gradle.org/) is used for project compilation. Installing it is not required. The included Gradle wrapper will automatically download the configured Gradle version.

Before running the Gradle commands below, make sure `JAVA_HOME` points to a JDK 21 or later installation and that `java -version` reports Java 21 or later.
Java 21 is required for the current dependency set. 


## Quick Instructions for Windows [Power Shell](https://github.com/PowerShell/PowerShell/releases/)
1. [Clone](https://git-scm.com/docs/git-clone) the [project repository](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git):
`git clone https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git`

2. Go into the cloned repository directory:
`cd .\Plain-ROS-Java-System-Example\`

3. Compile the project and prepare for running:
`.\gradlew.bat installDist`

4. Run the project using the generated script:
`.\build\install\Plain-ROS-Java-System-Example\bin\Plain-ROS-Java-System-Example.bat`
The demo runs for about 30 seconds and prints ROS publisher, subscriber, service, and ActionLib activity to the console.

5. Build and run in a single command using the [gradle application plugin](https://docs.gradle.org/current/userguide/application_plugin.html):
`.\gradlew.bat run`

## Quick Instructions for Linux
1. [Clone](https://git-scm.com/docs/git-clone) the [project repository](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git):
`git clone https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git`

2. Go into the cloned repository directory:
`cd Plain-ROS-Java-System-Example/`

3. [Add execute permission](http://manpages.ubuntu.com/manpages/focal/man1/chmod.1.html) to gradlew script:
`sudo chmod +x gradlew`

4. Compile the project and install it locally:
`./gradlew installDist`

5. Run the project using the generated script:
`./build/install/Plain-ROS-Java-System-Example/bin/Plain-ROS-Java-System-Example`
The demo runs for about 30 seconds and prints ROS publisher, subscriber, service, and ActionLib activity to the console.

6. Build and run in a single command using using the [gradle application plugin](https://docs.gradle.org/current/userguide/application_plugin.html):
`./gradlew run`

## ActionLib Example
The repository includes an ActionLib example for `actionlib_tutorials/FibonacciAction`.

- The ActionLib server is implemented in `ROSJavaActionServerNodeMain` and publishes the action at `/spyros/test/action/fibonacci`.
- The ActionLib client is implemented in `ROSJavaActionClientNodeMain` and waits for the full ActionLib Server before sending any goals.
  - The client makes two calls to the server:   
    - Basic flow: send a goal, receive feedback, and wait for a successful result.
    - Advanced flow: Get the returned `ActionFuture`, inspect its  state, read the latest feedback, cancel the running goal via `ActionFuture.cancel(true)`, and then wait for a preempted result.

- The server publishes incremental feedback after each step, checks for client cancellation while the goal is running, and returns either a successful or a preempted terminal result.
- The client logs information such as status transitions, feedback updates, and results to visualize the ActionLib lifecycle in the console output.

Both `Main` and `MainExternal` start the ActionLib server before the ActionLib client so the standalone and external-roscore demos behave the same way.

## Using an external roscore
It is possible to use rosjava to run rosjava nodes in an environment where a ros system is already running. E.g. a cpp noetic ros instance.
An example on how to run rosjava nodes programmatically without starting roscore is provided in [MainExternal](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example/blob/main/src/main/java/eu/spyros/koukas/ros/examples/MainExternal.java)
Note that if a roscore is not running, then this example will not run correctly.
To use it, do the following:

1. Set `ROS_MASTER_URI` and `ROS_IP` environment variables. Both these variables are needed. If these environment variables are missing, you will see some errors in step 4 below.
The following commands assumes the example and roscore run in `127.0.0.1` aka `localhost`.   
`export ROS_MASTER_URI=http://127.0.0.1:11311`   
`export ROS_IP=127.0.0.1`   
On Windows PowerShell the equivalent commands are:   
`$env:ROS_MASTER_URI='http://127.0.0.1:11311'`   
`$env:ROS_IP='127.0.0.1'`   

2. Start roscore
Run the following:
`roscore`   

3. With the roscore already started externaly run [MainExternal](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example/blob/main/src/main/java/eu/spyros/koukas/ros/examples/MainExternal.java)
4. You can start directly the [MainExternal](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example/blob/main/src/main/java/eu/spyros/koukas/ros/examples/MainExternal.java) from gradle by running:
`./gradlew runWithExternalRos`
On Windows PowerShell use:
`.\gradlew.bat runWithExternalRos`
