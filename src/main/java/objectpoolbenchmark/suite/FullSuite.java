/*
 * Copyright (C) 2014 Chris Vest (mr.chrisvest@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package objectpoolbenchmark.suite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.logic.results.RunResult;
import org.openjdk.jmh.output.format.OutputFormat;
import org.openjdk.jmh.output.format.TextReportFormat;
import org.openjdk.jmh.output.results.ResultFormat;
import org.openjdk.jmh.output.results.ResultFormatFactory;
import org.openjdk.jmh.output.results.ResultFormatType;
import org.openjdk.jmh.runner.BenchmarkRecord;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

public class FullSuite {
  public static void main(String[] args) throws Exception {
    if (args.length == 1 && args[0].equals("fullsuite")) {
      runFullSuite();
    } else {
      org.openjdk.jmh.Main.main(args);
    }
  }

  private static final int[] threads = new int[] {1, 2, 3, 4, 6, 8, 12, 16, 24};
  private static final int[] poolSizes = new int[] {1, 10, 100, 1000};
  private static final int[] claimBatchSizes = new int[] {1, 2, 3};
  private static final Modus[] modi = new Modus[] {
    new Modus("Throughput") {
      @Override
      public void apply(OptionsBuilder builder) {
        builder.timeUnit(TimeUnit.SECONDS);
        builder.mode(Mode.Throughput);
      }
    },
    new Modus("SampleTime") {
      @Override
      public void apply(OptionsBuilder builder) {
        builder.timeUnit(TimeUnit.MICROSECONDS);
        builder.mode(Mode.SampleTime);
      }
    }
  };

  private static void runFullSuite() throws Exception {
    OptionsBuilder parentOptions = new OptionsBuilder();
    // fullsuite doesn't play with the cost functions, so only do ClaimRelease:
    parentOptions.include(".*ClaimRelease.*");

    // speed it all up for testing purpose:
    parentOptions.forks(1);
    parentOptions.warmupIterations(5);
    parentOptions.measurementIterations(3);

    int totalPermutations =
        threads.length * poolSizes.length * claimBatchSizes.length * modi.length;
    int currentPermutation = 0;

    SortedMap<Record, SortedMap<BenchmarkRecord,RunResult>> results = new TreeMap<>();

    for (int threadCount : threads) {
      for (int poolSize : poolSizes) {
        for (int claimBatchSize : claimBatchSizes) {
          if (poolSize < claimBatchSize * threadCount && claimBatchSize != 1) {
            currentPermutation++;
            continue;
          }
          for (Modus modus : modi) {
            OptionsBuilder options = new OptionsBuilder();
            options.parent(parentOptions);
            options.threads(threadCount);
            options.jvmArgs("-Dpool.size=" + poolSize);
            options.jvmArgs("-Dcycle.claim.count=" + claimBatchSize);
            modus.apply(options);

            currentPermutation++;
            System.out.printf("# Total progress: %3.2f%% complete%n", 100 * ((double) currentPermutation) / totalPermutations);

            SortedMap<BenchmarkRecord,RunResult> result = new Runner(options.build()).run();
            Record record = new Record(threadCount, poolSize, claimBatchSize, modus);
            results.put(record, result);
            System.out.println();
          }
        }
      }
    }

    report(results);
  }

  private static void report(
      SortedMap<Record, SortedMap<BenchmarkRecord, RunResult>> results) throws IOException {
    Properties sysprops = System.getProperties();
    File resultsDir = new File("results");
    File ourResultsDir = new File(resultsDir,
        String.format("%1$tY-%1$tm-%1$td_%2$s-%3$s-%4$score",
            new Date(),
            sysprops.getProperty("os.name").replace(' ', '_'),
            sysprops.getProperty("os.arch"),
            Runtime.getRuntime().availableProcessors()));

    if (ourResultsDir.exists() && ourResultsDir.isDirectory() || ourResultsDir.mkdirs()) {
      reportToDirectory(results, ourResultsDir, sysprops);
    } else {
      System.err.println(
          "WARN: Will print results to console because directory could not " +
              "be created: " + ourResultsDir.getAbsolutePath());

      reportToConsole(results);
    }
  }

  private static void reportToConsole(
      SortedMap<Record, SortedMap<BenchmarkRecord, RunResult>> results) {
    OutputFormat format = new TextReportFormat(System.out, VerboseMode.EXTRA);
    for (Map.Entry<Record, SortedMap<BenchmarkRecord, RunResult>> entry : results.entrySet()) {
      Record record = entry.getKey();
      SortedMap<BenchmarkRecord, RunResult> result = entry.getValue();
      System.out.printf("[ Threads: %s, Pool Size: %s, Claim Batch Size: %s, Modus: %s ]%n",
          record.threadCount, record.poolSize, record.claimBatchSize, record.modus);
      format.endRun(result);
    }
  }

  private static void reportToDirectory(
      SortedMap<Record, SortedMap<BenchmarkRecord, RunResult>> results,
      File ourResultsDir,
      Properties sysprops) throws IOException {
    File systemproperties = new File(ourResultsDir, "system.properties");

    if (!systemproperties.createNewFile()) {
      System.err.println(
          "ERROR: Could not create new file: " + systemproperties.getAbsolutePath());
      System.out.println("# Overwrite existing data? [y/N] ");
      if (System.in.read() != 'y') {
        return;
      }
      System.out.println("# Okay, overwriting.");
    }

    sysprops.store(
        new FileWriter(systemproperties),
        " JVM system properties for the fullsuite benchmark");

    for (Map.Entry<Record, SortedMap<BenchmarkRecord, RunResult>> entry : results.entrySet()) {
      Record record = entry.getKey();
      SortedMap<BenchmarkRecord, RunResult> result = entry.getValue();
      File resultFile = new File(ourResultsDir, record.toFilename());
      ResultFormat format = ResultFormatFactory.getInstance(ResultFormatType.JSON, resultFile.getAbsolutePath());
      format.writeOut(result);
    }
    System.out.println("# done");
  }

  private static abstract class Modus {
    private final String name;

    protected Modus(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    public abstract void apply(OptionsBuilder builder);
  }

  private static class Record implements Comparable<Record> {
    private final int threadCount;
    private final int poolSize;
    private final int claimBatchSize;
    private final Modus modus;

    public Record(int threadCount, int poolSize, int claimBatchSize, Modus modus) {
      this.threadCount = threadCount;
      this.poolSize = poolSize;
      this.claimBatchSize = claimBatchSize;
      this.modus = modus;
    }

    @Override
    public int compareTo(Record that) {
      return threadCount < that.threadCount? -1
           : threadCount > that.threadCount? 1
           : poolSize < that.poolSize? -1
           : poolSize > that.poolSize? 1
           : claimBatchSize < that.claimBatchSize? -1
           : claimBatchSize > that.claimBatchSize? 1
           : modus.name.compareTo(that.modus.name);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Record record = (Record) o;

      return claimBatchSize == record.claimBatchSize
          && poolSize == record.poolSize
          && threadCount == record.threadCount
          && modus.equals(record.modus);

    }

    @Override
    public int hashCode() {
      int result = threadCount;
      result = 31 * result + poolSize;
      result = 31 * result + claimBatchSize;
      result = 31 * result + modus.hashCode();
      return result;
    }

    public String toFilename() {
      return String.format("threads=%02d_poolSize=%04d_claimBatchSize=%d_%s",
          threadCount, poolSize, claimBatchSize, modus);
    }
  }
}
