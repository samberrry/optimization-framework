## About
#### Directory structure is as follows:

The **Simulator** directory contains the dependencies required by the optimization framework module.

The **Dependencies** directory contains all of the dependencies (Jar files) which are used to run the whole project.

The **Optimization-framwork** directory is the project root for the developed optimization framework.

#### How ro run..
1. With **Intellj Idea** (recommended):
    - Open up the project with Intellij Idea from  **optimization-framework** directory (This directory contains .iml file)
    - Next, you should configure your JDK settings (make sure to do this step correctly)
2. With your favorite IDE: 
    - Import four different modules in a java project: three of them resides in **simulator** directory and our framework resides in **optimization-framework**
    - Some of these modules depend on each other. Define these dependencies
    - Import library dependencies to the project (Jar files in **dependencies** directory)
    - This project requires JDK 1.8 and higher
