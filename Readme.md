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

The main method shows one example simulation where multiple pick up, step and status commands happen in
sequential order at the end the actor system is terminated and the programm stops running.

###Implementation details

The elevator controll system and the elevators are implemented as akka actors. All elevators are started as
child actors of the elevator controll system end send an update messages to the system eveytime there internal
state has changed. In this simulation the control system waits for all elevators to perfom a step so all elevators
move syncronized. But it would also be possible to have all elevators move independly from each other and then we
won't need to wait until all elevators have done there step.

The ElevatorControlSystemActor schedules new pickup request to an alevator in an optimized
way it trys to find the elevator witch is moing towards the pickup location and witch has the neareast floor
distance to pickup this ride as soon as possible. It would also be possible to schedule the pickup requests
in a round robin, randomized, usage pattern etc. style.

The elevators itself moves towards the first pickup location and then moves to the first drop off location. The
elevator will serve on its way to the next goal floor all pick up and drop off locations. So on each floor the elevator
arrives it will pick up as many passangers as possible and also drop of as many passengers as possible. When the alevator
is at the goal floor and there are no pick ups or drop offs left in the queue it will stop moving and stay at this floor.

Commands witch can be send to the ElevatorControlSystem Actor

```
* PickUp command (signal a new ride from floor to floor)
* Status command (query all elevator states)
* Step command (force all elevators to move one step)
```

Commands witch can be send to the ElevatorActor

```
* PickUp command (enqueu a new ride from floor to floor)
* Status command (query elevator state)
* Step command (force the elevator to move one step)
```

###Possible improvements

* Track how many passengers are loaded to an elevator and do a better load balancing
* When an elevator stops move it to the most likely floor based on usaged patterns
* A better solution to find the next goal floor
* Command line interface for easy interaction or reading command sequence files
* Elevator usage metrics as possible data source for advanced movement strategies
* More tests