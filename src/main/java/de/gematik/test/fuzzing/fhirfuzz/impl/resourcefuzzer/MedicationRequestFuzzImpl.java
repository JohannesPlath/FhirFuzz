/*
 * ${GEMATIK_COPYRIGHT_STATEMENT}
 */

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirResourceFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AnnotationTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.DosageFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ReferenceFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.SimpleQuantityImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MedicationRequestFuzzImpl implements FhirResourceFuzz<MedicationRequest> {
    private final FuzzerContext fuzzerContext;


    public MedicationRequestFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    public MedicationRequest generateRandom() {
        val medReq = new MedicationRequest();
        medReq.setIdentifier(List.of(new IdentifierFuzzerImpl(fuzzerContext).generateRandom()));
        medReq.setId(fuzzerContext.getIdFuzzer().generateRandom());
        medReq.setMeta(new MetaFuzzerImpl(fuzzerContext).generateRandom());
        medReq.setLanguage(fuzzerContext.getStringFuzz().generateRandom(10));

        return medReq;
    }

    @Override
    public MedicationRequest fuzz(MedicationRequest mr) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<MedicationRequest> f : m) {
            f.accept(mr);
        }
        return mr;
    }

    private List<FuzzingMutator<MedicationRequest>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<MedicationRequest>>();
        if (getMapContent("KBV").toLowerCase().matches("true")) {
            manipulators.add(this::idFuzz);
            manipulators.add(this::metaFuzz);
            manipulators.add(this::extensionFuzz);
            manipulators.add(this::statusFuzz);
            manipulators.add(this::intendFuzz);
            manipulators.add(this::medicationFuzz);
            manipulators.add(this::subjectFuzz);
            manipulators.add(this::authoredOnFuzz);
            manipulators.add(this::requesterFuzz);
            manipulators.add(this::insuranceFuzz);
            manipulators.add(this::noteFuzz);
            manipulators.add(this::dispRequFuzz);
            manipulators.add(this::dosageFuzz);
        } else {
            manipulators.add(this::idFuzz);
            manipulators.add(this::identFuzz);
            manipulators.add(this::langFuzz);
            manipulators.add(this::metaFuzz);
            manipulators.add(this::extensionFuzz);
            manipulators.add(this::statusFuzz);
            manipulators.add(this::intendFuzz);
            manipulators.add(this::authoredOnFuzz);
            manipulators.add(this::subjectFuzz);
            manipulators.add(this::requesterFuzz);
            manipulators.add(this::insuranceFuzz);
            manipulators.add(this::noteFuzz);
            manipulators.add(this::medicationFuzz);
            manipulators.add(this::dispRequFuzz);
            manipulators.add(this::dosageFuzz);
        }
        return manipulators;
    }

    private void dosageFuzz(MedicationRequest mr) {
        val dosageFuzz = new DosageFuzzImpl(fuzzerContext);
        if (!mr.hasDosageInstruction()) {
            mr.setDosageInstruction(List.of(dosageFuzz.generateRandom()));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Dosage in MedicationRequest:", null, mr.hasDosageInstruction() ? mr.getDosageInstruction() : null));
        } else {
            val dosage = mr.hasDosageInstruction() ? mr.getDosageInstruction() : null;
            val listfuzzer = new ListFuzzerImpl<>(fuzzerContext, dosageFuzz);
            listfuzzer.fuzz(mr::hasDosageInstruction, mr::getDosageInstruction, mr::setDosageInstruction);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Dosage in MedicationRequest:", dosage, mr.hasDosageInstruction() ? mr.getDosageInstruction() : null));
        }
    }

    private void dispRequFuzz(MedicationRequest mr) {
        val mRDRC = mr.hasDispenseRequest() ? mr.getDispenseRequest() : null;
        if (mRDRC != null) {
            val dR = mRDRC.hasQuantity() ? mRDRC.getQuantity() : null;
            val simpleQuantetyFuzz = new SimpleQuantityImpl(fuzzerContext);
            simpleQuantetyFuzz.fuzz(mRDRC::hasQuantity, mRDRC::getQuantity, mRDRC::setQuantity);
            fuzzerContext.addLog(new FuzzOperationResult<>("set DispenseRequest in MedicationRequest:", dR, mr.getDispenseRequest().hasQuantity() ? mr.getDispenseRequest().getQuantity() : null));
        } else {
            val dispReq = new MedicationRequest.MedicationRequestDispenseRequestComponent().setQuantity(new SimpleQuantityImpl(fuzzerContext).generateRandom());
            mr.setDispenseRequest(dispReq);
            fuzzerContext.addLog(new FuzzOperationResult<>("set DispenseRequest in MedicationRequest:", dispReq, mr.getDispenseRequest().hasQuantity() ? mr.getDispenseRequest().getQuantity() : null));
        }
    }


    private void authoredOnFuzz(MedicationRequest mr) {
        if (!mr.hasAuthoredOn()) {
            val date = fuzzerContext.getRandomDate();
            mr.setAuthoredOn(date);
            fuzzerContext.addLog(new FuzzOperationResult<>("set AuthoredOn in MedicationRequest:", null, date));
        } else if (fuzzerContext.conditionalChance()) {
            val date = mr.getAuthoredOn();
            mr.setAuthoredOn(null);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Date in MedicationRequest:", date, mr.getAuthoredOn()));
        } else {
            val date = mr.getAuthoredOn();
            Date newDate;
            do {
                newDate = fuzzerContext.getRandomDate();
            } while (newDate == date);

            mr.setAuthoredOn(newDate);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Date in MedicationRequest:", date, newDate));
        }
    }

    private void medicationFuzz(MedicationRequest mr) {
        val ao = mr.getAuthoredOn();
        val referenceFuzzer = new ReferenceFuzzerImpl(fuzzerContext);
        val org = mr.hasMedication() ? mr.getMedication() : null;
        referenceFuzzer.fuzz(mr::hasMedication, mr::getRequester, mr::setMedication);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Medication in MedicationRequest:", org, mr.hasMedication() ? mr.getMedication() : null));
    }

    private void idFuzz(MedicationRequest mr) {
        val med = mr.getMedication();
        val orgId = mr.hasId() ? mr.getId() : null;
        fuzzerContext.getIdFuzzer().fuzz(mr::hasId, mr::getId, mr::setId);
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz ID in MedicationRequest:", orgId, mr.hasId() ? mr.getId() : null));
    }

    private void noteFuzz(MedicationRequest mr) {
        val notes = mr.hasNote() ? mr.getNote() : null;
        val annotFuzz = new AnnotationTypeFuzzImpl(fuzzerContext);
        if (notes == null) {
            val newNote = annotFuzz.generateRandom();
            mr.setNote(List.of(newNote));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Note in MedicationRequest", null, newNote));
        } else {
            val org = mr.getNote();
            val listFuzz = new ListFuzzerImpl<>(fuzzerContext, annotFuzz);
            listFuzz.fuzz(mr::getNote, mr::setNote);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Note in MedicationRequest", org, mr.getNote()));
        }

    }

    private void insuranceFuzz(MedicationRequest mr) {
        val referenceFuzzer = new ReferenceFuzzerImpl(fuzzerContext);
        val org = mr.hasInsurance() ? mr.getInsuranceFirstRep() : null;
        if (org == null) {
            val ins = referenceFuzzer.generateRandom();
            mr.setInsurance(List.of(ins));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Insurance in MedicationRequest", null, ins));
        } else {
            val listFuzz = new ListFuzzerImpl<>(fuzzerContext, referenceFuzzer);
            listFuzz.fuzz(mr::getInsurance, mr::setInsurance);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Insurance in MedicationRequest", org, mr.getInsurance()));
        }
    }

    private void subjectFuzz(MedicationRequest mr) {
        val referenceFuzzer = new ReferenceFuzzerImpl(fuzzerContext);
        val org = mr.hasSubject() ? mr.getSubject() : null;
        referenceFuzzer.fuzz(mr::hasSubject, mr::getSubject, mr::setSubject);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Subject in MedicationRequest", org, mr.hasSubject() ? mr.getSubject() : null));
    }

    private void requesterFuzz(MedicationRequest mr) {
        val referenceFuzzer = new ReferenceFuzzerImpl(fuzzerContext);
        val org = mr.hasRequester() ? mr.getRequester() : null;
        referenceFuzzer.fuzz(mr::hasRequester, mr::getRequester, mr::setRequester);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Requester in MedicationRequest", org, mr.hasRequester() ? mr.getRequester() : null));
    }

    private void intendFuzz(MedicationRequest mr) {
        val org = mr.hasIntent() ? mr.getIntent() : null;
        MedicationRequest.MedicationRequestIntent newEntry;
        if (org != null) {
            newEntry = fuzzerContext.getRandomOneOfClass(MedicationRequest.MedicationRequestIntent.class, List.of(org, MedicationRequest.MedicationRequestIntent.NULL));
        } else {
            newEntry = fuzzerContext.getRandomOneOfClass(MedicationRequest.MedicationRequestIntent.class, List.of(MedicationRequest.MedicationRequestIntent.NULL));
        }
        mr.setIntent(newEntry);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Intend in MedicationRequest", org, newEntry));
    }

    private void statusFuzz(MedicationRequest mr) {
        val org = mr.hasStatus() ? mr.getStatus() : null;
        MedicationRequest.MedicationRequestStatus newEntry;
        if (org != null) {
            newEntry = fuzzerContext.getRandomOneOfClass(MedicationRequest.MedicationRequestStatus.class, List.of(org, MedicationRequest.MedicationRequestStatus.NULL, MedicationRequest.MedicationRequestStatus.UNKNOWN));
        } else {
            newEntry = fuzzerContext.getRandomOneOfClass(MedicationRequest.MedicationRequestStatus.class, List.of(MedicationRequest.MedicationRequestStatus.NULL, MedicationRequest.MedicationRequestStatus.UNKNOWN));
        }
        mr.setStatus(newEntry);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Status in MedicationRequest", org, newEntry));
    }

    private void langFuzz(MedicationRequest mr) {
        var org = mr.hasLanguage() ? mr.getLanguageElement() : null;
        fuzzerContext.getLanguageCodeFuzzer().fuzz(mr::hasLanguage, mr::getLanguage, mr::setLanguage);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Language in MedicationRequest", org, mr.hasLanguage() ? mr.getLanguage() : null));
    }

    private void identFuzz(MedicationRequest mr) {
        var fhirIdentifierFuzzer = new IdentifierFuzzerImpl(fuzzerContext);
        if (!mr.hasIdentifier()) {
            val ident = fhirIdentifierFuzzer.generateRandom();
            mr.setIdentifier(List.of(ident));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Identifier in MedicationRequest:", null, ident));
        } else {
            val listFuzz = new ListFuzzerImpl<>(fuzzerContext, fhirIdentifierFuzzer);
            val ident = mr.getIdentifier();
            listFuzz.fuzz(mr::getIdentifier, mr::setIdentifier);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Identifier in MedicationRequest:", ident, mr.hasIdentifier() ? mr.getIdentifier() : null));
        }
    }

    private void metaFuzz(MedicationRequest mr) {
        MetaFuzzerImpl metaFuzzer = new MetaFuzzerImpl(fuzzerContext);
        val meta = mr.hasMeta() ? mr.getMeta() : null;
        metaFuzzer.fuzz(mr::hasMeta, mr::getMeta, mr::setMeta);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Meta in MedicationRequest:", meta, mr.hasMeta() ? mr.getMeta() : null));
    }

    private void extensionFuzz(MedicationRequest m) {
        if (fuzzerContext.getFuzzConfig().getDetailSetup() == null)
            fuzzerContext.getFuzzConfig().setDetailSetup(new HashMap<>());
        fuzzerContext.getFuzzConfig().getDetailSetup().put("TriggertByMedRequest", "TRUE");
        val extensionFuzzer = new ExtensionFuzzerImpl(fuzzerContext);
        if (!m.hasExtension()) {
            val ext = extensionFuzzer.generateRandom();
            m.setExtension(List.of(ext));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Extension in MedicationRequest", null, ext));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
            val org = m.getExtension();
            listFuzzer.fuzz(m::getExtension, m::setExtension);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Extension in MedicationRequest", org, m.hasExtension() ? m.getExtension() : null));
        }
        fuzzerContext.getFuzzConfig().getDetailSetup().remove("TriggertByMedRequest");

    }


    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }
}