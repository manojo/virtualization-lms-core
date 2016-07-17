package scala.lms
package epfl
package test4

import test2._
import test3._


trait ListMatch extends Extractors {

  implicit def listTyp[A: Typ: Nul]: Typ[List[A]]
  implicit def consTyp[T: Typ: Nul]: Typ[::[T]]

  object :!: {
    def apply[A: Typ: Nul](x: Rep[A], xs: Rep[List[A]]) = construct(classOf[::[A]], (::.apply[A] _).tupled, tuple(x, xs))
//    def unapply[A](x: Rep[::[A]]) = deconstruct2(classOf[::[A]], ::.unapply[A], x) // doesn't work: hd is private in :: !
    def unapply[A: Typ: Nul](x: Rep[List[A]]): Option[(Rep[A], Rep[List[A]])] =
      deconstruct2(classOf[::[A]].asInstanceOf[Class[List[A]]], (x: List[A]) => Some(x.head, x.tail), x)
  }

}


trait MatcherProg { this: Matching with ListMatch =>

  type Input = List[Char]

  implicit def charTyp: Typ[Char]
  implicit def boolTyp: Typ[Boolean]
  implicit def inputTyp: Typ[Input]

  def find(p: Input, s: Rep[Input]) = loop(p,s,p,s)

  def loop(p0: Input, s0: Rep[Input], pr: Input, sr: Rep[Input]): Rep[Boolean] = p0 match {
    case p::pp =>
      s0 switch {
        case (s: Rep[Char]):!:(ss: Rep[Input]) if s guard p => // matched p
          println("match")
          loop(pp,ss,pr,sr)
      } orElse {
        case s:!:ss => // no match for p
          println("no match")
          next(pr,sr)
      } orElse {
        case _ => unit(false)
      } end
    case _ => unit(true)
  }

  def next(p: Input, s: Rep[Input]): Rep[Boolean] = s switch {
    case s:!:(ss: Rep[Input]) => loop(p,ss,p,ss)
  } orElse {
    case _ => unit(false)
  } end

}


trait MatcherProgExp0 extends common.BaseExp with MatcherProg { this: Matching with ListMatch =>

  implicit def charTyp: Typ[Char] = ManifestTyp(implicitly)
  implicit def boolTyp: Typ[Boolean] = ManifestTyp(implicitly)
  implicit def inputTyp: Typ[Input] = ManifestTyp(implicitly)

  implicit def listTyp[T: Typ: Nul]: Typ[List[T]] = {
    implicit val ManifestTyp(m) = typ[T]
    ManifestTyp(implicitly)
  }
  implicit def consTyp[T: Typ: Nul]: Typ[::[T]] = {
    implicit val ManifestTyp(m) = typ[T]
    ManifestTyp(implicitly)
  }

}


class TestMatcher extends FileDiffSuite {

  val prefix = home + "test-out/epfl/test4-"

  def testMatcher1 = {
    withOutFile(prefix+"matcher1") {
      object MatcherProgExp extends MatcherProgExp0 with Matching with Extractors with ListMatch
        with MatchingExtractorsExpOpt
        with FunctionExpUnfoldRecursion with FunctionsExternalDef2
      import MatcherProgExp._

      val r = find("AAB".toList, fresh[Input])
      println(globalDefs.mkString("\n"))
      println(r)
      val p = new ExtractorsGraphViz with FunctionsGraphViz { val IR: MatcherProgExp.type = MatcherProgExp }
      p.emitDepGraph(r, prefix+"matcher1-dot")
    }
    assertFileEqualsCheck(prefix+"matcher1")
    assertFileEqualsCheck(prefix+"matcher1-dot")
  }

}
