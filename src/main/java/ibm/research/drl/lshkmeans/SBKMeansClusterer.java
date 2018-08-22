/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.research.drl.lshkmeans;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

/**
 *
 * @author dganguly
 */
public class SBKMeansClusterer {
    Properties prop;
    String propFile;
    SBVec[] sbvecs;
    int k, iters;
    SBVec[] centroids;
    int numVecs;
    VecReader vecReader;
    boolean estimateSumSigantures;
    boolean withProjections;
    
    public SBKMeansClusterer(String propFile) throws Exception {
        this.propFile = propFile;
        prop = new Properties();
        prop.load(new FileReader(propFile));
        k = Integer.parseInt(prop.getProperty("kmeans.numclusters"));
        iters = Integer.parseInt(prop.getProperty("kmeans.iterations", "10"));
        centroids = new SBVec[k];
        
        estimateSumSigantures = Boolean.parseBoolean(prop.getProperty("estimate.sum.signatures", "false"));
        withProjections = Boolean.parseBoolean(prop.getProperty("average.centroid.estimation", "false"));
    }
    
    void loadVecs() throws Exception {
        vecReader = new VecReader(propFile);
        sbvecs = vecReader.getSBVecs();
        numVecs = sbvecs.length;
    }
    
    void kmeans() throws Exception {
        System.out.println("Loading data points");
        loadVecs();
        
        System.out.println("Initializing centroids");
        initCentroids();
        
        for (int i=0; i < iters; i++) {
            System.out.println("Iteration " + i + " of K-means...");
            buildPartition();
            
            recomputeCentroids();
        }
        
        writeOutput();
    }
    
    SBVec[] estimateNewCentroids() {
        SBVec[] newcentroids = new SBVec[k];
        
        for (int i=0; i < sbvecs.length; i++) {
            if (newcentroids[sbvecs[i].clusterId] == null) {
                newcentroids[sbvecs[i].clusterId] = sbvecs[i]; // first vector in this partition 
            }
            else {
                newcentroids[sbvecs[i].clusterId].vecsum(sbvecs[i]); // subsequent times -- do vector sum
            }
        }
        return newcentroids;
    }
            
            
            
    void recomputeCentroids() throws Exception {
        if (!estimateSumSigantures) {
            centroids = vecReader.recomputeSignaturesOfCentroids(sbvecs, centroids);
            return;
        }
            
        System.out.println("Performing in-mem recomputations (estimated with signatures)...");

        SBVec[] newcentroids = estimateNewCentroids();        
        this.centroids = newcentroids;
    }
    
    void buildPartition() {
        int sim, maxsim;
        int clusterToAssign;

        for (int i=0; i < numVecs; i++) {
            maxsim = 0;
            clusterToAssign = 0;
            
            for (int j=0; j < k; j++) {
                if (centroids[j]==null)
                    continue;
                
                sim = sbvecs[i].computeHammingSim(centroids[j]);
                if (sim > maxsim) {
                    maxsim = sim;
                    clusterToAssign = j;
                }
            }
            sbvecs[i].clusterId = clusterToAssign;
        }
    }
    
    SBVec createSBVecObj() {
        SBVec sbvec = !withProjections? new SBVec(0) : new SBVecProjStats(0);
        return sbvec;
    }
    
    void copyTo(SBVec dest, SBVec src) {
        dest.signature = src.signature;
        dest.clusterId = src.clusterId;
        if (withProjections)
            ((SBVecProjStats)dest).avgProjSignature = ((SBVecProjStats)src).avgProjSignature;
    }
    
    void initCentroids() {
        int randomId;
        SBVec tmp = createSBVecObj();
        
        for (int i=0; i < k; i++) {
            randomId = (int)(Math.random() * (numVecs-i));
            
            // swap the selected point with the last one -- no duplicate selection
            copyTo(tmp, sbvecs[randomId]);
            copyTo(sbvecs[randomId], sbvecs[numVecs-1-i]);
            copyTo(sbvecs[numVecs-1-i], tmp);
            
            centroids[i] = sbvecs[numVecs-1-i];
        }
    }
    
    public void writeOutput() throws Exception {
        FileWriter fw = new FileWriter(prop.getProperty("kmeans.outfile"));
        BufferedWriter bw = new BufferedWriter(fw);
        
        for (SBVec sbvec: sbvecs) {
            bw.write(String.valueOf(sbvec.clusterId));
            bw.newLine();
        }
        
        bw.close();
        fw.close();
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[1];
            System.out.println("Usage: java SBKMeansClusterer <prop-file>");
            args[0] = "init_synthetic.properties";
        }
        
        try {
            SBKMeansClusterer sbkmeans = new SBKMeansClusterer(args[0]);
            sbkmeans.kmeans();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
