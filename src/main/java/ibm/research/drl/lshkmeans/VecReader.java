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
    
    public final String randomSamplesFileName() {
        String fileName = prop.getProperty("datafile");
        return fileName;
    }
    
    public VecReader(String propFile) throws Exception {
        prop = new Properties();
        prop.load(new FileReader(propFile));         
        
        numDimensions = Integer.parseInt(prop.getProperty("vec.numdimensions"));
        inFile = randomSamplesFileName();
        
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
        
        System.out.println("Recomputing centroids (involves disk access to read data)...");
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
        boolean withProjections = Boolean.parseBoolean(prop.getProperty("average.centroid.estimation", "false"));
        
        String line;
        boolean init = false;
        int vec_index = 0;
        Vec[] projections = new Vec[numvecs];
        
        while ((line = br.readLine()) != null) {
            Vec v = new Vec(line);
            if (!init) {
                SBVec.init(v.x.length);
                init = true;
            }
            
            if (withProjections) {
                vecs[vec_index] = new SBVecProjStats(v);
                projections[vec_index] = new Vec(0, ((SBVecProjStats)vecs[vec_index]).getProjections(v));
            }
            else {
                vecs[vec_index] = new SBVec(v);
            }
            
            vec_index++;
        }
        
        if (withProjections) {
            // Construct the 64 bit signature vectors based on average values
            // of the projections.
            SBVec.normalizeProjections();
            
            for (int i=0; i < numvecs; i++) {
                ((SBVecProjStats)vecs[i]).encodeProjections(projections[i]);
                
                /*
                System.out.println(String.format("signature[%d] = %x, proj-signature[%d] = %x",
                            i, vecs[i].signature, i, ((SBVecProjStats)vecs[i]).avgProjSignature));
                */
            }
        }
    }
    
    final int getNumVecs(BufferedReader br) throws Exception {
        int numLines = 0;
        while ((br.readLine())!=null) numLines++;
        return numLines;
    }
    
    SBVec[] getSBVecs() { return vecs; }
}
