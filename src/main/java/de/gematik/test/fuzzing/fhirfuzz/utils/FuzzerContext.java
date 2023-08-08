/*
 * ${GEMATIK_COPYRIGHT_STATEMENT}
 */

package de.gematik.test.fuzzing.fhirfuzz.utils;

import com.github.javafaker.Faker;
import de.gematik.test.fuzzing.fhirfuzz.impl.numberfuzzer.IntFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.IdFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.LanguageCodeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.StringFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.UrlFuzzImpl;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Type;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


@Slf4j
public class FuzzerContext {

    @Delegate
    @Getter
    private final Random random;
    @Getter
    private final FuzzConfig fuzzConfig;
    @Getter
    private final Faker faker;
    @Getter
    private final StringFuzzImpl stringFuzz;
    @Getter
    private final UrlFuzzImpl urlFuzz;
    @Getter
    private final IdFuzzerImpl idFuzzer;
    @Getter
    private final LanguageCodeFuzzerImpl languageCodeFuzzer;
    @Getter
    private final IntFuzzImpl intFuzz;

    public FuzzerContext(FuzzConfig fuzzConfig) {
        this(new SecureRandom(), fuzzConfig);
    }

    @Getter
    private final List<FuzzOperationResult<?>> operationLogs;

    public FuzzerContext(Random random, FuzzConfig fuzzConfig) {
        this.random = random;
        if (fuzzConfig.getPercentOfAll() == null) {
            log.info("fuzzConfig.getPercentOfAll() has been set up default to 5 %");
            fuzzConfig.setPercentOfAll(5.00f);
        }
        if (fuzzConfig.getPercentOfEach() == null) {
            log.info("fuzzConfig.getPercentOfEach() has been set up default to 5 %");
            fuzzConfig.setPercentOfEach(5.00f);
        }
        if (fuzzConfig.getDetailSetup() == null) {
            fuzzConfig.setDetailSetup(new HashMap<>());
        }
        this.fuzzConfig = fuzzConfig;
        this.operationLogs = new LinkedList<>();
        faker = new Faker(new Locale("de"));
        this.stringFuzz = new StringFuzzImpl(this);
        this.urlFuzz = new UrlFuzzImpl(this);
        this.idFuzzer = new IdFuzzerImpl(this);
        this.languageCodeFuzzer = new LanguageCodeFuzzerImpl(this);
        this.intFuzz = new IntFuzzImpl(this);
    }

    public void clearOperationLogs() {
        this.operationLogs.clear();
    }

    public Long generateFakeLong() {
        return generateFakeLong(Long.MAX_VALUE);
    }

    public Long generateFakeLong(Long bound) {
        if (bound < 0) {
            bound = bound * (-1L);
        }
        return random.nextLong(bound);
        //return faker.number().randomNumber();
    }


    /**
     * generates a conditional chance
     * condition is set in FuzzConfig:
     * as Float percentOfAll
     *
     * @return bool
     */
    public boolean conditionalChance() {
        return conditionalChance(fuzzConfig.getPercentOfAll());
    }

    /**
     * generates a conditional chance
     * if percent is NaN it will be set to 5%
     *
     * @param percent as Float
     * @return bool
     */
    public boolean conditionalChance(Float percent) {
        if (percent.isNaN()) {
            percent = 5.00f;
            log.info("caused by given Input (NaN) percent is set up to 5.00%");
        }
        float v = random.nextFloat(100f);
        return v < percent;
    }

    /**
     * Method to generate a Random List with parts of the given List
     * !! If useAllMutators in FuzzConig is True, the original given List will be returned !!
     *
     * @param <T>List
     * @return part of <T>List
     */
    public <T> List<T> getRandomPart(List<T> l) {
        if (l == null || l.size() == 0) {
            return new LinkedList<>();
        }
        if (fuzzConfig.getUseAllMutators() != null && fuzzConfig.getUseAllMutators()) {
            return l;
        }
        if (fuzzConfig.getUseAllMutators() != null && fuzzConfig.getUsedPercentOfMutators() > 100) {
            return l;
        }
        float mutationFactor;
        if (fuzzConfig.getUsedPercentOfMutators() == null) {
            mutationFactor = 0.5f;
            log.info("Given Attribute usedPercentOfMutators was null !! setup default to 0.5%");
        } else {
            mutationFactor = fuzzConfig.getUsedPercentOfMutators() / 100;
        }
        if (mutationFactor <= 0.0f) {
            return new LinkedList<T>();
        }
        float initialCap = random.nextFloat((l.size() * mutationFactor));
        if (l.size() < 5 /*&& mutationFactor < 0.2*/) initialCap = random.nextFloat(3);
        if (l.size() < 3 /*&& mutationFactor < 0.2*/) initialCap = random.nextFloat(2);

        if (initialCap > l.size()) initialCap = l.size();

        val erg = new ArrayList<T>((int) initialCap);
        for (int i = 0; i < initialCap; i++) {
            val idx = random.nextInt(l.size());
            erg.add(l.get(idx));
        }
        return erg;
    }

    public <T extends Resource> Boolean shouldFuzz(T t) {
        return (t != null && this.conditionalChance(fuzzConfig.getPercentOfAll()));
    }
    public <T extends Type> Boolean shouldFuzz(T t) {
        return this.shouldFuzz(List.of(t));
    }
    public <T > Boolean shouldFuzz(T t) {
        return (t != null && this.conditionalChance(fuzzConfig.getPercentOfAll()));
    }

    public <T extends Type> Boolean shouldFuzz(List<T> t) {
        return (t != null && this.conditionalChance(fuzzConfig.getPercentOfAll()));
    }


    public void addLog(FuzzOperationResult<?> fuzzOperationResult) {
        operationLogs.add(fuzzOperationResult);
    }

    /**
     * generates a random date
     * between 1970 and 2000
     *
     * @return Date
     */
    public Date getRandomDate() {
        return getRandomDate(10L);
    }

    /**
     * to hit a year before 2000 set param to 1
     * use 10 to get a Date after 2000
     *
     * @param factor 1 == Year < 2000 || 10 == Year < 2000
     * @return a date
     * @ 10 most results are later than 2000
     * and so on ...
     */
    public Date getRandomDateWithFactor(long factor) {
        return getRandomDate(factor);
    }

    public Date getRandomDate(long factor) {
        long maxYear = 900000000000L * factor;
        return new Date(generateFakeLong(maxYear));
    }


    public <E extends Enum<?>> E getRandomOneOfClass(Class<E> eClass) {
        return getRandomOneOfClass(eClass, List.of());
    }


    public <E extends Enum<?>> E getRandomOneOfClass(Class<E> eClass, E excludeThis) {
        {
            return getRandomOneOfClass(eClass, List.of(excludeThis));
        }
    }

    public <E extends Enum<?>> E getRandomOneOfClass(Class<E> eClass, List<E> excludeThis) {
        val eList = Arrays.stream(eClass.getEnumConstants())
                .filter(ec -> !excludeThis.contains(ec)).toList();
        if (eList.isEmpty()) {
            throw new IllegalArgumentException("Enum has no fields or given exclude List matched Enum!");
        }
        val index = random.nextInt(eList.size());
        return eList.get(index);
    }



}
