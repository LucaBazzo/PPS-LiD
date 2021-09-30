package utils

import alice.tuprolog._
import controller.GameEvent._
import model.entity.State._

object Scala2P {

  def extractTerm(solveInfo:SolveInfo, i:Integer): Term =
    solveInfo.getSolution.asInstanceOf[Struct].getArg(i).getTerm

  def extractTerm(solveInfo:SolveInfo, s:String): Term =
    solveInfo.getTerm(s)

  implicit def stringToTerm(s: String): Term = Term.createTerm(s)
  implicit def seqToTerm[T](s: Seq[T]): Term = s.mkString("[",",","]")
  implicit def stringToTheory[T](s: String): Theory = new Theory(s)

  def mkPrologEngine(theory: Theory): Term => Iterable[SolveInfo] = {
    val engine = new Prolog
    engine.setTheory(theory)

    goal => new Iterable[SolveInfo]{

      override def iterator: Iterator[SolveInfo] = new Iterator[SolveInfo]{
        var solution: Option[SolveInfo] = Some(engine.solve(goal))

        override def hasNext: Boolean = solution.isDefined &&
                              (solution.get.isSuccess || solution.get.hasOpenAlternatives)

        override def next(): SolveInfo =
          try solution.get
          finally solution = if (solution.get.hasOpenAlternatives) Some(engine.solveNext()) else None
      }
    }
  }

  def solveWithSuccess(engine: Term => Iterable[SolveInfo], goal: Term): Boolean =
    engine(goal).map(_.isSuccess).headOption.contains(true)
}

object TryScala2P extends App {
  import Scala2P._

  val engine: Term => Iterable[SolveInfo] = mkPrologEngine("""
    checkUp(X):-(X \= state(falling)),(X \= state(somersault)),(X \= state(crouching)).
    checkDown(X):-(X = state(standing)), !.
    checkDown(X):-(X = state(running)).
    checkDownRelease(X):-(X = state(crouching)).
    checkLeftAndRight(_).
    checkSlide(X):-(X \= state(jumping)),(X \= state(falling)),(X \= state(somersault)).

    checkCommand(C, S) :-
    (C=command(up) -> call(checkUp(S)));
    (C=command(down) -> call(checkDown(S)));
    (C=command(downReleased) -> call(checkDownRelease(S)));
    (C=command(moveLeft) -> call(checkLeftAndRight(_)));
    (C=command(moveRight) -> call(checkLeftAndRight(_)));
    (C=command(slide) -> call(checkSlide(S))).
  """)

  val goal: String = "checkCommand(command(" + Down.toString.toLowerCase() + "), state(" + Standing.toString.toLowerCase() + "))"

}
