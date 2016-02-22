package GUTI

import checkers.inference._
import checkers.inference.model._

import GUT.GUTAnnotatedTypeFactory

import org.checkerframework.javacutil.AnnotationUtils

import scala.collection.mutable.HashMap

import com.sun.source.tree.Tree.Kind

object GUTConstants {
  // call getRealChecker to ensure that it is initialized
  val rc = InferenceMain.getInstance.getRealTypeFactory.asInstanceOf[GUTAnnotatedTypeFactory]
  val BOTTOM = new ConstantSlot(rc.BOTTOM)
  val SELF = new ConstantSlot(rc.SELF)
  val PEER = new ConstantSlot(rc.PEER)
  val REP = new ConstantSlot(rc.REP)
  val LOST = new ConstantSlot(rc.LOST)
  val ANY = new ConstantSlot(rc.ANY)
}

class GUTIConstraintSolver extends PBSConstraintSolver {
  // TODO: AbstractVariable was a supertype of VariableSlot
  type AbstractVariable = VariableSlot

  import GUTConstants._

  val NRBITS = 4
  val CONSTANTS = List(ANY, PEER, REP, LOST)

  // Map from bits to the corresponding variable and constant.
  // There is no mapping for FormulaVariables.
  val bitmap = new HashMap[Int, (AbstractVariable, ConstantSlot)]

  def bit(v: AbstractVariable, c: ConstantSlot): String = {
    // add 1, because zero is special in CNF
    val base = v.getId * NRBITS + 1

    val bitid =
      if (AnnotationUtils.areSame(c.getValue, PEER.getValue)) {
        base + 0
      } else if (AnnotationUtils.areSame(c.getValue, REP.getValue)) {
        base + 1
      } else if (AnnotationUtils.areSame(c.getValue, ANY.getValue)) {
        base + 2
      } else if (AnnotationUtils.areSame(c.getValue, LOST.getValue)) {
        base + 3
      } else {
        System.err.println("TODO: NO match for: " + c)
        99
      }

    bitmap += bitid -> (v, c)

    bitid.toString
  }

  private def bvarBase = variables.size * NRBITS + 1

  def bvarBit(bv: BVar): String = {
    (bvarBase + bv.id).toString
  }

  def unbit(in: Int): Elem = {
    if (in < 0) {
      Not(unbit(in.abs))
    } else {
      val (v, c) = bitmap(in)

      if (c != null) {
        EqConst(v, c)
      } else {
        BVar(in - bvarBase)
      }
    }
  }

  def decl(v: Slot): (AndOfOrs, List[BVar]) = {
    // TODO: clean up types, remove this overload
    decl(v.asInstanceOf[AbstractVariable])
  }

  def decl(v: AbstractVariable): (AndOfOrs, List[BVar]) = {
    if (v.isInstanceOf[CombVariableSlot]) {
      // lost is allowed for these internal variables
      val o1: OrOfElems = EqConst(v, ANY).or(EqConst(v, PEER)).or(EqConst(v, REP)).or(EqConst(v, LOST))
      val o2 = Not(EqConst(v, PEER)).or(Not(EqConst(v, REP)))
      val o3 = Not(EqConst(v, PEER)).or(Not(EqConst(v, ANY)))
      val o4 = Not(EqConst(v, ANY)).or(Not(EqConst(v, REP)))
      val o5 = Not(EqConst(v, LOST)).or(Not(EqConst(v, REP)))
      val o6 = Not(EqConst(v, LOST)).or(Not(EqConst(v, ANY)))
      val o7 = Not(EqConst(v, LOST)).or(Not(EqConst(v, PEER)))

      (AndOfOrs(o1, o2, o3, o4, o5, o6, o7), null)

    } else {
      // variables that will be inserted into the program cannot be lost
      val o1: OrOfElems = EqConst(v, ANY).or(EqConst(v, PEER)).or(EqConst(v, REP))
      val o2 = Not(EqConst(v, PEER)).or(Not(EqConst(v, REP)))
      val o3 = Not(EqConst(v, PEER)).or(Not(EqConst(v, ANY)))
      val o4 = Not(EqConst(v, ANY)).or(Not(EqConst(v, REP)))
      val o5 = OrOfElems(Not(EqConst(v, LOST)))
      (AndOfOrs(o1, o2, o3, o4, o5), null)
    }
  }

