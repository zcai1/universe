package GUTI;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import org.sat4j.core.VecInt;

import GUT.qual.Any;
import GUT.qual.Bottom;
import GUT.qual.Lost;
import GUT.qual.Peer;
import GUT.qual.Rep;
import GUT.qual.Self;
import GUT.qual.VPLost;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import maxsatbackend.MaxSatSerializer;
import util.MathUtils;
import util.VectorUtils;

public class GUTIMaxSatSerializer extends MaxSatSerializer {

    private AnnotationMirror ANY, PEER, REP, LOST, VPLOST, BOTTOM, SELF;
    private boolean allowLost;

    protected GUTIMaxSatSerializer(Elements elements) {
        super();
        ANY = AnnotationUtils.fromClass(elements, Any.class);
        PEER = AnnotationUtils.fromClass(elements, Peer.class);
        REP = AnnotationUtils.fromClass(elements, Rep.class);
        LOST = AnnotationUtils.fromClass(elements, Lost.class);
        VPLOST = AnnotationUtils.fromClass(elements, VPLost.class);
        BOTTOM = AnnotationUtils.fromClass(elements, Bottom.class);
        SELF = AnnotationUtils.fromClass(elements, Self.class);
    }

    @Override
    public VecInt[] serialize(CombineConstraint combineConstraint) {
        return new VariableCombos<EqualityConstraint>() {

            public VecInt[] accept(Slot target, Slot decl, Slot result,
                    CombineConstraint constraint) {

                final VecInt[] resultClauses;

                if (target instanceof ConstantSlot) {
                    if (decl instanceof ConstantSlot) {
                        resultClauses = constant_constant((ConstantSlot) target,
                                (ConstantSlot) decl, (VariableSlot) result,
                                constraint);
                    } else {
                        resultClauses = constant_variable((ConstantSlot) target,
                                (VariableSlot) decl, (VariableSlot) result,
                                constraint);
                    }
                } else if (decl instanceof ConstantSlot) {
                    resultClauses = variable_constant((VariableSlot) target,
                            (ConstantSlot) decl, (VariableSlot) result,
                            constraint);
                } else {
                    resultClauses = variable_variable((VariableSlot) target,
                            (VariableSlot) decl, (VariableSlot) result,
                            constraint);
                }
                return resultClauses;
            }

            protected VecInt[] constant_constant(ConstantSlot target,
                    ConstantSlot decl, VariableSlot result,
                    CombineConstraint combineConstraint) {
                return new VecInt[0];
            }

            protected VecInt[] constant_variable(ConstantSlot target,
                    VariableSlot decl, VariableSlot result,
                CombineConstraint combineConstraint) {
                List<VecInt> resultClauses = new ArrayList<VecInt>();
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), ANY),
                        MathUtils.mapIdToMatrixEntry(result.getId(), ANY)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), LOST),
                        MathUtils.mapIdToMatrixEntry(result.getId(), LOST)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), VPLOST)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), BOTTOM),
                        MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM)));

                if (areSameType(target.getValue(), PEER)){
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                } else if (areSameType(target.getValue(), REP)){
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                } else if (areSameType(target.getValue(), ANY)){
                    resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(),PEER),
                                MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                    resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(),REP),
                                MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                } else if (areSameType(target.getValue(), SELF)){
                    resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                    resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                        MathUtils.mapIdToMatrixEntry(result.getId(),REP)));
                } else if (areSameType(target.getValue(), VPLOST)){
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                } else if (areSameType(target.getValue(), LOST)){
                    if (!allowLost) {
                        ErrorReporter.errorAbort("Error: Receiver type contains LOST!");
                    }
                    else {
                        resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                                MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                        resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                    }
                } else if (areSameType(target.getValue(), BOTTOM)){
                    ErrorReporter.errorAbort("Error: Receiver type is BOTTOM!");
                } else {
                    ErrorReporter.errorAbort("Error: Unknown target type!");
                }

                if (!allowLost) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), LOST)));
                }

                return resultClauses.toArray(new VecInt[resultClauses.size()]);
                }


            protected VecInt[] variable_constant(VariableSlot target,
                    ConstantSlot decl, VariableSlot result,
                    CombineConstraint combineConstraint) {

                List<VecInt> resultClauses = new ArrayList<VecInt>();

                    resultClauses.add(VectorUtils.asVec(-MathUtils
                            .mapIdToMatrixEntry(target.getId(), BOTTOM)));
                if (areSameType(decl.getValue(), PEER)) {
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), REP),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), PEER),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), ANY),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), LOST),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), VPLOST),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                } else if (areSameType(decl.getValue(), REP)) {
                        resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                } else if (areSameType(decl.getValue(), ANY)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), ANY)));
                } else if (areSameType(decl.getValue(), BOTTOM)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM)));
                } else if (areSameType(decl.getValue(), LOST)) {
                    if (!allowLost) {
                        ErrorReporter.errorAbort(
                                "Error: Declared type contatins LOST!");
                    }
                    else {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), LOST)));
                    }
                } else if (areSameType(decl.getValue(), VPLOST)) {
                    ErrorReporter.errorAbort(
                            "Error: Declared type contatins VPLOST!");
                } else if (areSameType(decl.getValue(), SELF)) {
                    ErrorReporter.errorAbort("Error: Declared type contatins SELF!");
                } else {
                    ErrorReporter.errorAbort("Error: Unknown declared type!");
                }
                
                if (!allowLost) {
                    resultClauses.add(VectorUtils.asVec(-MathUtils
                            .mapIdToMatrixEntry(target.getId(), LOST)));
                }

                return resultClauses.toArray(new VecInt[resultClauses.size()]);
        }

            protected VecInt[] variable_variable(VariableSlot target,
                    VariableSlot decl, VariableSlot result,
                    CombineConstraint combineConstraint) {
                List<VecInt> resultClauses = new ArrayList<VecInt>();
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), ANY),
                        MathUtils.mapIdToMatrixEntry(result.getId(), ANY)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), LOST),
                        MathUtils.mapIdToMatrixEntry(result.getId(), LOST)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), SELF)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), VPLOST)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), BOTTOM),
                        MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), BOTTOM)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), REP),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), PEER),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), ANY),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), LOST),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), VPLOST),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                        MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                if (!allowLost) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), LOST)));
                    resultClauses.add(VectorUtils.asVec(-MathUtils
                            .mapIdToMatrixEntry(target.getId(), LOST)));
                }
                return resultClauses.toArray(new VecInt[resultClauses.size()]);
        }
    }.accept(combineConstraint.getTarget(), combineConstraint.getDeclared(), combineConstraint.getResult(), combineConstraint);
    }
}
