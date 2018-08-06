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
    
    public SBKMeansClusterer(String propFile) throws Exception {
        this.propFile = propFile;
        prop = new Properties();
        prop.load(new FileReader(propFile));
        k = Integer.parseInt(prop.getProperty("kmeans.numclusters"));
        iters = Integer.parseInt(prop.getProperty("kmeans.iterations", "10"));
        centroids = new SBVec[k];
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
            
            System.out.println("Recomputing centroids (involves disk access to read data)...");
            centroids = vecReader.recomputeSignaturesOfCentroids(sbvecs, centroids);
        }
        
        writeOutput();
    }
    
    void buildPartition() {
        float sim, maxsim;
        int clusterToAssign;
        
        for (int i=0; i < numVecs; i++) {
            maxsim = 0;
            clusterToAssign = 0;
            
            for (int j=0; j < k; j++) {
                sim = sbvecs[i].computeSim(centroids[j]);
                if (sim > maxsim) {
                    maxsim = sim;
                    clusterToAssign = j;
                }
            }
            sbvecs[i].clusterId = clusterToAssign;
        }
    }
    
    void initCentroids() {
        int randomId;
        for (int i=0; i < k; i++) {
            randomId = (int)(Math.random() * numVecs);
            centroids[i] = sbvecs[randomId];
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
