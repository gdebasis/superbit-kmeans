/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.research.drl.lshkmeans;

import ibm.research.drl.lsh.SuperBit;
import java.util.*;

/**
 *
 * @author dganguly
 */
public class SBVec {    
    long signature;
    int clusterId;      // the assigned cluster id during a K-means iteration
    
    static final int SIGNATURE_CODELEN = 64;
    static final int N = 8;
    static final int L = 8;  // N*L MUST BE EQUAL to CODELEN
    static final int SEED = 123456;
    static double[] avgProjectionsPos;  // running average of projected values along each dimension -- filled while data loading
    static double[] avgProjectionsNeg;  // running average of projected values along each dimension -- filled while data loading
    static int[] numPos, numNeg;
    static SuperBit sb;
    
    public static void init(int dim) {
        sb = new SuperBit(dim, N, L, SEED);
        avgProjectionsPos = new double[SIGNATURE_CODELEN];
        avgProjectionsNeg = new double[SIGNATURE_CODELEN];
        numPos = new int[SIGNATURE_CODELEN];
        numNeg = new int[SIGNATURE_CODELEN];
    }
    
    public SBVec(double[] x) {
        boolean[] bits = sb.signature(x);        
        encodeSignature(bits);
    }
    
    public SBVec(Vec vec) {
        this(vec.x);
    }
        
    public SBVec(long signature) {
        this.signature = signature;
    }
    
    final void encodeSignature(boolean[] bits) {
        long mask;
        
        for (int i=0; i < bits.length; i++) {
            mask = (bits[i]? 1l : 0l)<<i;
            if (mask != 0)
                signature = signature | mask;
        }
    }
    
    float computeCosSim(SBVec that) {
        long thisSignature = this.signature;
        long thatSignature = that.signature;
        byte sum = 0;
        long bitvalue;
        
        for (int i=0; i < SIGNATURE_CODELEN; i++) {
            bitvalue = (thisSignature&1l) ^ (thatSignature&1l);
            if (bitvalue==0)
                sum++;
            thisSignature = thisSignature>>1;
            thatSignature = thatSignature>>1;
        }
        
        return (float)Math.cos((1 - sum/(float)SIGNATURE_CODELEN) * Math.PI);
    }

    int computeHammingSim(SBVec that) {        
        int numBitsNotMatching = Long.bitCount(this.signature ^ that.signature); // Hamming distance
        int numBitsMatching = SIGNATURE_CODELEN - numBitsNotMatching;
        return numBitsMatching;
    }
    
    float computeCosSimFast(SBVec that) {        
        int numBitsNotMatching = Long.bitCount(this.signature ^ that.signature); // Hamming distance
        int numBitsMatching = SIGNATURE_CODELEN - numBitsNotMatching;
        
        return (float)Math.cos((1 - numBitsMatching/(float)SIGNATURE_CODELEN) * Math.PI);
    }
    
    long signatureSum(long aSignature, long bSignature) {
        long cSignature = 0;
        long diffBit;
        long value;
        float x;
        
        for (int i=0; i < SIGNATURE_CODELEN; i++) {
            diffBit = (aSignature&1l) ^ (bSignature&1l);
            if (diffBit == 0) {
                value = (aSignature&1l);
            }
            else {
                x = (float)Math.random();
                value = x < 0.5f? 0 : 1;
            }
            
            if (value == 1)
                cSignature = cSignature | (value<<i);
            
            aSignature = aSignature>>1;
            bSignature = bSignature>>1;
        }
        
        return cSignature;
    }
    
    // Estimate the signature of the vector sum of a and b.
    // Note that we don't know the true vectors. We only know
    // their signatures. If the bits match -- we copy the same bit into
    // the estimated sum signature.
    // If they don't (e.g. 01 or 10) then we flip a coin to determine
    // the outcome.    
    void vecsum(SBVec b) {
        this.signature = signatureSum(this.signature, b.signature);
    }
    
    public static void main(String[] args) {

        int d = 10;
        int count = 5;
        Random rand = new Random();
        
        init(d);
        
        List<SBVec> vecs = new ArrayList<>();
        
        for (int j = 0; j < count; j++) {        
            double[] v = new double[d];
            for (int i = 0; i < d; i++) {
                v[i] = rand.nextGaussian();
                SBVec sbvec = new SBVec(v);
                vecs.add(sbvec);
            }
        }
        
        for (int j = 0; j < count; j++) {        
            SBVec a = vecs.get(j);
            for (int k = j+1; k < count; k++) {        
                SBVec b = vecs.get(k);
                
                System.out.format("Sim (%d, %d) = %4f\n", j, k, a.computeCosSim(b));
                System.out.format("Sim (%d, %d) = %4f\n", j, k, a.computeCosSimFast(b));
            }
        }
    }
    
    public static void normalizeProjections() {
        for (int i=0; i < SIGNATURE_CODELEN; i++) {
            avgProjectionsPos[i] = avgProjectionsPos[i]/(double)numPos[i];
            avgProjectionsNeg[i] = avgProjectionsNeg[i]/(double)numNeg[i];
        }
        
    }
}
