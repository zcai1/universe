/*package GUTI

import org.checkerframework.javacutil.AnnotationUtils
import org.checkerframework.framework.`type`.QualifierHierarchy

import checkers.inference.InferenceSolver
import checkers.inference.InferenceSolution
import checkers.inference.model._

import javax.lang.model.element.AnnotationMirror
import javax.annotation.processing.ProcessingEnvironment

import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import java.util.Collection
import java.util.Map

import scala.collection.JavaConversions._

abstract class PBSConstraintSolver extends InferenceSolver {

  var configuration: Map[String, String] = null
  var procEnv: ProcessingEnvironment = null

  /** All variables used in this program. */
  var variables: Collection[Slot] = null

  /** Empty, b/c there is no viewpoint adaptation (yet?). */
  // var combvariables: List[CombVariableSlot] = null

  /** All constraints that have to be fulfilled. */
  var constraints: Collection[Constraint] = null

  val NRBITS: Int

  var t_start: Long = 0
  var t_decl: Long = 0
  var t_encode: Long = 0
  var t_cnf: Long = 0
  var t_end: Long = 0

  var optWCNFFile: String = "constraints.wcnf"
  var optWCNFSolver: String = "sat4j-maxsat.sh"

  // TOOD: use InferenceMain logger
  val DEBUG = true
