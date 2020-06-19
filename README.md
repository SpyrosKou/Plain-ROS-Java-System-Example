# Plain-ROS-Java-System-Example

ROS Java contains a plain java, standalone version of roscore that does not require any ROS instalation to be present.
This is an example ROS system running completely on Java with no ROS instalation required.
It is an example that demonstrates running a roscore  and two nodes.
A java publisher and a java subscriber.

This example project demonstrates how to:
3. Create, start and shutdown the Java roscore 
3. Create, start and shutdown ROS nodes programmatically through the ROS Java API.

##Requirements
In order to compile and run this project GIT and JDK need to be installed.

##Quick Instructions for Windows Power Shell
1. Clone the repository:
`git clone https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git`

1. Go into the cloned repository directory:
`cd .\Plain-ROS-Java-System-Example\`

1. Compile the project and prepare for running:
`gradlew installDist`

1. Run the project using the generated script:
`.\build\install\Plain-ROS-Java-System-Example\bin\Plain-ROS-Java-System-Example.bat`

##Quick Instructions for Linux
2. Clone the repository:
`git clone https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git`

2. Go into the cloned repository directory:
`cd Plain-ROS-Java-System-Example/`

2. Add execute permission to gradlew script:
`sudo chmod +x gradlew`

2. Compile the project and install it locally:
`gradlew installDist`

2. Run the project using the generated script:
`./build/install/Plain-ROS-Ja-System-Example/bin/Plain-ROS-Java-System-Example`

 