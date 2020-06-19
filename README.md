# [Plain ROS Java System Example Project](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git)

ROS Java contains a plain java, standalone version of roscore that does not require any ROS instalation to be present.
This is an example ROS system running completely on Java with no ROS instalation required.
It is an example that demonstrates running a roscore  and two nodes.
A java publisher and a java subscriber.

This example project demonstrates how to do the following **programmatically** through the ROS Java API:
1. Create, start and shutdown the Java roscore 
1. Create, start and shutdown ROS nodes
1. Publish and subscribe to a ROS topic

##Requirements

In order to compile and run this project only jave needs to be installed. [GIT](https://git-scm.com/downloads) also makes getting the source very easy. Links are provided for convenience.
- [GIT](https://git-scm.com/downloads) , in order to clone the [project repository](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git)
- JDK, the [project](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git) has been developed and tested using [JDK 14](https://jdk.java.net/14/)
 

##Quick Instructions for Windows Power Shell
2. [Clone](https://git-scm.com/docs/git-clone) the [project repository](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git):
`git clone https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git`

2. Go into the cloned repository directory:
`cd .\Plain-ROS-Java-System-Example\`

2. Compile the project and prepare for running:
`gradlew installDist`

2. Run the project using the generated script:
`.\build\install\Plain-ROS-Java-System-Example\bin\Plain-ROS-Java-System-Example.bat`

##Quick Instructions for Linux
3. [Clone](https://git-scm.com/docs/git-clone) the [project repository](https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git):
`git clone https://github.com/SpyrosKou/Plain-ROS-Java-System-Example.git`

3. Go into the cloned repository directory:
`cd Plain-ROS-Java-System-Example/`

3. [Add execute permission](http://manpages.ubuntu.com/manpages/focal/man1/chmod.1.html) to gradlew script:
`sudo chmod +x gradlew`

3. Compile the project and install it locally:
`gradlew installDist`

3. Run the project using the generated script:
`./build/install/Plain-ROS-Ja-System-Example/bin/Plain-ROS-Java-System-Example`

 