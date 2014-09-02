
Build the benchmarks like this:

    mvn clean package

Run the full benchmark suite (note that this will take a while) like this:

    ./fullsuite.sh

The results of the fullsuite benchmark will be put in a new subdirectory of `results`.

Run individual benchmarks like this:

    java -jar target/benchmarks.jar ".*ClaimRelease.*"

For instance, running a single-threaded claim-release throughput benchmark could look like this:

    java -jar target/benchmarks.jar ".*ClaimRelease.*" -t 1 -i 6 -wi 6 -f 1 -tu ms -bm thrpt

Set pool-size by specifying `-p poolSize=10`.

Costs can be set with `-jvmArgs -Dcost.of.allocation=?`, `-jvmArgs -Dcost.of.deallocation=?`
and `-jvmArgs -Dcost.of.validation=?` objects.

The cost is expressed in units of 'cpu' (arbitrary but stable units of CPU busy-work) or 'ms'
(milliseconds of sleep) like so: `-jvmArgs -Dcost.of.allocation=100cpu`. By default there is no cost.

Specially configured executions do not automatically save their results in the `results` directory. Only the
fullsuite execution does that.

Ask JMH for further possible CLI configurations like this:

    java -jar target/benchmarks.jar -h

