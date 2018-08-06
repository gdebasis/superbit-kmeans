/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.research.drl.lshkmeans;

import info.debatty.java.lsh.SuperBit;
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
    static SuperBit sb;
    
    public static void init(int dim) {
        sb = new SuperBit(dim, N, L, SEED);
    }
    
    public SBVec(double[] x) {
        boolean[] bits = sb.signature(x);        
        encodeSignature(bits);
    }
    
    public SBVec(Vec vec) {
        boolean[] bits = sb.signature(vec.x);        
        encodeSignature(bits);
    }
    
    final void encodeSignature(boolean[] bits) {
        long mask;
        
        for (int i=0; i < bits.length; i++) {
            mask = (bits[i]? 1l : 0l)<<i;
            signature = signature | mask;
        }
    }
    
    float computeSim(SBVec that) {
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
                
                System.out.format("Sim (%d, %d) = %4f\n", j, k, a.computeSim(b));
            }
        }
    }
}
