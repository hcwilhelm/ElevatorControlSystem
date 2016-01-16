package actors

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import model.Elevator._
import model.ElevatorControlSystem._
import org.specs2._

import scala.concurrent.duration._

class ElevatorActorTest extends TestKit(ActorSystem("ElevatorActorTest")) with mutable.SpecificationLike {

  /**
    * Implicit timeout for ask pattern
    */
  implicit val timeout = Timeout(5.seconds)

  def getElevator: TestActorRef[ElevatorActor] = TestActorRef(new ElevatorActor(ElevatorID(1)))

  "An elevators move function" should {
    "move up when direction is Up" in {
      val elevatorState = ElevatorState(0, 0, Up, Vector.empty, Vector.empty)
      val elevator = getElevator

      elevator.underlyingActor.move(elevatorState).currentFloor mustEqual 1
    }

    "move down when direction is Down" in {
      val elevatorState = ElevatorState(0, 0, Down, Vector.empty, Vector.empty)
      val elevator = getElevator

      elevator.underlyingActor.move(elevatorState).currentFloor mustEqual -1
    }

    "move not move when direction is NoDirection" in {
      val elevatorState = ElevatorState(0, 0, NoDirection, Vector.empty, Vector.empty)
      val elevator = getElevator

      elevator.underlyingActor.move(elevatorState).currentFloor mustEqual 0
    }
  }

  "An elevators initial state" should {
    "be (0, 0, NoDirection, empty, empty)" in {
      val expectedState = ElevatorState(0, 0, NoDirection, Vector.empty, Vector.empty)
      val elevator = getElevator

      elevator.underlyingActor.elevatorState mustEqual expectedState
    }
  }

  "An elevator" should {
    "queue PickUp requests" in {
      val elevator = getElevator
      val ride = ElevatorRide(0 , 1)

      elevator ! PickUp(ride)

      elevator.underlyingActor.elevatorState.pickUpQueue must contain(ride)
    }

    "move to the first pickUp floor" in {
      val elevator = getElevator
      val ride = ElevatorRide(1 , 2)

      elevator ! PickUp(ride)
      elevator ? Step

      elevator.underlyingActor.elevatorState.goalFloor mustEqual 1
    }

    "on pickup floor enqueue drop off floor" in {
      val elevator = getElevator
      val ride = ElevatorRide(1 , 2)

      elevator ! PickUp(ride)
      elevator ? Step
      elevator ? Step

      elevator.underlyingActor.elevatorState.dropOffQueue must contain(ride.to)
    }

    "drop off on goal floor" in {
      val elevator = getElevator
      val ride = ElevatorRide(1, 2)

      elevator ! PickUp(ride)
      elevator ? Step
      elevator ? Step
      elevator ? Step

      elevator.underlyingActor.elevatorState.currentFloor mustEqual ride.to
      elevator.underlyingActor.elevatorState.dropOffQueue must beEmpty
    }

    "pick up and drop off persons while moving towards to goal floors" in {
      val elevator = getElevator

      elevator ! PickUp(ElevatorRide(1, 4))
      elevator ! PickUp(ElevatorRide(2, 3))

      elevator.underlyingActor.elevatorState.pickUpQueue must haveSize(2)

      elevator ? Step
      elevator ? Step

      elevator.underlyingActor.elevatorState.pickUpQueue must haveSize(1)
      elevator.underlyingActor.elevatorState.dropOffQueue must contain(4)

      elevator ? Step

      elevator.underlyingActor.elevatorState.pickUpQueue must haveSize(0)
      elevator.underlyingActor.elevatorState.dropOffQueue must contain(4, 3)

      elevator ? Step
      elevator.underlyingActor.elevatorState.dropOffQueue must not contain(3)
    }
  }
}
