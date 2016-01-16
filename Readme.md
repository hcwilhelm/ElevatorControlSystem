# ElevatorControlSystem


## Task

Design and implement an elevator control system. What data structures, interfaces and algorithms
will you need? Your elevator control system should be able to handle a few elevators — say up to 16.

Can you please use Scala to implement it? In the end, your control system should provide an
interface for:

1. Querying the state of the elevators (what floor are they on and where they are going),
2. Receiving an update about the status of an elevator,
3. Receiving a pickup request,
4. Time-stepping the simulation.

## Implementation

### Project Structure

Main code in `src/main/scala`

Test code in `src/test/scala`

Interfaces (traits), domain specific case classes and objects are located in the package model. The
actor implementations can be found in package actors.

### Build and Run

The solution is written in Scala with one Main object in the root of the project. It can be built
and executed by:

```
sbt run
```

Tests can be executed by:

```
sbt test
```

The main method shows one example simulation where multiple pick up, step and status commands happen
in sequential order at the end the actor system is terminated and the program stops running.

### Program output

When the `ElevatorControlSystemActor` receives a `Status` message it will send back a map of
all elevator states. The map has the following type `Map[ElevatorID, ElevatorState]` where
`ElevatorID` and `ElevatorState` has the following structure


```
case class ElevatorID(underlaying: Int) extends AnyVal
```

```
case class ElevatorState(currentFloor: FloorID, goalFloor: FloorID, direction: Direction, pickUpQueue: Seq[ElevatorRide], dropOffQueue: Seq[FloorID])
```

Example output :

```
(ElevatorID(1),ElevatorState(3,5,Up,Vector(ElevatorRide(4,8)),Vector(5)))
(ElevatorID(2),ElevatorState(0,0,NoDirection,Vector(),Vector()))
(ElevatorID(3),ElevatorState(0,0,NoDirection,Vector(),Vector()))
(ElevatorID(4),ElevatorState(0,0,NoDirection,Vector(),Vector()))
```

### Implementation Details

The elevator control system and the elevators are implemented as __Akka__ actors. All elevators are
started as child actors of the elevator control system end send an update message to the system
every time their internal state has changed. In this simulation the control system waits for all
elevators to perform a step so all elevators move synchronized. However, it would also be possible
to have all elevators move independently from each other and then we won't need to wait until all
elevators have done there step.

To solve the problem that each person may want to go to different floor this implementation uses a
modified `PickUp` request witch holds an `ElevatorRide` object to encode where the person wants to
be picked up and and where the person wants to be dropped off.

The `ElevatorControlSystemActor` schedules new pickup request to an elevator in an optimized way. It
tries to find the elevator witch is moving towards the pickup location and which has the nearest
floor distance to pickup this ride as soon as possible. It would also be possible to schedule the
pickup requests in a round robin, randomized, usage pattern etc. style.

The elevators itself moves towards the first pickup location and then moves to the first drop off
location. The elevator will serve on its way to the next goal floor all pick up and drop off
locations. So on each floor the elevator arrives it will pick up as many passengers as possible and
also drops of as many passengers as possible. When the elevator is at the goal floor and there are
no pick ups or drop offs left in the queue it will stop moving and stay at this floor.

Commands witch can be send to the `ElevatorControlSystem` actor

```
* PickUp command (signal a new ride from floor to floor)
* Status command (query all elevator states)
* Step command (force all elevators to move one step)
```

Commands witch can be send to the `ElevatorActor`

```
* PickUp command (enqueu a new ride from floor to floor)
* Status command (query elevator state)
* Step command (force the elevator to move one step)
```

### Possible Improvements

* Track how many passengers are loaded into an elevator and use this information to implement load balancing
* When an elevator stops, move it to the floor that will most likely have new passengers
* A better solution to find the next goal floor
* Command line interface for easy interaction or reading command sequence files
* Elevator usage metrics as possible data source for advanced movement strategies
* More tests
