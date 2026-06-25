package com.rockernun.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job helloJob;

    public JobRunner(JobLauncher jobLauncher, Job helloJob) {
        this.jobLauncher = jobLauncher;
        this.helloJob = helloJob;
    }

    @Override
    public void run(String... args) throws Exception {
        JobParameters jobParameter = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(helloJob, jobParameter);
    }
}
