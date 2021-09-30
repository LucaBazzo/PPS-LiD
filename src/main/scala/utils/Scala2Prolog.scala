package utils

import alice.tuprolog._
import controller.GameEvent._
import model.entity.State._

object Scala2Prolog {

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