  def encodeSubtype(sub: Slot, sup: Slot): (AndOfOrs, List[BVar]) = {
    sub match {
      case vsub: AbstractVariable => {
        sup match {
          case vsup: AbstractVariable => {
            val o1 = EqConst(vsub, ANY).implies(EqConst(vsup, ANY))
            val o2 = EqConst(vsup, PEER).implies(EqConst(vsub, PEER))
            val o3 = EqConst(vsup, REP).implies(EqConst(vsub, REP))
            // vsub=LOST => (vsup=ANY \/ vsup=LOST)
            val o4 = Not(EqConst(vsub, LOST)).or(EqConst(vsup, ANY)).or(EqConst(vsup, LOST))
            val and = AndOfOrs(o1, o2, o3, o4)
            (and, null)
          }
          case BOTTOM => {
            println("GUTIConstraintSolver::encodeSubtype between " + vsub + " and " + sup)
            (AndOfOrs(EqConst(vsub, PEER)), null)
          }
          case SELF => {
            println("GUTIConstraintSolver::encodeSubtype between " + vsub + " and " + sup)
            (AndOfOrs(EqConst(vsub, PEER)), null)
          }
          case PEER => {
            (AndOfOrs(EqConst(vsub, PEER)), null)
          }
          case REP => {
            (AndOfOrs(EqConst(vsub, REP)), null)
          }
          case LOST => {
            // if the supertype is LOST, everything but ANY is allowed
            (AndOfOrs(Not(EqConst(vsub, ANY))), null)
          }
          case ANY => {
            // if the supertype is ANY, we don't generate a constraint
            // (AndOfOrs(EqConst(vsub, PEER).or(EqConst(vsub, REP)).or(EqConst(vsub, LOST)).or(EqConst(vsub, ANY))), null)
            (null, null)
          }
        }
      }
      case csub: ConstantSlot => {
        sup match {
          case vsup: AbstractVariable => {
            csub match {
              case BOTTOM => {
                // subtype of everthing
                (null, null)
              }
              case SELF => {
                (AndOfOrs(EqConst(vsup, PEER).or(EqConst(vsup, ANY))), null)
              }
              case PEER => {
                // ignore LOST as supertype?
                (AndOfOrs(EqConst(vsup, PEER).or(EqConst(vsup, ANY))), null)
              }
              case REP => {
                // ignore LOST as supertype?
                (AndOfOrs(EqConst(vsup, REP).or(EqConst(vsup, ANY))), null)
              }
              case LOST => {
                // ignore LOST as supertype?
                (AndOfOrs(EqConst(vsup, LOST).or(EqConst(vsup, ANY))), null)
              }
              case ANY => {
                // ignore LOST as supertype?
                (AndOfOrs(EqConst(vsup, ANY)), null)
              }
            }
          }
          case csup: ConstantSlot => {
            if (csub == csup || csup == ANY || ((csub == PEER || csub == REP || csub == SELF) && csup == LOST) ||
                (csub == SELF && (csup == PEER || csup == ANY)) ||
                csub == BOTTOM) {
              // nothing todo, valid subtypes
            } else {
              println("GUTConstraintSolver::encodeSubtype: Something is wrong! Subtype constraint between two constants: " + sub + " and " + sup)
            }
            (null, null)
          }
        }
      }
    }
  }
  //maybe the only one that needs to be considered.
  def encodeCombine(target: Slot, decl: Slot, res: Slot): (AndOfOrs, List[BVar]) = {
    if (!res.isInstanceOf[CombVariableSlot]) {
      println("encodeCombine should only be called with a CombVariable as third argument!")
      return (null, null)
    }
    val vres = res.asInstanceOf[AbstractVariable]

    target match {
      case vtarget: AbstractVariable => {
        decl match {
          case vdecl: AbstractVariable => {
            // (A /\ B) => C
            // !(A /\ B) \/ C
            // !A \/ !B \/ C
            val o1 = Not(EqConst(vtarget, PEER)).or(Not(EqConst(vdecl, PEER))).or(EqConst(vres, PEER))
            val o2 = Not(EqConst(vtarget, REP)).or(Not(EqConst(vdecl, PEER))).or(EqConst(vres, REP))
            val o3 = Not(EqConst(vdecl, ANY)).or(EqConst(vres, ANY))
            val o4 = Not(EqConst(vdecl, LOST)).or(EqConst(vres, LOST))
            // (A /\ !B) => C
            val o5 = Not(EqConst(vtarget, ANY)).or(EqConst(vdecl, ANY)).or(EqConst(vres, LOST))
            val o6 = Not(EqConst(vtarget, LOST)).or(EqConst(vdecl, ANY)).or(EqConst(vres, LOST))
            // Access on REP only allowed on LiteralThis
            val o7 = Not(EqConst(vdecl, REP)).or(EqConst(vres, LOST))
            val and = AndOfOrs(o1, o2, o3, o4, o5, o6, o7)
            (and, null)
          }
          case cdecl: ConstantSlot => {
            cdecl match {
              case SELF => {
                // Self is used as main modifier of extends and implements clauses
                // Treat SELF and PEER the same
                val o1 = Not(EqConst(vtarget, PEER)).or(EqConst(vres, PEER))
                val o2 = Not(EqConst(vtarget, REP)).or(EqConst(vres, REP))
                val o3 = Not(EqConst(vtarget, ANY)).or(EqConst(vres, LOST))
                val o4 = Not(EqConst(vtarget, LOST)).or(EqConst(vres, LOST))
                val and = AndOfOrs(o1, o2, o3, o4)
                (and, null)
              }
              case PEER => {
                val o1 = Not(EqConst(vtarget, PEER)).or(EqConst(vres, PEER))
                val o2 = Not(EqConst(vtarget, REP)).or(EqConst(vres, REP))
                val o3 = Not(EqConst(vtarget, ANY)).or(EqConst(vres, LOST))
                val o4 = Not(EqConst(vtarget, LOST)).or(EqConst(vres, LOST))
                val and = AndOfOrs(o1, o2, o3, o4)
                (and, null)
              }
              case REP => {
                // note that access through "this" is not covered in
                // this branch, as "this" is a literal
                (AndOfOrs(EqConst(vres, LOST)), null)
              }
              case ANY => {
                (AndOfOrs(EqConst(vres, ANY)), null)
              }
              case LOST => {
                (AndOfOrs(EqConst(vres, LOST)), null)
              }
              case _ => {
                println("GUTConstraintSolver::encodeCombine: Something is wrong! Combine constraint between: " + target + " and " + decl)
                (null, null)
              }
            }
          }
        }
      }
      case ctarget: ConstantSlot => {
        decl match {
          case vdecl: AbstractVariable => {
            ctarget match {
              case SELF => {
                (equalVars(vdecl, vres), null)
              }
              case PEER => {
                val o1 = Not(EqConst(vdecl, PEER)).or(EqConst(vres, PEER))
                val o2 = Not(EqConst(vdecl, REP)).or(EqConst(vres, LOST))
                val o3 = Not(EqConst(vdecl, ANY)).or(EqConst(vres, ANY))
                val o4 = Not(EqConst(vdecl, LOST)).or(EqConst(vres, LOST))
                val and = AndOfOrs(o1, o2, o3, o4)
                (and, null)
              }
              case REP => {
                val o1 = Not(EqConst(vdecl, PEER)).or(EqConst(vres, REP))
                val o2 = Not(EqConst(vdecl, REP)).or(EqConst(vres, LOST))
                val o3 = Not(EqConst(vdecl, ANY)).or(EqConst(vres, ANY))
                val o4 = Not(EqConst(vdecl, LOST)).or(EqConst(vres, LOST))
                val and = AndOfOrs(o1, o2, o3, o4)
                (and, null)
              }
              case ANY => {
                val o1 = Not(EqConst(vdecl, PEER)).or(EqConst(vres, LOST))
                val o2 = Not(EqConst(vdecl, REP)).or(EqConst(vres, LOST))
                val o3 = Not(EqConst(vdecl, ANY)).or(EqConst(vres, ANY))
                val o4 = Not(EqConst(vdecl, LOST)).or(EqConst(vres, LOST))
                val and = AndOfOrs(o1, o2, o3, o4)
                (and, null)
              }
              case LOST => {
                val o1 = Not(EqConst(vdecl, PEER)).or(EqConst(vres, LOST))
                val o2 = Not(EqConst(vdecl, REP)).or(EqConst(vres, LOST))
                val o3 = Not(EqConst(vdecl, ANY)).or(EqConst(vres, ANY))
                val o4 = Not(EqConst(vdecl, LOST)).or(EqConst(vres, LOST))
                val and = AndOfOrs(o1, o2, o3, o4)
                (and, null)
              }
              case _ => {
                println("GUTConstraintSolver::encodeCombine: Something is wrong! Combine constraint between: " + target + " and " + decl)
                (null, null)
              }
            }
          }
          case cdecl: ConstantSlot => {
            if (ctarget == SELF) {
              (AndOfOrs(EqConst(vres, cdecl)), null)
            } else if (ctarget == PEER && cdecl == PEER) {
              (AndOfOrs(EqConst(vres, PEER)), null)
            } else if (ctarget == REP && cdecl == PEER) {
              (AndOfOrs(EqConst(vres, REP)), null)
            } else if (cdecl == ANY) {
              (AndOfOrs(EqConst(vres, ANY)), null)
            } else {
              (AndOfOrs(EqConst(vres, LOST)), null)
            }
          }
        }
      }
    }
  }

