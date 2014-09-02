#!/bin/sh

THREAD_COUNTS=( 1 2 3 4 6 8 )
FORKS=10
ITERS=10
WITERS=10
POOL_SIZE=100

if [ "Linux" == "$(uname -s)" ]
then
  CORES="$(grep -c "processor" /proc/cpuinfo)"
else
  CORES="$(sysctl hw.ncpu | cut -d ' ' -f 2)"
fi
DIR="results/$(date "+%Y-%m-%d")-$(uname -s)-$(uname -m)-${CORES}core"

mkdir -p $DIR
mvn clean package

for THREADS in ${THREAD_COUNTS[@]}
do
  echo "Benchmarking with $THREADS worker threads"
  java -jar target/benchmarks.jar '.*Stormpot.*' -tu us -rf json -f $FORKS -i $ITERS -wi $WITERS -t $THREADS -p poolSize=$POOL_SIZE -bm thrpt -rff "${DIR}/${THREADS}-threads-thrpt.json"
done
