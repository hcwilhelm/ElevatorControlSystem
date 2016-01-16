import actors.ElevatorControllSystemActor
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import model.Elevator.{ElevatorID, ElevatorRide, ElevatorState}
import model.ElevatorControlSystem.{PickUp, Status, Step}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * ElevatorControlSystem main method
  */
object Main extends App {
  val actorSystem = ActorSystem("ElevatorControlSystem")
  implicit val timeout = Timeout(5.seconds)

  /**
    * Elevator control system actor ref with numberOfElevators.
    */
  val numberOfElevators = 4
  val cs = actorSystem.actorOf(Props(new ElevatorControllSystemActor(numberOfElevators)), "ElevatorControllActor")

  /**
    * Sending commands to the control system with the ask pattern to
    * ensure a strict sequence. In a more async system one would choose
    * send but then we would need to have some Thread.sleep statements
    * to watch the ElevatorStates
    */
  for {
    _ <- cs ? PickUp(ElevatorRide(2, 5))
    _ <- cs ? Step
    _ <- cs ? Step
    _ <- (cs ? Status).mapTo[Map[ElevatorID, ElevatorState]] map (_ foreach println)
    _ <- Future(println)

    _ <- cs ? PickUp(ElevatorRide(4, 8))
    _ <- cs ? Step
    _ <- (cs ? Status).mapTo[Map[ElevatorID, ElevatorState]] map (_ foreach println)
    _ <- Future(println)

    _ <- cs ? PickUp(ElevatorRide(1, 3))
    _ <- cs ? Step
    _ <- (cs ? Status).mapTo[Map[ElevatorID, ElevatorState]] map (_ foreach println)
    _ <- Future(println)

    _ <- cs ? PickUp(ElevatorRide(4, 7))
    _ <- cs ? Step
    _ <- (cs ? Status).mapTo[Map[ElevatorID, ElevatorState]] map (_ foreach println)
    _ <- Future(println)

    _ <- cs ? PickUp(ElevatorRide(1, 3))
    _ <- cs ? Step
    _ <- (cs ? Status).mapTo[Map[ElevatorID, ElevatorState]] map (_ foreach println)
    _ <- Future(println)

    _ <- cs ? PickUp(ElevatorRide(6, 9))
    _ <- cs ? Step
    _ <- cs ? Step
    _ <- cs ? Step
    _ <- (cs ? Status).mapTo[Map[ElevatorID, ElevatorState]] map (_ foreach println)

    _ <- Future(println)
    _ <- actorSystem.terminate()
  } (println("Good by"))

}