  private def equalVars(v1: AbstractVariable, v2: AbstractVariable): AndOfOrs = {
    AndOfOrs(
      (for (c <- CONSTANTS) yield {
        // v1=c => v2=c
        (Not(EqConst(v1, c)).or(EqConst(v2, c)))
      }): _*)
  }

  def encodeEquality(ell: Slot, elr: Slot): (AndOfOrs, List[BVar]) = {
    ell match {
      case vell: AbstractVariable => {
        elr match {
          case velr: AbstractVariable => {
            val o1 = EqConst(vell, ANY).implies(EqConst(velr, ANY))
            val o2 = EqConst(vell, PEER).implies(EqConst(velr, PEER))
            val o3 = EqConst(vell, REP).implies(EqConst(velr, REP))
            val o4 = EqConst(vell, LOST).implies(EqConst(velr, LOST))
            val and = AndOfOrs(o1, o2, o3, o4)
            (and, null)
          }
          case celr: ConstantSlot => {
            (AndOfOrs(EqConst(vell, celr)), null)
          }
        }
      }
      case cell: ConstantSlot => {
        elr match {
          case velr: AbstractVariable => {
            (AndOfOrs(EqConst(velr, cell)), null)
          }
          case celr: ConstantSlot => {
            if (cell != celr) {
              println("Something is wrong! Equality constraint between two constants: " + ell + " and " + elr)
            } else {
              // The two constants are equal, so the constraint is satisfied
            }
            (null, null)
          }
        }
      }
    }
  }

