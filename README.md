
Build the benchmarks like this:

    mvn clean package

Run the full benchmark suite (note that this will take a while) like this:

    java -jar target/object-pool-benchmarks.jar fullsuite

The results of the fullsuite benchmark will be put in a new subdirectory of `results`.

Run individual benchmarks like this:

    java -jar target/object-pool-benchmarks.jar ".*ClaimRelease.*"

For instance, running a single-threaded claim-release throughput benchmark could look like this:

    java -jar target/object-pool-benchmarks.jar ".*ClaimRelease.*" -t 1 -i 6 -wi 6 -f 1 -tu s -bm thrpt

Set pool-size by specifying `-jvmArgs -Dpool.size=10`.

Set the number of objects that each benchmark "operation" shall claim by specifying `-jvmArgs -Dcycle.claim.count=1`.

Likewise set the `cost.of.allocation`, `cost.of.deallocation` and `cost.of.validation` objects.

The cost is expressed in units of 'cpu' (arbitary but stable units of CPU busy-work) or 'ms'
(milliseconds of sleep) like so: `-jvmArgs -Dcost.of.allocation=100cpu`. By default there is no cost.

Specially configured executions do not automatically save their results in the `results` directory. Only the
fullsuite execution does that.

Ask JMH for further possible CLI configurations like this:

    java -jar target/object-pool-benchmarks.jar -h

