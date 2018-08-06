# Super-bit based fast approximate K-means (SBK-means)

This is a maven based java project that provides the functionality to cluster a massive (billion scale)
collection of dense vectors (e.g. document embedding of Web pages).

Specifically speaking, the program executes the following steps.

1. Reads the vectors from a text file; each line of the input file represents a vector (values are space separated).
Each line could be optionally followed by a reference class (an integer value) separted by a tab.
An example input file of 100 dimensional vectors (ref classes number 0 and 1) is as follows.

```
-2.0163 -3.1664 -4.0982 -4.3897 1.0958 -5.0709 -6.5136 -2.2066 -4.1871 -5.6862 -5.4851 -5.2048 -4.7153 -2.9654 -5.8969 -4.7516 -1.5632 -5.7968 -6.9994 -4.5639 -3.3999 -5.9574 2.0949 -5.8661 0.4983 -5.0436 -1.2210 -2.7664 -6.5661 -7.0724 -0.8433 -1.5053 -6.8975 -3.5987 -6.3981 -4.9324 -4.4294 -5.3785 -8.4301 -1.8796 -3.2526 -4.9011 -2.6371 -6.6604 -5.6168 -7.5522 2.5393 -6.8212 -7.3803 -7.2237 0.1382 -5.7584 -3.8628 -5.0830 -2.5424 -0.2768 -4.3146 -3.9154 -7.3760 -9.1311 -2.7630 -6.4273 -6.3664 -5.7422 -5.8856 -2.9880 -5.0466 -4.7764 -9.8739 -7.0612 -2.6221 -0.0653 -2.5174 -5.8244 -9.6511 -5.5853 -5.8145 -3.1379 -4.2252 -5.3163 -0.8885 -6.9874 -3.0541 -6.1991 -4.4046 -7.6635 -5.4241 -3.5211 -0.6163 -5.7196 -2.1838 -7.0814 1.0658 -5.5807 -2.5779 -7.2293 -7.0928 -6.3560 0.5281 -4.0741	0
4.8630 0.3199 2.7189 1.3172 2.2896 6.1713 5.7675 4.5056 1.6021 4.6235 1.4324 4.2164 1.3262 3.3072 4.6611 1.6659 5.5176 7.4233 5.3090 5.7193 2.5425 2.6475 0.6607 7.0063 1.0332 4.9826 3.8671 0.3404 3.1158 1.4133 1.5227 5.6264 4.3533 2.6113 3.5469 4.7750 2.8758 -0.5963 6.3084 3.4385 5.3099 2.2437 2.2196 2.5956 0.6540 0.9328 1.5263 -1.6999 3.1054 2.5633 0.1672 4.2488 6.0853 4.8878 1.6012 4.5826 2.5797 3.1220 1.8719 1.6817 8.0459 4.8916 2.5113 5.0307 3.9897 5.5691 4.1743 6.2058 6.5516 3.0244 4.2172 5.6676 3.5458 5.5301 5.2825 5.3259 6.8670 5.9726 2.7946 3.0213 0.0512 1.9864 2.2308 6.7898 1.0458 3.9689 5.8130 5.1168 6.1112 1.0995 4.1426 3.6159 3.1442 1.0027 4.8454 2.2582 -1.0014 2.4655 2.0117 8.2490	1
```

2. The program does not store the vectors themselves in memory. Instead it saves a 64-bit (long) signature for each vector.
Each vector's signature thus requires 8 bytes of storage in memory. To store 1M vectors, one would thus need approximately need 8Mb of memory.

3. The program uses the signatures to compute estimated cosine similarity values.

4. During each iteration of K-means, the program recomputes the signatures of the new centroids by another pass through the disk-resident vectors.

## Running the project on synthetic data

To run the project, simply type from your command-line.
```
sh scripts/sbkmeans_synthetic.sh <num-data-points> <dimension> <num-ref-classes> <value of K for K-means>
```

For example, a sample invocation is
```
sh scripts/sbkmeans_synthetic.sh 100000 100 10 10
```

