package model

import Elevator._

/**
  * Basic interface for an elevator
  */
trait Elevator {
  val id: ElevatorID

  /**
    * Mutable elevator state
    */
  var elevatorState: ElevatorState

  /**
    * Simulate an elevator step
    *
    */
  def step(): Unit

  /**
    * Is the elevator in a state where the goal floor is reached
    *
    * @param state
    * @return
    */
  def isGoalFloorReached(state: ElevatorState): Boolean =
    state.currentFloor == state.goalFloor

}

object Elevator {

  /**
    * Typesafe elevator id as value class to avoid runtime object allocation overhead
    *
    * @param underlying
    */
  case class ElevatorID(underlying: Int) extends AnyVal

  /**
    * Type FloorID
    */
  type FloorID = Int

  /**
    * Typed description of elevator directions
    */
  sealed trait Direction
  case object Up extends Direction
  case object Down extends Direction
  case object NoDirection extends Direction

  /**
    * ElevatorRide represents a pickup request with the possibility that every person
    * wants to go to a different Floor. The movement direction can be derived by (to - from)
    *
    * @param from
    * @param to
    */
  case class ElevatorRide(from: FloorID, to: FloorID) {
    val direction = if (from < to) Up else if (from > to) Down else NoDirection
  }

  /**
    * ElevatorState represents the state of an elevator
    *
    * @param currentFloor
    * @param goalFloor
    * @param direction
    * @param pickUpQueue
    * @param dropOffQueue
    */
  case class ElevatorState(currentFloor: FloorID, goalFloor: FloorID, direction: Direction, pickUpQueue: Seq[ElevatorRide], dropOffQueue: Seq[FloorID])
}