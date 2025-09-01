# 👤 About the Instructor

[![Ayan Dutta - Instructor](https://img-c.udemycdn.com/user/200_H/5007784_d6b8.jpg)](https://www.udemy.com/user/ayandutta/)

Hi, I'm **Ayan Dutta**, a Software Architect, Instructor, and Content Creator.  
I create practical, hands-on courses on **Java, Spring Boot, Debugging, Git, Python**, and more.

---

## 🌐 Connect With Me

- 💬 Slack Group: [Join Here](https://join.slack.com/t/learningfromexp/shared_invite/zt-1fnksxgd0-_jOdmIq2voEeMtoindhWrA)
- 📢 After joining, go to the **#java-virtual-threads-and-structured-concurrency** channel
- 📧 Email: j2eeexpert2015@gmail.com
- 🔗 YouTube: [LearningFromExperience](https://www.youtube.com/@learningfromexperience)
- 📝 Medium Blog: [@mrayandutta](https://medium.com/@mrayandutta)
- 💼 LinkedIn: [Ayan Dutta](https://www.linkedin.com/in/ayan-dutta-a41091b/)

---

## 📺 Subscribe on YouTube

[![YouTube](https://img.shields.io/badge/Watch%20on%20YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/@learningfromexperience)

---

## 📚 Explore My Udemy Courses

### 🧩 Java Debugging Courses with Eclipse, IntelliJ IDEA, and VS Code

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

### 💡 Java Productivity & Patterns

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

### 🐍 Python Debugging Courses

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

### 🛠 Git & GitHub Courses

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

---

## 🧭 Spring Boot Virtual Threads Demo App

This repository contains a **production-ready Spring Boot application** for comparing **platform threads** vs **virtual threads** under load with comprehensive **JFR (Java Flight Recorder)** monitoring and real-time metrics visualization. You can flip virtual threads on/off and change the HTTP port **without** modifying `application.properties`.

### ⚙️ Requirements

- **JDK 21** (virtual threads are stable)
- **Maven 3.9+** for building projects
- **Docker** (for monitoring stack - Prometheus, Grafana)
- **JMeter** (optional, for load testing)

### 📥 Getting Started

Clone the repository and navigate to the project directory:

```bash
git clone https://github.com/j2eeexpert2015/sample-retail-app.git
cd sample-retail-app
```

## ▶️ Quick Run (Disable Virtual Threads, Use Port 8081)

Use this when you want a **baseline with platform threads** and to run on a **non-default port (8081)**:

```bash
# Run Spring Boot with virtual threads DISABLED and server port set to 8081 (works on Windows/macOS/Linux)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=false --server.port=8081"
```

- This passes **Spring Boot application arguments** (not JVM `-D` flags).
- Same command works in **Windows (CMD/PowerShell)** and **macOS/Linux**.

### Optional Variants

```bash
# Default port (8080), virtual threads ENABLED (JDK 21+)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=true"

# Pass as JVM system properties (alternative approach)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.threads.virtual.enabled=false -Dserver.port=8081"
```

## 📊 Start Monitoring Stack

```bash
# Start Prometheus + Grafana
docker-compose up -d

# Access Grafana dashboard
open http://localhost:3000/d/virtual-thread-metrics
```

**Login credentials:** `admin` / `admin123`

## 🔌 Endpoints

- GET `/order/hello` → simple health/demo endpoint
- GET `/order/placeorder/{productId}` → demo flow that calls a service (e.g., DB or simulated I/O)
- GET `/jfr` → JFR recording control dashboard
- GET `/virtual-thread-test-dashboard.html` → Virtual thread test runner UI

## 🎯 JFR Monitoring Features

### Automatic Monitoring
- **Real-time JFR streaming** → Prometheus metrics
- **Spring Boot lifecycle integration**
- **Configurable via properties** (`jfr.enabled=true/false`)

### Manual Control
- **REST API** for on-demand recordings
- **File-based recordings** for JDK Mission Control analysis
- **Independent of automatic monitoring**

### Virtual Thread Testing
- **Burst tests** (mass VT creation)
- **Pinning simulation** (synchronized blocks)
- **Sustained workloads** (long-running VTs)

## 🏗️ Architecture

### Core JFR Components

#### 1. **JFRVirtualThreadUtil** (Framework-agnostic)
```java
// Pure Java utility - no Spring dependencies
public class JFRVirtualThreadUtil {
    // Creates JFR recordings with VT events enabled
    public static Recording createVirtualThreadRecording(String name)
    
    // Sets up real-time event streaming
    public static RecordingStream createEventStream(...)
    
    // File operations and resource management
    public static Path saveRecording(Recording recording, String outputDir)
}
```

#### 2. **JFRVirtualThreadListener** (Spring Integration)
```java
@Component
public class JFRVirtualThreadListener {
    // Automatic lifecycle management
    @EventListener(ApplicationReadyEvent.class)
    public void startJFRMonitoring()
    
    // Prometheus metrics export
    private final Counter startCounter;
    private final Timer pinnedTimer;
}
```

#### 3. **JFRController** (Manual Control)
```java
@RestController
@RequestMapping("/jfr")
public class JFRController {
    // REST API for manual JFR control
    @GetMapping("/start") // Start recording
    @GetMapping("/stop")  // Stop & save to file
    @GetMapping("/status") // Check recording state
}
```

## 📊 Monitoring Stack

```mermaid
graph LR
    A[JFR Events] --> B[JFRVirtualThreadListener]
    B --> C[Prometheus Metrics]
    C --> D[Grafana Dashboard]
    
    E[Manual JFR] --> F[JFRController]
    F --> G[.jfr Files]
    G --> H[JDK Mission Control]
```

### Exported Metrics
- `jfr_virtual_thread_starts_total` - VT creations
- `jfr_virtual_thread_ends_total` - VT completions
- `jfr_virtual_thread_pinned_seconds` - Pinning duration histogram
- `jfr_virtual_thread_pinned_events_total` - Total pinning events

## 📈 JMeter CLI: Run & Generate HTML Report

Windows (single line, write **JTL** to **..\jtl** and **HTML report + log** to **..\report**):

```bash
jmeter -n -t Rest_Call_Test_Plan.jmx -l ..\jtl\Rest_Call_Test_Plan.jtl -j ..\report\jmeter.log -e -o ..\report\Rest_Call_Test_Report -f
```

**Flags:**
- `-n` non-GUI mode
- `-t` test plan (.jmx)
- `-l` results (.jtl)
- `-j` JMeter run log
- `-e -o dir_name` generate HTML dashboard
- `-f` overwrite output dir

### 🧪 Quick JMeter Recipe
Charts to watch: **Active Threads Over Time**, **Response Times Over Time**, **Transactions Per Second**

## 🎮 User Interfaces

### 1. JFR Dashboard (`/jfr` → `jfr-dashboard.html`)
Manual JFR recording control with these key features:

```html
<!-- Manual JFR recording control -->
<button onclick="handleJFRAction('/jfr/start')">▶️ Start Recording</button>
<button onclick="handleJFRAction('/jfr/stop')">⏹️ Stop Recording</button>
```

### 2. VT Test Runner (`/virtual-thread-test-dashboard.html`)
Virtual thread event generation for testing:

```html
<!-- Virtual thread event generation -->
<button onclick="runTest('burst', 'count=500')">🚀 Burst Test</button>
<button onclick="runTest('pinning', 'count=10')">📌 Pinning Test</button>
```

## ⚙️ Configuration

### Enable/Disable Virtual Threads
```properties
# Toggle between platform and virtual threads
spring.threads.virtual.enabled=true

# JFR monitoring control (IMPORTANT: Set to true for event streaming)
jfr.enabled=true
jfr.output.dir=jfr-recordings
```

### Quick Start Commands
```bash
# Platform threads (baseline)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=false --server.port=8081"

# Virtual threads (comparison)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=true"
```

## ⚠️ Important Configuration Notes

### JFR Event Streaming
For real-time metrics to work, ensure `jfr.enabled=true` in `application.properties`:

```properties
# CRITICAL: Enable JFR for event streaming
jfr.enabled=true

# Other JFR settings
jfr.output.dir=jfr-recordings
jfr.recording.name=SpringBootApp
```

### Virtual Thread Configuration
```properties
# Enable/disable virtual threads
spring.threads.virtual.enabled=true

# Security credentials for /security-demo endpoints
spring.security.user.name=demouser
spring.security.user.password=demo123
```

## 🔍 Working with `jdk.internal.vm.Continuation` in IntelliJ IDEA

`jdk.internal.vm.Continuation` is an internal JDK API used by Project Loom to implement virtual threads. If you explore it directly, allow access via VM options.

### Step 1: Add VM Options (Run/Debug Configuration → VM Options)

```bash
--add-exports java.base/jdk.internal.vm=ALL-UNNAMED
--enable-preview
```

**Notes:**
- `--enable-preview` is required only on **JDK 19/20**.
- On **JDK 21+**, virtual threads are stable; keep `--enable-preview` only if you use other preview features.

### Step 2: Debugger Additional Command Line Parameters

```bash
--enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
```

This lets the debugger step into `Continuation` frames if you dive deep.

## 🚀 Build & Run

```bash
# Build
mvn clean verify

# Run (baseline: platform threads on 8081)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=false --server.port=8081"

# Run (virtual threads on 8080)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.threads.virtual.enabled=true"
```

## 📋 Key Features

- Toggle **virtual threads** on/off via app args
- Compare throughput/latency under blocking vs non-blocking handlers
- Ready for **JMeter** closed-loop load tests
- **Comprehensive JFR monitoring** with real-time metrics
- **Manual JFR recording control** via REST API
- **Interactive web dashboards** for testing and monitoring

## 🧰 Tech Stack

- **Java 21** with virtual threads
- **Spring Boot 3.x** (Tomcat NIO)
- **JFR** for low-overhead monitoring
- **Micrometer + Prometheus** for metrics
- **Grafana** for visualization
- **Docker Compose** for monitoring stack
- **Maven**
- **JMeter** (external, optional for load tests)

## 🐛 Troubleshooting

### No Metrics in Grafana?
1. Check `jfr.enabled=true` in `application.properties`
2. Verify virtual threads are enabled: `spring.threads.virtual.enabled=true`
3. Look for startup logs: `🚀 Starting JFR Virtual Thread monitoring`
4. Test endpoints: `http://localhost:8080/virtual-thread-test-dashboard.html`

### JFR Dashboard Issues?
1. Navigate to: `http://localhost:8080/jfr-dashboard.html`
2. Check JFR status via the dashboard
3. Ensure JDK 21+ is being used

### Virtual Thread Test UI?
- Correct URL: `http://localhost:8080/virtual-thread-test-dashboard.html`
- Check browser console for JavaScript errors
- Verify controller endpoints are responding: `/vt-demo/burst`, `/vt-demo/pinning`, `/vt-demo/sustained`

## 📖 Detailed Technical Documentation

1. **Start Application** with desired VT configuration
2. **Automatic Monitoring** begins (real-time Prometheus metrics)
3. **Manual Recording** via `/jfr/start` for detailed analysis
4. **Run Workloads** using VT test runner or JMeter
5. **View Real-time** metrics in Grafana dashboard
6. **Stop Recording** via `/jfr/stop` → saves `.jfr` file
7. **Deep Analysis** using JDK Mission Control

## 🎯 Use Cases

### Performance Testing
- **JMeter integration** with different thread models
- **Load testing** with configurable VT settings
- **Bottleneck identification** through pinning detection

### Development & Debugging
- **On-demand recordings** during development
- **Real-time monitoring** of VT behavior
- **Educational demonstrations** of VT vs platform threads

### Production Monitoring
- **Continuous metrics export** to monitoring systems
- **Pinning alerts** for performance issues
- **Resource usage tracking**

## 🛠️ Technology Stack

- **Java 21** with virtual threads
- **Spring Boot 3.x** with Tomcat NIO
- **JFR** for low-overhead monitoring
- **Micrometer + Prometheus** for metrics
- **Grafana** for visualization
- **Docker Compose** for monitoring stack
- **Maven** for build management

## 📁 Project Structure

```
src/main/java/com/example/retail/
├── jfr/
│   ├── JFRVirtualThreadUtil.java      # Core JFR operations
│   └── JFRVirtualThreadListener.java  # Spring integration
├── controller/
│   ├── JFRController.java             # Manual JFR control
│   └── VirtualThreadEventSimulationController.java
└── config/
    └── VirtualThreadConfig.java       # VT configuration

src/main/resources/static/
├── jfr-dashboard.html                 # JFR control UI
├── virtual-thread-test-dashboard.html # VT testing UI
└── css/ & js/                         # Styling & JavaScript
```

## 🎯 Course Companion Repository

This repository is designed to complement the **"Java Virtual Threads & Structured Concurrency w/ Spring Boot"** Udemy course. This provides the production Spring Boot examples while the core concepts are covered in the companion repository:

**[virtual-threads-structured-concurrency](https://github.com/j2eeexpert2015/virtual-threads-structured-concurrency)**

---

This application provides a complete toolkit for understanding, testing, and monitoring virtual thread performance in real-world Spring Boot scenarios.

**Happy Learning! 🚀**
