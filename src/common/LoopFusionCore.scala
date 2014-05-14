package scala.virtualization.lms
package common


trait LoopFusionCore2 extends LoopFusionExtractors with BaseFatExp with LoopsFatExp with IfThenElseExp with BooleanOpsExp

// TODO doc
trait LoopFusionExtractors extends internal.Expressions { // copied from LoopFusionOpt: LoopFusionCore trait

  def unapplySimpleIndex(e: Def[Any]): Option[(Exp[Any], Exp[Int])] = None
  def unapplySimpleDomain(e: Def[Any]): Option[Exp[Any]] = None
  def unapplyFixedDomain(e: Def[Any]): Option[Exp[Int]] = e match {
    case EmptyColl() => Some(Const(0))
    case SingletonColl(_) => Some(Const(1))
    case _ => None
  }

  def unapplyEmptyColl(a: Def[Any]): Boolean = false
  def unapplySingletonColl(a: Def[Any]): Option[Exp[Any]] = None
  def unapplyMultiCollect[T](a: Def[T]): Option[Exp[T]] = None

  def unapplyForlike[T](e: Def[T]): Option[Exp[T]] = None
  
  object SimpleIndex {
    def unapply(a: Def[Any]): Option[(Exp[Any], Exp[Int])] = unapplySimpleIndex(a)
  }

  object SimpleDomain {
    def unapply(a: Def[Any]): Option[Exp[Any]] = unapplySimpleDomain(a)
  }

  object FixedDomain {
    def unapply(a: Def[Any]): Option[Exp[Int]] = unapplyFixedDomain(a)
  }

  object EmptyColl {
    def unapply(a: Def[Any]): Boolean = unapplyEmptyColl(a) 
  }

  object SingletonColl {
    def unapply(a: Def[Any]): Option[Exp[Any]] = unapplySingletonColl(a) 
  }

  object MultiCollect {
    def unapply[T](a: Def[T]): Option[Exp[T]] = unapplyMultiCollect(a)
  }

  object Forlike {
    def unapply[T](a: Def[T]): Option[Exp[T]] = unapplyForlike(a)
  }

  // object ResultBlock {
  //   def unapply(a: Def[Any]): Option[Exp[Any]] = a match {
  //     case SimpleCollect(res) => Some(res)
  //     case SimpleCollectIf(res, _) => Some(res)
  //     case MultiCollect(res, _) => Some(res)
  //     case Reduce(res, _, _) => Some(res)
  //     case _ => None
  //   }
  // }

  // object AllBlocks {
  //   def unapply(a: Def[Any]): Option[List[Exp[Any]]] = a match {
  //     case SimpleCollect(res) => Some(List(res))
  //     case SimpleCollectIf(res, conds) => Some(res :: conds)
  //     case MultiCollect(res, _) => Some(List(res))
  //     case Reduce(res, _, _) => Some(List(res))
  //     case _ => None
  //   }
  // }
}