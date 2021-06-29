/*
 * Created by sahmad on 29/01/19 11:28
 * UniProt Consortium.
 * Copyright (c) 2002-2019.
 *
 */

package uk.ac.ebi.uniprot.ds.importer.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import uk.ac.ebi.uniprot.ds.importer.listener.LogChunkListener;
import uk.ac.ebi.uniprot.ds.importer.listener.LogJobListener;
import uk.ac.ebi.uniprot.ds.importer.listener.LogStepListener;
import uk.ac.ebi.uniprot.ds.importer.util.Constants;

@Configuration
@EnableBatchProcessing
public class BatchConfigurationDiseaseService {
    @Bean
    public Job importUniProtDataJob(JobBuilderFactory jobBuilderFactory, JobExecutionListener jobExecutionListener,
                                    Step humDiseaseStep, Step uniProtStep, Step geneCoordsLoad,
                                    Step mondoDiseaseStep,
                                    Step parentChildLoadStep,
                                    Step chDrugLoad,
                                    Step alzheimerProteinLoad,
                                    Step siteMappingStep,
                                    Step descendentsLoadStep) {

        return jobBuilderFactory.get(Constants.DISEASE_SERVICE_DATA_LOADER)
                .incrementer(new RunIdIncrementer())
                .start(humDiseaseStep)//Load Diseases From HumDisease file
                .next(uniProtStep)// Load Human Protein from curated Protein file
                .next(geneCoordsLoad)// load gene co-ordinates
                .next(mondoDiseaseStep)// load synonyms and/or disease group from Mondo data
                .next(parentChildLoadStep)// create parents children relationship with mondo data
                .next(descendentsLoadStep)// add all the descendents of each disease in flattened manner
                .next(chDrugLoad)// load drugs
                .next(alzheimerProteinLoad)// Alzheimer disease protein load step
                .next(siteMappingStep)// Site mapping load step
                .listener(jobExecutionListener)
                .build();
    }


    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new LogJobListener();

    }

    @Bean
    public StepExecutionListener stepListener(){
        return new LogStepListener();
    }

    @Bean
    public ChunkListener chunkListener() {
        return new LogChunkListener();
    }
}
