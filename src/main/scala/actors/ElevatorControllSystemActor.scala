package actors

import akka.actor._
import akka.util.Timeout
import model.Elevator._
import model.ElevatorControlSystem.{PickUp, Status, Step, Update}
import akka.pattern.ask

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ElevatorControllSystemActor(numberOfElevators: Int) extends Actor with ActorLogging {

  /**
    * Collect all elevator states
    */
  var elevatorStates: Map[ElevatorID, ElevatorState] = Map.empty

  /**
    * Start numberOfElevators child ElevatorActors and store
    * the actorRefs in a Map
    */
  val elevators: Map[ElevatorID, ActorRef] = {
    val elevatorIDs = 1 to numberOfElevators map ElevatorID
    val actors = elevatorIDs map (id => id -> context.actorOf(Props(new ElevatorActor(id)), s"Elevator-${id.underlying}"))
    actors.toMap
  }

  /**
    * Set implicit Timeout for akka ask pattern
    */
  implicit val timeout = Timeout(5.seconds)

  /**
    * Get initial elevator states and wait for all updates
    */
  val initialUpdate = Future.sequence(elevators.values.map(ref => (ref ? Status).mapTo[Update])) map (_ foreach {
    case Update(id, state) => elevatorStates = elevatorStates + (id -> state)
  })

  Await.ready(initialUpdate, 5.seconds)

  /**
    * Trigger all elevators to perform a Step and wait for there updates.
    *
    * It would be much better not to wait but I am not smart enough to find a
    * better sync model.
    *
    * A better soulution may be a an ElevatorControlSystemActor with two states.
    * Every time a step is performed it will go to a WaitForUpdate state and in that
    * state it will simply queue new command messages which will processed when the
    * actor changes to Idle state.
    *
    * Or we can just run alll elevators in pure async way where each elevator is moving
    * independently from all other elevators.
    *
    * @return
    */
  def sendStepAndWaitForUpdates() = {
    val updates = Future.sequence(elevators.values.map(ref => (ref ? Step).mapTo[Update])) map (_ foreach {
      case Update(id, state) => elevatorStates = elevatorStates + (id -> state)
    })

    Await.ready(updates, 5.seconds)
  }

  /**
    * This actors receive method
    *
    * @return
    */
  override def receive: Receive = {
    case Status =>
      sender ! elevatorStates

    case PickUp(ride) =>
      scheduleRide(ride)
      sender ! Unit

    case Step =>
      sendStepAndWaitForUpdates()
      sender ! Unit

    case msg => log.error(s"Cannot handle message $msg")
  }

  /**
    * Schedule a Elevator ride to one of the elevators in an optimized way.
    * The scheduling could also be implemented as in a round robin etc...
    *
    * @param ride
    */
  def scheduleRide(ride: ElevatorRide): Unit = {
    /**
      * Filter for elevators witch move towards the pickup location
      */
    val possibleElevators = elevatorStates filter {
      case (id, ElevatorState(currentFloor, _, Up, _, _))   => ride.from > currentFloor
      case (id, ElevatorState(currentFloor, _, Down, _, _)) => ride.from < currentFloor
      case (id, ElevatorState(_, _, NoDirection, _, _))     => true
    }

    /**
      * Choose the nearest elevator
      */
    val bestElevatorId = possibleElevators.minBy{ case (id, state) => Math.abs(ride.from - state.currentFloor) }._1

    /**
      * Send a pickup request to the best elevator
      */
    elevators(bestElevatorId) ! PickUp(ride)
  }


}
