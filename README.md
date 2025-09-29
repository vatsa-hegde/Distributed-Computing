# Distributed-Computing
This repository contains five programming assignments completed as part of the Distributed Computing course. Each project explores concepts in distributed systems, parallel processing, and scalability, with additional features implemented beyond the core requirements. Every folder has it's own report tagged to it. Please refer to the report if any information is required.

## 📂 Projects Overview

### Project 1 – Introduction to Distributed Systems
Set up environment and explored basic distributed system concepts.
Small-scale experiments with message passing and coordination.
Built a TicTacToe with single and multiplayer optons connected through a distributed network.

### Project 2 – Real-Time Flight Data Analysis with Apache Storm
Implemented a Storm topology to process live-like flight data.

Components:
- HubIdentifier bolt: grouped flights by geographic square-region (1° latitude/longitude).
- AirlineSorter bolt: sorted/grouped flights by airlines.
- Focused on stream processing and parallel bolt execution.

### Project 3 – Agent-Based Simulation (Turtle Movement)
Simulated a turtle moving in a 10×10 grid using probabilistic modeling.
Additional feature: 
1. indirect inter-agent communication via Place
2. implemented with ConcurrentHashMap message queues.
3. Showed how agents can coordinate without direct messaging.

### Project 4 – Distributed Indexing with Hazelcast
Built an inverted index across distributed nodes using Hazelcast.

Additional features:
1. Mobile Agents – deployed and executed on specific nodes.
2. Hierarchical Dispatch – parent agent (HierarchicalIndexingAgent) delegated tasks to child agents (ChildIndexingAgent).
3. Web Interface – Java Servlet (Tomcat) for keyword search via browser.

### Project 5 – Distributed Graph Processing
Designed a graph processing framework in a distributed environment.
Additional features:
1. Support for variable number of tasks.
2. Configurable graph sizes.
3. Graphical output of results.

## 🚀 Technologies Used
- Java (core implementation),
- Apache Storm (stream processing),
- Hazelcast (distributed in-memory data grid),
- Tomcat + Java Servlets (web interface),
- Concurrent Programming (threads, message queues, agents)


## ✨ Highlights
* Explored real-time data processing, distributed indexing, and agent-based systems.
* Built fault-tolerant, scalable, and modular distributed applications.
* Added advanced features (mobile agents, hierarchical dispatch, web interface, visualization).
