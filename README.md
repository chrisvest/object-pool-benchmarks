
Build the benchmarks like this:

    mvn clean package

Run the full benchmark suite (note that this will take a while) like this:

    java -jar target/object-pool-benchmarks.jar fullsuite

Run individual benchmarks like this:

    java -jar target/object-pool-benchmarks.jar ".*ClaimRelease.*"

For instance, running a single-threaded claim-release throughput benchmark could look like this:

    java -jar target/object-pool-benchmarks.jar ".*ClaimRelease.*" -t 1 -i 6 -wi 6 -f 1 -tu s -bm thrpt

Set pool-size by specifying `-jvmArgs -Dpool.size=10`.
Set object time-to-live by specifying `-jvmArgs -Dpool.ttl.ms=60000`.
Set the number of objects that each benchmark "operation" shall claim by specifying `-jvmArgs -Dcycle.claim.count=1`.

Ask JMH for further possible CLI configurations like this:

    java -jar target/object-pool-benchmarks.jar -h

