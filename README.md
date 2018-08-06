# Super-bit based fast approximate K-means (SBK-means)

This is a maven based java project that provides the functionality to cluster a massive (billion scale)
collection of dense vectors (e.g. document embedding of Web pages).

Specifically speaking, the program executes the following steps.

1. Reads the vectors from a text file; each line of the input file represents a vector (values are space separated).
Each line could be optionally followed by a reference class (an integer value) separted by a tab.
An example input file of 5 dimensional vectors (ref classes number 0 and 1) is as follows.

```
-2.0163 -3.1664 -4.0982 -4.3897 1.0958	0 
4.8630 0.3199 2.7189 1.3172 2.2896	1
```

2. The program does not store the vectors themselves in memory. Instead it saves a 64-bit (long) signature for each vector.
Each vector's signature thus requires 8 bytes of storage in memory. To store 1M vectors, one would thus need approximately need 8Mb of memory.

3. The program uses the signatures to compute estimated cosine similarity values.

4. During each iteration of K-means, the program recomputes the signatures of the new centroids by another pass through the disk-resident vectors.

## Running the project on synthetic data

To run the project, the first step is to build the classes with the maven compilers.

```
mvn compile
```

Then execute the following steps.
```
sh scripts/sbkmeans_synthetic.sh <num-data-points> <dimension> <num-ref-classes> <value of K for K-means>
```

For example, a sample invocation is
```
sh scripts/sbkmeans_synthetic.sh 100000 100 10 10
```