@Override
  def solve(configuration: Map[String, String],
    variables: Collection[Slot],
    constraints: Collection[Constraint],
    qualHierarchy: QualifierHierarchy,
    procEnv: ProcessingEnvironment): InferenceSolution = {

    val TIMING = (configuration.get("TIMING") != null)

    {
      val optFile = configuration.get("WCNFFile")
      if (optFile != null) {
        optWCNFFile = optFile
      }
    }
    {
      val optSolver = configuration.get("WCNFSolver")
      if (optSolver != null) {
        optWCNFSolver = optSolver
      }
    }

    if (TIMING) {
      t_start = System.currentTimeMillis()
    }

    this.variables = variables
    this.constraints = constraints

    System.out.println("PBSSolver variables: " + variables);
    System.out.println("PBSSolver constraints: " + constraints);

    this.procEnv = procEnv
    this.configuration = configuration

    var ands: AndOfOrs = new AndOfOrs()
    val bools = collection.mutable.ListBuffer[BVar]()

    for (v <- variables) {
      val (a, b) = decl(v)
      if (a != null) ands.and(a)
      if (b != null) bools.appendAll(b)
    }

    if (TIMING) {
      t_decl = System.currentTimeMillis()
    }

    for (cst <- constraints) {
      val (a, b) = encode(cst)
      if (a != null) ands.and(a)
      if (b != null) bools.appendAll(b)
    }

    if (TIMING) {
      t_encode = System.currentTimeMillis()
    }

    val cnf: StringBuilder = toWCNF(ands, bools.toList)
    // println("CNF: " + cnf)

    if (TIMING) {
      t_cnf = System.currentTimeMillis()
    }

    val solveroutput = solveCNF(cnf.toString)

    if (TIMING) {
      t_end = System.currentTimeMillis()
    }

    solveroutput match {
      case None => {
        // TODO handle errors
        null
      }
      case Some(ssolveroutput) => {
        val solution = decode(ssolveroutput)
        solution match {
          case Some(ssolution) => {
            // Might no need to fix at all?
            ssolution
          }
          case None => {
            null
          }
        }
      }
    }
  }

  def timing: String = {
    "Total time in PBSConstraintSolver: " + (t_end - t_start) +
      "\nGenerating declarations: " + (t_decl - t_start) +
      "\nGenerating constraints: " + (t_encode - t_decl) +
      "\nGenerating CNF: " + (t_cnf - t_encode) +
      "\nSAT solver: " + (t_end - t_cnf)
  }

  def version: String = "PBSConstraintSolver version 0.2"

  def decode(cnfout: List[String]): Option[scala.collection.Map[Integer, AnnotationMirror]] = {
    // return type previously was Option[Map[AbstractVariableSlot, AnnotationMirror]] = {
    val sum = cnfout.find(_.startsWith("s "))

    print("CNF solution summary: ")
    sum match {
      case Some("s UNSATISFIABLE") => {
        println("unsatisfiable!")
        return None
      }
      case Some("s UNKNOWN") => {
        println("unknown problem!")
        return None
      }
      case Some(s) => {
        // more cases?
        println("satisfied!")
      }
      case None => {
        println("none found! Quitting.")
        return None
      }
    }

    val rawsol = cnfout.find(_.startsWith("v "))

    val solline = rawsol match {
      case Some(s) => s.drop(2)
      case None => {
        println("The solver said satisifed, but I didn't find the solution!")
        println("Solver replied: " + (cnfout mkString "\n"))
        return None
      }
    }

    val solarr: Array[Int] = solline.split(' ').map(_.toInt).filterNot(_ == 0)
    val solconst = solarr.map(unbit(_))
    // TODO: do you want to ensure that every variable is true only once?
    val onlytrue = solconst.filter(_ match {
      case EqConst(v, c) => true
      case Not(x) => false
      case BVar(v) => {
        // ignore the BVars
        false
      }
    })

    // println("Solution: " + onlytrue.mkString("\n"))

    val varmap = new collection.mutable.HashMap[Integer, AnnotationMirror]
    for (eq <- onlytrue) {
      eq match {
        case EqConst(v, c) => varmap += (new Integer(v.getId) -> c.getValue)
        case _ => {
          // the filter above ensures that only EqConst are in onlytrue
        }
      }
    }
    Some(varmap.toMap)
  }
  // TODO: allow repeating runs, excluding the last result.
  // From the string v 1 2 3 ... we create the single line -1 -2 -3, the or-ing of the negations.
  // Should this be done at this low level or at the higher decoded level?

  def solveCNF(cnf: String): Option[List[String]] = {
    System.err.println("file: " + optWCNFFile)
    val cnfFile: PrintWriter = new PrintWriter(new FileWriter(new File(optWCNFFile), false))

    if (cnf.size < 200000) {
      cnfFile.print(cnf)
    } else {
      val chksize = 65536
      var chunk: String = null

      val blocks = cnf.size / chksize
      for (i <- 0 until blocks) {
        // println("Printing from " + i*chksize + " to " + ((i+1)*chksize-1) % cnf.size)
        chunk = cnf.substring(i * chksize, (i + 1) * chksize)
        cnfFile.print(chunk)
      }
      if (blocks * chksize < cnf.size) {
        chunk = cnf.substring(blocks * chksize, cnf.size)
        cnfFile.print(chunk)
      }
    }
    cnfFile.close()

    // TODO: add timing code to measure the time spent in the different parts of the program
    // To add a timeout, add something like this (time in seconds): -t 600
    val cmd: String = optWCNFSolver + " " + optWCNFFile

    val p = Runtime.getRuntime().exec(cmd)

    var inReply = List[String]()
    val inReader: Thread = new Thread(new Runnable() {
      def run() {
        val in: BufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()))
        var s: String = in.readLine()
        while (s != null) {
          // println("Read: " + s)
          inReply = inReply :+ s
          s = in.readLine()
        }

      }
    });
    inReader.start();

    var errReply = List[String]()
    val errReader: Thread = new Thread(new Runnable() {
      def run() {
        val err: BufferedReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))
        var s: String = err.readLine()
        while (s != null) {
          errReply = inReply :+ s
          s = err.readLine()
        }

      }
    });
    errReader.start();

    p.waitFor
    inReader.join()
    errReader.join()

    if (DEBUG) {
      println("CNF solver reply: " + inReply.mkString("\n"))
    }

    if (errReply.size > 0) {
      println("CNF solver errors:\n" + errReply.mkString("\n   "))
    }

    if (inReply != null && !inReply.isEmpty) {
      Some(inReply)
    } else {
      None
    }
  }

  def toWCNF(and: AndOfOrs, bools: List[BVar]): StringBuilder = {
    val res = new StringBuilder()

    val bits = variables.size * NRBITS + bools.size
    val clauses = and.ors.size

    // TODO: we want this for the statistics table. Is there a nicer place to put this?
    println("CNF sizes: bools & clauses: " + bits + " & " + clauses)

    // comments not supported by MUS solver
    res.append("c Generated WCNF File:\n")
    res.append("p wcnf " + bits + " " + clauses + " " + OrOfElems.maxWeight + "\n")

    for (o <- and.ors) {
      toWCNF(res, o)
    }

    res
  }

  def toWCNF(res: StringBuilder, or: OrOfElems) {
    res.append(or.weight + " ")
    for (e <- or.elems) {
      toCNF(res, e)
    }
    res.append(" 0\n")
  }

  def toCNF(res: StringBuilder, el: Elem) {
    // println("ToCNF: " + el)
    el match {
      case Not(nel) => {
        res.append("-")
        toCNF(res, nel)
      }
      case bv: BVar => {
        res.append(bvarBit(bv))
      }
      case EqConst(v, c) => {
        res.append(bit(v, c))
      }
    }
    res.append(" ")
  }

  def bit(v: /*Abstract*/VariableSlot, c: ConstantSlot): String
  def bvarBit(bv: BVar): String
  def unbit(in: Int): Elem

  def decl(v: /*AbstractVariable*/Slot): (AndOfOrs, List[BVar])

  def encodeSubtype(sub: Slot, sup: Slot): (AndOfOrs, List[BVar])
  def encodeCombine(target: Slot, decl: Slot, res: Slot): (AndOfOrs, List[BVar])
  def encodeEquality(ell: Slot, elr: Slot): (AndOfOrs, List[BVar])
  def encodeInequality(ell: Slot, elr: Slot): (AndOfOrs, List[BVar])
  def encodeComparable(ell: Slot, elr: Slot): (AndOfOrs, List[BVar])

  def encodePreference(elv: VariableSlot, elc: ConstantSlot, weight: Int): (AndOfOrs, List[BVar]) = {
    (AndOfOrs(OrOfElems(weight, EqConst(elv, elc))), null)
  }

  def encode(c: Constraint): (AndOfOrs, List[BVar]) = {
    c match {
      case st : SubtypeConstraint => {
        encodeSubtype(st.getSubtype, st.getSupertype)
      }
      case cc : CombineConstraint => {
        encodeCombine(cc.getTarget, cc.getDeclared, cc.getResult)
      }
      case eq : EqualityConstraint => {
        encodeEquality(eq.getFirst, eq.getSecond)
      }
      case iq : InequalityConstraint => {
        encodeInequality(iq.getFirst, iq.getSecond)
      }
      case compc : ComparableConstraint => {
        encodeComparable(compc.getFirst, compc.getSecond)
      }
      case prefc : PreferenceConstraint => {
        encodePreference(prefc.getVariable, prefc.getGoal, prefc.getWeight)
      }
      case _ => {
        System.err.println("PBSConstraintSolver: Unhandled case: " + c)
        (null, null)
      }
    }
  }

}*/
