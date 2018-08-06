#!/bin/bash

if [ $# -lt 5 ]
then
	echo "Usage: $0 <num vecs> <num dimensions> <num ref classes> <num predicted classes (K)> <estimate signatures of centroids (true/false)>"
	exit
fi

NUMVECS=$1
DIM=$2
NUMCLASSES=$3
K=$4
ITERS_KMEANS=10

DATAFILE=rndvecs.$NUMVECS.$DIM.$NUMCLASSES
PROPFILE=init_synthetic.$NUMVECS.$DIM.$NUMCLASSES.properties
OUTFILE=kmeans.out.$NUMVECS.$DIM.$NUMCLASSES

# create the data properties file

cat > $PROPFILE  << EOF1
datafile=$DATAFILE
syntheticdata.numsamples=$NUMVECS
syntheticdata.numgaussians=$NUMCLASSES
vec.numdimensions=$DIM
syntheticdata.outdir=./
kmeans.numclusters=$K
kmeans.iterations=$ITERS_KMEANS
kmeans.outfile=$OUTFILE
estimate.sum.signatures=$5
EOF1

if [ ! -e $DATAFILE ]
then
	echo "Generating data points..."
	mvn exec:java -Dexec.mainClass="ibm.research.drl.lshkmeans.RandomVecGenerator" -Dexec.args="$PROPFILE"
fi

echo "K-means clustering..."
mvn exec:java -Dexec.mainClass="ibm.research.drl.lshkmeans.SBKMeansClusterer" -Dexec.args="$PROPFILE"

cat $DATAFILE | awk '{print $NF}' > tmp1
paste $OUTFILE tmp1 > tmp2

echo "Evaluating o/p..."
mvn exec:java -Dexec.mainClass="ibm.research.drl.lshkmeans.ClustEval" -Dexec.args="tmp2"

rm tmp*

