package actors

import akka.actor._
import model.Elevator
import model.Elevator._
import model.ElevatorControlSystem.{PickUp, Status, Step, Update}

/**
  * Actor implementation af an elevator.
  *
  * Commands handled
  *
  * PickUp : To enqueue an ElevatorRide
  * Step   : To trigger the simulation of one elevator step
  *
  * When the elevator is moving to its next goal it will pickup and
  * drop off all passengers witch want to enter or leave the elevator
  * at its current floor.
  *
  * When the elevator has not pickups or drop offs left it will
  * stop moving and stay at its current floor.
  *
  * After a simulation step the actor will send the new elevator state back to
  * the sender witch is the ElevatorControllSystem actor.
  *
  * @param id
  */
class ElevatorActor(val id: ElevatorID) extends Actor with ActorLogging with Elevator {

  /**
    * Initial elevator state
    */
  var elevatorState = ElevatorState(0, 0, NoDirection, Vector.empty, Vector.empty)

  /**
    * Simulate an elevator step
    *
    */
  override def step() =
    elevatorState = (pickUp andThen dropOff andThen nextGoal andThen move)(elevatorState)

  /**
    * Function to pickup all passengers waiting at the current floor and
    * enqueue there drop off locations to the drop off queue
    */
  val pickUp: ElevatorState => ElevatorState = { state =>
    val dropOffQueue = state.dropOffQueue ++ state.pickUpQueue.filter(_.from == state.currentFloor).map(_.to)
    val pickUpQueue = state.pickUpQueue.filter(_.from != state.currentFloor)

    state.copy(pickUpQueue = pickUpQueue, dropOffQueue = dropOffQueue)
  }

  /**
    * Function to drop off all passengers who want to leave at the current floor
    */
  val dropOff: ElevatorState => ElevatorState = { state =>
    val dropOffQueue = state.dropOffQueue.filter(_ != state.currentFloor)
    state.copy(dropOffQueue = dropOffQueue)
  }

  /**
    * Function to find the next goal floor with is
    *
    * 1. The first drop off location if the drop off queue is non empty
    * 2. The first pickup location if the pickup queue is non empty
    * 3. The current floor if the is nothing to move to
    */
  val findNextGoal: ElevatorState => ElevatorState = { state =>
    val newGoalFloor = state.dropOffQueue.headOption orElse state.pickUpQueue.headOption.map(_.from) getOrElse state.currentFloor
    state.copy(goalFloor = newGoalFloor)
  }

  /**
    * Function to determine the new movement direction after a
    * new goal floor was found
    */
  val changeDirection: ElevatorState => ElevatorState = { state =>
    if (state.goalFloor > state.currentFloor)
      state.copy(direction = Up)
    else if (state.goalFloor < state.currentFloor)
      state.copy(direction = Down)
    else
      state.copy(direction = NoDirection)
  }

  /**
    * Function to find a new goal floor and change the elevators
    * movement direction
    */
  val nextGoal: ElevatorState => ElevatorState = { state =>
    if (isGoalFloorReached(state))
      (findNextGoal andThen changeDirection)(state)
    else
      state
  }

  /**
    * Function to move the elevator one step in the current direction
    */
  val move: ElevatorState => ElevatorState = { state =>
    state.direction match {
      case Up           => state.copy(currentFloor = state.currentFloor + 1)
      case Down         => state.copy(currentFloor = state.currentFloor - 1)
      case NoDirection  => state
    }
  }

  /**
    * Enqueue a ride in the elevators pickup queue
    *
    * @param ride
    */
  def enqueueRide(ride: ElevatorRide): Unit =
    elevatorState = elevatorState.copy(pickUpQueue = elevatorState.pickUpQueue :+ ride)

  /**
    * The actors receive method
    *
    * @return
    */
  override def receive: Receive = {
    case PickUp(ride) => enqueueRide(ride)
    case Step => {
      step()
      sender ! Update(id, elevatorState)
    }
    case Status => sender ! Update(id, elevatorState)
    case msg => log.error(s"Cannot handle message $msg")
  }

}
