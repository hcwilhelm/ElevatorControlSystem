package model

import model.Elevator.{ElevatorRide, ElevatorState, ElevatorID}

/**
  * Basic interface of an elevator control system
  *
  * The control system should provide an interface for
  *
  * 1. Querying the state of the elevators (what floor are they on and where they are going)
  * 2. Receiving an up update about the status of an elevator
  * 3. Receiving a pickup request
  * 4. Time-stepping simulation
  */
trait ElevatorControlSystem {

  /**
    * Query the state of all elevators in the system
    * @return
    */
  def status(): Map[ElevatorID, ElevatorState]

  /**
    * Schedules a new pickup request to an elevator
    *
    * @param ride
    */
  def pickup(ride: ElevatorRide): Unit

  /**
    * Force a step of the simulation and move all elevators
    */
  def step(): Unit
}

object ElevatorControlSystem {

  /**
    * Base trait for commands witch can be send to the control system actor
    */
  sealed trait Command

  /**
    * Commands witch can be send to the control system actor
   */
  case object Status extends Command
  case object Step extends Command
  case class PickUp(ride: ElevatorRide) extends Command
  case class Update(id: ElevatorID, state: ElevatorState) extends Command
}