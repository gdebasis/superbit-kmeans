/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.research.drl.lshkmeans;

import java.io.*;
import java.util.*;

/**
 * Format of the input file:
 * ([floating-point-value \white-space]+) \tab (ref-cluster) 
 * 
 * @author dganguly
 */

class Vec {
    double[] x;
    int refClass;
    
    Vec(int dimension) {
        x = new double[dimension];
    }
    
    Vec(int refClass, double[] x) {
        this.x = x;
        this.refClass = refClass;
    }
    
    Vec(String line) {
        String[] tokens = line.split("\\t");
        refClass = Integer.parseInt(tokens[1]);
        
        String[] values = tokens[0].split("\\s+");
        x = new double[values.length];
        
        for (int i=0; i < values.length; i++) {
            x[i] = Double.parseDouble(values[i]);
        }
    }
    
    void sum(Vec that) {
        for (int i=0; i < this.x.length; i++) {
            x[i] += that.x[i];
        }
    }
    
    void scalarMultiply(float a) {
        for (int i=0; i < this.x.length; i++) {
            x[i] *= a;
        }
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        for (int i=0; i < x.length; i++) {
            buff.append(String.format("%.4f", x[i]));
            if (i < x.length-1)
                buff.append(" ");
        }
        buff.append("\t");
        buff.append(refClass);
        return buff.toString();
    }
}

public class VecReader {
    Properties prop;
    String inFile;
    SBVec[] vecs;  // each object requires 8 bytes of memory
    int numvecs;
    int numDimensions;
    
    final String randomSamplesFileName(int numGaussians, int numDimensions) {
        String fileName = prop.getProperty("syntheticdata.outdir") + "/" + 
                "data." + numGaussians + "." + numDimensions + ".txt";
        return fileName;
    }
    
    public VecReader(String propFile) throws Exception {
        prop = new Properties();
        prop.load(new FileReader(propFile));         
        
        int numGaussians = Integer.parseInt(prop.getProperty("syntheticdata.numgaussians"));
        numDimensions = Integer.parseInt(prop.getProperty("vec.numdimensions"));
        
        inFile = randomSamplesFileName(numGaussians, numDimensions);
        
        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
        
        numvecs = getNumVecs(br);
        vecs = new SBVec[numvecs];
        
        br.close();
        fr.close();
        
        fr = new FileReader(inFile);
        br = new BufferedReader(fr);
        
        loadVecs(br);
        
        br.close();
        fr.close();
    }
    
    // During a K-means iteration recompute signature vectors
    SBVec[] recomputeSignaturesOfCentroids(SBVec[] vecs, SBVec[] centroids) throws Exception {
        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
        
        int k = centroids.length;
        
        SBVec[] newcentroidSignatures = new SBVec[k];
        Vec[] centroidVecs = new Vec[k];
        int[] partitionSizes = new int[k];
        
        for (int i=0; i < k; i++)
            centroidVecs[i] = new Vec(numDimensions);
        
        String line;
        
        int i = 0;
        while ((line = br.readLine()) != null) {
            Vec v = new Vec(line);
            
            centroidVecs[vecs[i].clusterId].sum(v);
            partitionSizes[vecs[i].clusterId]++;
            
            i++;
        }
        br.close();
        
        for (i=0; i < centroidVecs.length; i++) {
            Vec v = centroidVecs[i];
            v.scalarMultiply(1/(float)partitionSizes[i]);
            
            newcentroidSignatures[i] = new SBVec(v); // signature for new centroid
        }
        
        br.close();
        fr.close();
        
        return newcentroidSignatures;
    }
    
    final void loadVecs(BufferedReader br) throws Exception {
        String line;
        boolean init = false;
        int vec_index = 0;
        
        while ((line = br.readLine()) != null) {
            Vec v = new Vec(line);
            if (!init) {
                SBVec.init(v.x.length);
                init = true;
            }
            
            vecs[vec_index++] = new SBVec(v);
        }
    }
    
    final int getNumVecs(BufferedReader br) throws Exception {
        int numLines = 0;
        while ((br.readLine())!=null) numLines++;
        return numLines;
    }
    
    SBVec[] getSBVecs() { return vecs; }
}
