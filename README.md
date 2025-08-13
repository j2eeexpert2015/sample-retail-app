# README

## 🧭 Project Overview
This repo contains a Spring Boot demo for comparing **platform threads** vs **virtual threads** under load and for quick JMeter experiments. You can flip virtual threads on/off and change the HTTP port **without** modifying `application.properties`.

## ▶️ Quick Run (Disable Virtual Threads, Use Port 8081)
Use this when you want a **baseline with platform threads** and to run on a **non-default port (8081)**.

    # Run Spring Boot with virtual threads DISABLED and server port set to 8081 (works on Windows/macOS/Linux)
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=false --server.port=8081"

- This passes **Spring Boot application arguments** (not JVM `-D` flags).
- Same command works in **Windows (CMD/PowerShell)** and **macOS/Linux**.

### 📈 JMeter CLI: Run & Generate HTML Report
Windows (single line, write **JTL** to **..\jtl** and **HTML report + log** to **..\report**)

    jmeter -n -t Rest_Call_Test_Plan.jmx -l ..\jtl\Rest_Call_Test_Plan.jtl -j ..\report\jmeter.log -e -o ..\report\Rest_Call_Test_Report -f

## Flags:
- -n non-GUI mode
- -t test plan (.jmx)
- -l results (.jtl)
- -j JMeter run log
- -e -o dir_name generate HTML dashboard
- -f overwrite output dir
### Optional Variants

    # Default port (8080), virtual threads ENABLED (JDK 21+)
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=true"

    # Pass as JVM system properties (alternative approach)
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.threads.virtual.enabled=false -Dserver.port=8081"

## 🔌 Endpoints (examples)
- GET `/order/hello` → simple health/demo endpoint
- GET `/order/place?productId=123` → demo flow that calls a service (e.g., DB or simulated I/O)

## 📋 Features
- Toggle **virtual threads** on/off via app args
- Compare throughput/latency under blocking vs non-blocking handlers
- Ready for **JMeter** closed-loop load tests
- Simple controller/service setup for stepwise demos (hello → DB/mock I/O)

## 🧰 Tech Used
- **Java 21**
- **Spring Boot 3.x** (Tomcat NIO)
- **Maven**
- **JUnit 5** (if tests present)
- **JMeter** (external, optional for load tests)

## ✅ Prerequisites
- JDK **21**
- Maven **3.9+**
- Docker (optional, if you later use Testcontainers or Dockerized DB)
- JMeter (optional, for load testing)

## 🚀 Build & Run

    # Build
    mvn clean verify

    # Run (baseline: platform threads on 8081)
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=false --server.port=8081"

    # Run (virtual threads on 8080)
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=true"

## 🧪 Quick JMeter Recipe
- Charts to watch: **Active Threads Over Time**, **Response Times Over Time**, **Transactions Per Second**



## 🔍 Working with `jdk.internal.vm.Continuation` in IntelliJ IDEA
`jdk.internal.vm.Continuation` is an internal JDK API used by Project Loom to implement virtual threads. If you explore it directly, allow access via VM options.

### Step 1: Add VM Options (Run/Debug Configuration → VM Options)

    --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
    --enable-preview

Notes:
- `--enable-preview` is required only on **JDK 19/20**.
- On **JDK 21+**, virtual threads are stable; keep `--enable-preview` only if you use other preview features.

### Step 2: Debugger Additional Command Line Parameters

    --enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED

This lets the debugger step into `Continuation` frames if you dive deep.

## 👤 About the Instructor
[![Ayan Dutta - Instructor](https://img-c.udemycdn.com/user/200_H/5007784_d6b8.jpg)](https://www.udemy.com/user/ayandutta/)
Hi, I’m **Ayan Dutta**, a Software Architect, Instructor, and Content Creator. I create practical, hands-on courses on **Java, Spring Boot, Debugging, Git, Python**, and more.

## 🌐 Connect With Me
- Slack Group: https://join.slack.com/t/learningfromexp/shared_invite/zt-1fnksxgd0-_jOdmIq2voEeMtoindhWrA
- After joining, go to the **#java-virtual-threads-and-structured-concurrency** channel
- Email: j2eeexpert2015@gmail.com
- YouTube: https://www.youtube.com/@learningfromexperience
- Medium: https://medium.com/@mrayandutta
- LinkedIn: https://www.linkedin.com/in/ayan-dutta-a41091b/

## 📺 Subscribe on YouTube
[Watch on YouTube](https://www.youtube.com/@learningfromexperience)

## 📚 Explore My Udemy Courses

### Java Debugging Courses with Eclipse, IntelliJ IDEA, and VS Code
<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/eclipse-debugging-techniques-and-tricks">
        <img src="https://img-c.udemycdn.com/course/480x270/417118_3afa_4.jpg" width="250"><br/>
        <b>Eclipse Debugging Techniques</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/java-debugging-with-intellij-idea">
        <img src="https://img-c.udemycdn.com/course/480x270/2608314_47e4.jpg" width="250"><br/>
        <b>Java Debugging With IntelliJ</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/java-debugging-with-visual-studio-code-the-ultimate-guide">
        <img src="https://img-c.udemycdn.com/course/480x270/5029852_d692_3.jpg" width="250"><br/>
        <b>Java Debugging with VS Code</b>
      </a>
    </td>
  </tr>
</table>

### Java Productivity & Patterns
<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/intellij-idea-tips-tricks-boost-your-java-productivity">
        <img src="https://img-c.udemycdn.com/course/480x270/6180669_7726.jpg" width="250"><br/>
        <b>IntelliJ IDEA Tips & Tricks</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/design-patterns-in-javacreational">
        <img src="https://img-c.udemycdn.com/course/480x270/779796_5770_2.jpg" width="250"><br/>
        <b>Creational Design Patterns</b>
      </a>
    </td>
  </tr>
</table>

### Python Debugging Courses
<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/learn-python-debugging-with-pycharm-ide">
        <img src="https://img-c.udemycdn.com/course/480x270/4840890_12a3_2.jpg" width="250"><br/>
        <b>Python Debugging With PyCharm</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/python-debugging-with-visual-studio-code">
        <img src="https://img-c.udemycdn.com/course/480x270/5029842_d36f.jpg" width="250"><br/>
        <b>Python Debugging with VS Code</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/get-started-with-python-debugging-in-visual-studio-code">
        <img src="https://img-c.udemycdn.com/course/480x270/6412275_a17d.jpg" width="250"><br/>
        <b>Python Debugging (Free)</b>
      </a>
    </td>
  </tr>
</table>

### Git & GitHub Courses
<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/getting-started-with-github-desktop">
        <img src="https://img-c.udemycdn.com/course/480x270/6112307_3b4e_2.jpg" width="250"><br/>
        <b>GitHub Desktop Guide</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/learn-to-use-git-and-github-with-eclipse-a-complete-guide">
        <img src="https://img-c.udemycdn.com/course/480x270/3369428_995b.jpg" width="250"><br/>
        <b>Git & GitHub with Eclipse</b>
      </a>
    </td>
  </tr>
</table>
