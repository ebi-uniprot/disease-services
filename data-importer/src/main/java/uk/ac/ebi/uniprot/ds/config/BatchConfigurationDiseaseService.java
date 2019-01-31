/*
 * Created by sahmad on 29/01/19 11:28
 * UniProt Consortium.
 * Copyright (c) 2002-2019.
 *
 */

package uk.ac.ebi.uniprot.ds.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.uniprot.ds.listener.LogJobListener;
import uk.ac.ebi.uniprot.ds.listener.LogStepListener;
import uk.ac.ebi.uniprot.ds.util.Constants;

@Configuration
@EnableBatchProcessing
public class BatchConfigurationDiseaseService {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    public Job importUniProtDataJob(JobExecutionListener jobExecutionListener, Step humDiseaseStep, Step uniProtStep) {

        return jobBuilderFactory.get(Constants.DISEASE_SERVICE_DATA_LOADER)
                .incrementer(new RunIdIncrementer())
                .start(humDiseaseStep)
                .next(uniProtStep)
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
}