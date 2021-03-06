#!/bin/sh

# Used to test the Java Socket functionality locally. Uses mpi as the parallelization methodology.

mvn clean install -DskipTests

cd ./target
count=3
mpirun --oversubscribe -np $count java -cp *jar-with-dependencies.jar ee.ut.jbizur.InitMainMultiJVM $count false

# arguments:
# arg1 = the count of the nodes.
# arg2 = true if the system profiling is enabled, false otherwise.
# arg3 = multicast port number.
# arg4 = multicast group address.
