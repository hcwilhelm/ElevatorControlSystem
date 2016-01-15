ElevatorControlSystem
---

###Task
Design and implement an elevator control system. What data structures, interfaces and algorithms will you need? Your
elevator control system should be able to handle a few elevators — say up to 16.

Can you please use Scala to implement it? In the end, your control system should provide an interface for:

1. Querying the state of the elevators (what floor are they on and where they are going),
2. receiving an update about the status of an elevator,
3. receiving a pickup request,
4. time-stepping the simulation.

###Implementation

###Project Structure

Main code in src/main/scala
Test code in src/test/scala

Interfaces (traits), domain specific case classes and objects are located in the package model. The actor
implementations can be found in package actors.

####Build and Run
The solution is written in scala with one Main object in the root of the project. It can be execute by running

```
sbt run
```

Tests can be executed by

```
sbt test
```

The main method shows one example simulation where multiple pick up, step and status commands happen over time.


