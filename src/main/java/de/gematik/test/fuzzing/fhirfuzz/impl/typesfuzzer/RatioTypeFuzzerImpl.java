/*
 * ${GEMATIK_COPYRIGHT_STATEMENT}
 */

package de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;

import java.util.LinkedList;
import java.util.List;

public class RatioTypeFuzzerImpl implements FhirTypeFuzz<Ratio> {
    private final FuzzerContext fuzzerContext;

    public RatioTypeFuzzerImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    public Ratio generateRandom() {
        return new Ratio()
                .setDenominator(new Quantity(2))
                .setNumerator(new Quantity(1));

    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public Ratio fuzz(Ratio type) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (val f : m) {
            f.accept(type);
        }
        return type;
    }

    private List<FuzzingMutator<Ratio>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Ratio>>();
        manipulators.add(this::numFuzz);
        manipulators.add(this::denomFuzz);
        manipulators.add(this::idFuzz);
        manipulators.add(this::extFuzz);
        manipulators.add(this::numCodeFuzz);
        manipulators.add(this::numUnitFuzz);
        manipulators.add(this::numSystemFuzz);
        manipulators.add(this::denomCodeFuzz);
        manipulators.add(this::denomSystemFuzz);
        manipulators.add(this::denomUnitFuzz);
        return manipulators;
    }

    private void numFuzz(Ratio r) {
        if (!r.hasNumerator()) {
            val numerator = new Quantity(fuzzerContext.getRandom().nextInt());
            r.setNumerator(numerator);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Numerator in Ratio", null, numerator));
        } else {
            if (fuzzerContext.conditionalChance()) {
                val num = r.getNumerator();
                r.setNumerator(null);
                fuzzerContext.addLog(new FuzzOperationResult<>("set Numerator in Ratio", num, null));
            } else {
                val num = r.getNumerator();
                val newNum = new Quantity(fuzzerContext.getRandom().nextInt());
                r.setNumerator(newNum);
                fuzzerContext.addLog(new FuzzOperationResult<>("set Numerator in Ratio", num, newNum));
            }
        }
    }

    private void denomFuzz(Ratio r) {
        if (!r.hasDenominator()) {
            val denom = new Quantity(fuzzerContext.getRandom().nextInt());
            r.setDenominator(denom);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Denominator in Ratio", null, denom));
        } else {
            if (fuzzerContext.conditionalChance()) {
                val denom = r.getDenominator();
                r.setDenominator(null);
                fuzzerContext.addLog(new FuzzOperationResult<>("set Denominator in Ratio", denom, null));
            } else {
                val denom = r.getDenominator();
                val newdenom = new Quantity(fuzzerContext.getRandom().nextInt());
                r.setDenominator(newdenom);
                fuzzerContext.addLog(new FuzzOperationResult<>("set Denominator in Ratio", denom, newdenom));
            }
        }
    }

    private void idFuzz(Ratio r) {
        if (!r.hasId()) {
            val entry = fuzzerContext.getIdFuzzer().generateRandom();
            r.setId(entry);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Id in Ratio", null, entry));
        } else {
            val id = r.getId();
            fuzzerContext.getIdFuzzer().fuzz(r::getId, r::setId);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzzed Id in Ratio", id, r.hasId() ? r.getId() : null));
        }
    }

    private void extFuzz(Ratio r) {
        val extensionFuzz = new ExtensionFuzzerImpl(fuzzerContext);
        if (!r.hasExtension()) {
            val ex = extensionFuzz.generateRandom();
            r.setExtension(List.of(ex));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Extension in Ratio", null, ex.getValue()));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzz);
            val org = r.getExtension();
            listFuzzer.fuzz(r::getExtension, r::setExtension);
        }
    }

    private void numCodeFuzz(Ratio r) {
        if (r.hasNumerator()) {
            String code = "";
            if (r.getNumerator().hasCode()) {
                code = r.getNumerator().getCode();
            }
            val newCode = fuzzerContext.conditionalChance() ? null : fuzzerContext.getStringFuzz().generateRandom(5);
            r.getNumerator().setCode(newCode);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Numerator -> Code in Ratio", code, newCode));
        }
    }

    private void numUnitFuzz(Ratio r) {
        if (r.hasNumerator()) {
            String unit = "";
            if (r.getNumerator().hasUnit()) {
                unit = r.getNumerator().getUnit();
            }
            val newUnit = fuzzerContext.conditionalChance() ? null : fuzzerContext.getStringFuzz().generateRandom(5);
            r.getNumerator().setUnit(newUnit);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Numerator -> Unit in Ratio", unit, newUnit));
        }
    }

    private void numSystemFuzz(Ratio r) {
        if (r.hasNumerator()) {
            String system = "";
            if (r.getNumerator().hasSystem()) {
                system = r.getNumerator().getSystem();
            }
            val newUnit = fuzzerContext.conditionalChance() ? null : fuzzerContext.getStringFuzz().generateRandom(5);
            r.getNumerator().setSystem(newUnit);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Numerator -> System in Ratio", system, newUnit));
        }
    }

    private void denomCodeFuzz(Ratio r) {
        if (r.hasDenominator()) {
            String code = "";
            if (r.getDenominator().hasCode()) {
                code = r.getDenominator().getCode();
            }
            val newCode = fuzzerContext.conditionalChance() ? null : fuzzerContext.getStringFuzz().generateRandom(5);
            r.getDenominator().setCode(newCode);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Denominator -> Code in Ratio", code, newCode));
        }
    }

    private void denomSystemFuzz(Ratio r) {
        if (r.hasDenominator()) {
            String system = "";
            if (r.getDenominator().hasSystem()) {
                system = r.getDenominator().getSystem();
            }
            val newSystem = fuzzerContext.conditionalChance() ? null : fuzzerContext.getStringFuzz().generateRandom(5);
            r.getDenominator().setSystem(newSystem);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Denominator -> System in Ratio", system, newSystem));
        }
    }

    private void denomUnitFuzz(Ratio r) {
        if (r.hasDenominator()) {
            String unit = "";
            if (r.getDenominator().hasUnit()) {
                unit = r.getDenominator().getUnit();
            }
            val newUnit = fuzzerContext.conditionalChance() ? null : fuzzerContext.getStringFuzz().generateRandom(5);
            r.getDenominator().setUnit(newUnit);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Denominator -> Unit in Ratio", unit, newUnit));
        }
    }

}
