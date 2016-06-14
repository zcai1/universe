package GUTI;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import GUT.GUTAnnotatedTypeFactory;
import checkers.inference.InferenceMain;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import constraintsolver.VariableCombos;
import maxsatbackend.MaxSatSerializer;
import util.MathUtils;
import util.VectorUtils;

public class GUTIMaxSatSerializer extends MaxSatSerializer {

    private AnnotationMirror ANY, PEER, REP, LOST, VPLOST, BOTTOM, SELF;
    private boolean allowLost;

    public GUTIMaxSatSerializer() {
        super();
        GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory) InferenceMain
                .getInstance().getRealTypeFactory();
        ANY = gutATF.ANY;
        PEER = gutATF.PEER;
        REP = gutATF.REP;
        LOST = gutATF.LOST;
        VPLOST = gutATF.VPLOST;
        BOTTOM = gutATF.BOTTOM;
        SELF = gutATF.SELF;
    }

    @Override
    public VecInt[] serialize(CombineConstraint combineConstraint) {
        return new VariableCombos<CombineConstraint, VecInt[]>(new VecInt[0]) {

            public VecInt[] accept(Slot target, Slot decl, Slot result,
                    CombineConstraint constraint) {

                final VecInt[] resultClauses;
                if (target instanceof CombVariableSlot) {
                    Slot realTypeWithoutClassBound = ((CombVariableSlot)target).getSecond();
                        target = realTypeWithoutClassBound;
                }
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
                List<VecInt> resultClauses = new ArrayList<VecInt>();
                if (AnnotationUtils.areSame(target.getValue(), BOTTOM)) {
                    ErrorReporter.errorAbort("Error: Receiver type is BOTTOM!");
                }
                if (!allowLost) {
                    if (AnnotationUtils.areSame(target.getValue(), LOST)) {
                        ErrorReporter.errorAbort("Error: Receiver type contains LOST!");
                    }
                }
                if (AnnotationUtils.areSame(decl.getValue(), PEER)) {
                    if (AnnotationUtils.areSame(target.getValue(), REP)) {
                        resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                    }
                    if (AnnotationUtils.areSame(target.getValue(), SELF)) {
                        resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                    }
                    if (AnnotationUtils.areSame(target.getValue(), PEER)) {
                        resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                    }
                    if (AnnotationUtils.areSame(target.getValue(), ANY)) {
                        resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                    }
                    if (AnnotationUtils.areSame(target.getValue(), LOST)) {
                        resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                    }
                    if (AnnotationUtils.areSame(target.getValue(), VPLOST)) {
                        resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                    }
                } else if (AnnotationUtils.areSame(decl.getValue(), REP)) {
                    if (AnnotationUtils.areSame(target.getValue(), SELF)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                    }
                    else {
                    resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                    }
                } else if (AnnotationUtils.areSame(decl.getValue(), ANY)) {
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), ANY)));
                } else if (AnnotationUtils.areSame(decl.getValue(), BOTTOM)) {
                resultClauses.add(VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM)));
                } else if (AnnotationUtils.areSame(decl.getValue(), LOST)) {
                if (!allowLost) {
                    ErrorReporter.errorAbort(
                            "Error: Declared type contatins LOST!");
                }
                else {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));//change lost to vplost
                }
                } else if (AnnotationUtils.areSame(decl.getValue(), VPLOST)) {
                ErrorReporter.errorAbort(
                        "Error: Declared type contatins VPLOST!");
                } else if (AnnotationUtils.areSame(decl.getValue(), SELF)) {
                ErrorReporter.errorAbort("Error: Declared type contatins SELF!");
            } else {
                ErrorReporter.errorAbort("Error: Unknown declared type!");
            }

                return resultClauses.toArray(new VecInt[resultClauses.size()]);
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

                if (AnnotationUtils.areSame(target.getValue(), PEER)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                            MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                } else if (AnnotationUtils.areSame(target.getValue(), REP)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                } else if (AnnotationUtils.areSame(target.getValue(), ANY)) {
                    resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(),PEER),
                                MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                    resultClauses.add(VectorUtils.asVec(
                                -MathUtils.mapIdToMatrixEntry(decl.getId(),REP),
                                MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                } else if (AnnotationUtils.areSame(target.getValue(), SELF)) {
                    resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                        MathUtils.mapIdToMatrixEntry(result.getId(), PEER)));
                    resultClauses.add(VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                        MathUtils.mapIdToMatrixEntry(result.getId(),REP)));
                } else if (AnnotationUtils.areSame(target.getValue(), VPLOST)) {
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), PEER),
                            MathUtils.mapIdToMatrixEntry(result.getId(),VPLOST)));
                    resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(decl.getId(), REP),
                        MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                } else if (AnnotationUtils.areSame(target.getValue(), LOST)) {
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
                } else if (AnnotationUtils.areSame(target.getValue(), BOTTOM)) {
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
                if (AnnotationUtils.areSame(decl.getValue(), PEER)) {
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
                } else if (AnnotationUtils.areSame(decl.getValue(), REP)) {
                        resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                            MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));
                        resultClauses.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(target.getId(), SELF),
                            MathUtils.mapIdToMatrixEntry(result.getId(), REP)));
                } else if (AnnotationUtils.areSame(decl.getValue(), ANY)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), ANY)));
                } else if (AnnotationUtils.areSame(decl.getValue(), BOTTOM)) {
                    resultClauses.add(VectorUtils.asVec(
                            MathUtils.mapIdToMatrixEntry(result.getId(), BOTTOM)));
                } else if (AnnotationUtils.areSame(decl.getValue(), LOST)) {
                    if (!allowLost) {
                        ErrorReporter.errorAbort(
                                "Error: Declared type contatins LOST!");
                    }
                    else {
                        resultClauses.add(VectorUtils.asVec(
                                MathUtils.mapIdToMatrixEntry(result.getId(), VPLOST)));//change lost to vplost
                    }
                } else if (AnnotationUtils.areSame(decl.getValue(), VPLOST)) {
                    ErrorReporter.errorAbort(
                            "Error: Declared type contatins VPLOST!");
                } else if (AnnotationUtils.areSame(decl.getValue(), SELF)) {
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