  def encodeInequality(ell: Slot, elr: Slot): (AndOfOrs, List[BVar]) = {
    ell match {
      case vell: AbstractVariable => {
        elr match {
          case velr: AbstractVariable => {
            val o1 = EqConst(vell, ANY).implies(Not(EqConst(velr, ANY)))
            val o2 = EqConst(vell, PEER).implies(Not(EqConst(velr, PEER)))
            val o3 = EqConst(vell, REP).implies(Not(EqConst(velr, REP)))
            val o4 = EqConst(vell, LOST).implies(Not(EqConst(velr, LOST)))
            val and = AndOfOrs(o1, o2, o3, o4)
            (and, null)
          }
          case celr: ConstantSlot => {
            (AndOfOrs(Not(EqConst(vell, celr))), null)
          }
        }
      }
      case cell: ConstantSlot => {
        elr match {
          case velr: AbstractVariable => {
            (AndOfOrs(Not(EqConst(velr, cell))), null)
          }
          case celr: ConstantSlot => {
            if (cell == celr) {
              println("Something is wrong! Inequality constraint between two constants: " + ell + " and " + elr)
            } else {
              // The two constants are not equal, so the constraint is satisfied
            }
            (null, null)
          }
        }
      }
    }
  }

  def encodeComparable(ell: Slot, elr: Slot): (AndOfOrs, List[BVar]) = {
    ell match {
      case vell: AbstractVariable => {
        elr match {
          case velr: AbstractVariable => {
            (AndOfOrs(EqConst(vell, PEER).implies(Not(EqConst(velr, REP))),
              EqConst(vell, REP).implies(Not(EqConst(velr, PEER)))), null)
          }
          case PEER => {
            (AndOfOrs(EqConst(vell, PEER).or(EqConst(vell, ANY))), null)
          }
          case REP => {
            (AndOfOrs(EqConst(vell, REP).or(EqConst(vell, ANY))), null)
          }
          case LOST => {
            // if one of the sides is LOST or ANY, we don't need a constraint
            (null, null)
          }
          case ANY => {
            (null, null)
          }
        }
      }
      case cell: ConstantSlot => {
        elr match {
          case velr: AbstractVariable => {
            cell match {
              case PEER => {
                (AndOfOrs(EqConst(velr, PEER).or(EqConst(velr, ANY))), null)
              }
              case REP => {
                (AndOfOrs(EqConst(velr, REP).or(EqConst(velr, ANY))), null)
              }
              case LOST => {
                // if one of the sides is LOST or ANY, we don't need a constraint
                (null, null)
              }
              case ANY => {
                (null, null)
              }
            }
          }
          case celr: ConstantSlot => {
            println("Something is wrong! Comparable constraint between constant and constant: " + ell + " and " + elr)
            (null, null)
          }
        }
      }
    }

    /* This seems too complicated:
       TODO: can we get rid of the BVar everywhere?
    val (oneAnds, oneBs) = encodeSubtype(ell, elr)
    val (twoAnds, twoBs) = encodeSubtype(elr, ell)
    val (resAnds, resB) = oneAnds or twoAnds
    (resAnds, List(resB))
    */
  }

}
