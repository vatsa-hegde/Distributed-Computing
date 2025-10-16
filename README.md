# Distributed-Computing

This repository contains five CS projects demonstrating distributed systems, big data, mobile agents, and graph analysis. Each project is self-contained in its folder.

---

## Projects

### 1. Distributed Tic-Tac-Toe
- **Description:** Networked Tic-Tac-Toe allowing two players to play on different machines with synchronized game state.
- **Tech:** Java, RMI, Sockets, Multithreading
- **Run:** Start server and clients via Java commands in the project folder.

### 2. Flight Data Filtering with Apache Storm
- **Description:** Real-time flight data filtering based on altitude, vertical rate, and proximity to airports.
- **Tech:** Java, Apache Storm, RMI
- **Run:** Deploy topology using Storm and feed flight data from `flights.txt`.

### 3. Mobile Agent Platform
- **Description:** Java RMI-based mobile agents that migrate between hosts, communicate via shared Places, and spawn child agents.
- **Tech:** Java, RMI, Multithreading
- **Run:** Start Place servers, then deploy agents to move across hosts.

### 4. Hazelcast-based Distributed Document Indexing
- **Description:** Distributed indexing system with web interface for keyword search and retrieval.
- **Tech:** Java, Hazelcast, Tomcat, Servlets
- **Run:** Start Hazelcast nodes, deploy servlet on Tomcat, and use the web form to search.

### 5. Variable Graph Analysis and Task Execution
- **Description:** Task execution simulation on variable-sized graphs with visual output showing dependencies.
- **Tech:** Java, Multithreading, Swing
- **Run:** Compile and run the main Java class to see graph execution visualization.

---

## Common Skills Demonstrated
- Java, Multithreading
- Distributed Systems (RMI, Hazelcast)
- Real-time data processing (Apache Storm)
- Mobile Agents and Concurrency
- Web interface with Java Servlets
- Graph-based task analysis and visualization

---

## How to Run
Each project folder contains instructions to compile and run.  
Typical steps:
```bash
javac *.java
java <MainClass>
