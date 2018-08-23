/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.research.drl.lshkmeans;

/**
 *
 * @author dganguly
 */
public class SBVecProjStats extends SBVec {
    long avgProjSignature;

    public SBVecProjStats(Vec v) {
        super(v);
    }
    
    public SBVecProjStats(long signature) {
        super(signature);
    }
    
    public double[] getProjections(Vec v) {
        double[] projections = sb.projectedValues(v.x);
        
        // update the running average
        for (int i=0; i < avgProjectionsPos.length; i++) {
            
            if (projections[i] >= 0) {
                avgProjectionsPos[i] += projections[i];
                numPos[i]++;
            }
            else {
                avgProjectionsNeg[i] += projections[i];
                numNeg[i]++;
            }
        }
        
        return projections;
    }

    public void encodeProjections(Vec projV) {
        long mask = 0;
        
        for (int i=0; i < SIGNATURE_CODELEN; i++) {
            
            if (projV.x[i] >= 0) {
                mask = (projV.x[i] >= avgProjectionsPos[i]? 1l : 0l)<<i;
            }
            else {  // proj value < 0
                mask = (projV.x[i] <= avgProjectionsNeg[i]? 1l : 0l)<<i;
            }
            if (mask != 0)
                avgProjSignature = avgProjSignature | mask;
        }
    }
    
    long getProbSetBitProj(int i, long fx, long fy, long gx, long gy) {
        float p;
        float mu_p = (float)avgProjectionsPos[i];
        float mu_n = (float)Math.abs(avgProjectionsNeg[i]);
        boolean mu_p_higher = mu_p > mu_n;
        long tmp;
        int desiredValue = 1;
        
        // rename variables to handle the complementary cases with the same workflow
        // swap gx with gy
        // reverse the function of x and y. if x-y > 0 then y-x < 0
        // so reverse the role of the o/p bit from 1 to 0 => prob. of set
        // becomes prob. of reset
        if (fx==0 && fy==1) { 
            tmp = gy;
            gy = gx;
            gx = tmp;
            desiredValue = 0;
        }
        
        if (gx==1 && gy==0) {
            p = mu_p_higher? 1 : (mu_n - mu_p)/2*mu_n;
        }
        else if (gx==0 && gy==1) {
            p = mu_p_higher? (mu_p - mu_n)/2*mu_p: 0;
        }
        else if (gx==0 && gy==0) {
            p = mu_p_higher? (2*mu_p - mu_n)/2*mu_p : mu_p/2*mu_n;
        }
        else { // 1 1
            p = 0.5f;
        }
        
        if (p==1)
            return 1;
        if (p==0)
            return 0;
        
        float x = (float)Math.random();
        long bValue = x < p? desiredValue : 1-desiredValue;
        
        return bValue;
    }
    
    /* Estimate the signature based on the signs (0/1) of average projection values */
    @Override
    void vecsum(SBVec b) {
        
        // Estimate the avgproj-signatures
        SBVecProjStats that = (SBVecProjStats)b;
        
        long aSignature = this.signature;
        long bSignature = that.signature;
        long aAvgProjSignature = this.avgProjSignature;
        long bAvgProjSignature = that.avgProjSignature;
        
        long cSignature = 0;
        long aMaskedBit, bMaskedBit, aMaskedBitAvgProj, bMaskedBitAvgProj;
        
        long sameBit, bValue = 0;
        
        for (int i=0; i < SIGNATURE_CODELEN; i++) {
            aMaskedBit = aSignature&1l;
            bMaskedBit = bSignature&1l;
            aMaskedBitAvgProj = aAvgProjSignature&1l;
            bMaskedBitAvgProj = bAvgProjSignature&1l;
            
            sameBit = aMaskedBit ^ bMaskedBit;            
            if (sameBit == 0) {   // signature bits match
                bValue = aMaskedBit;
            }
            else {  // sign bits don't match, i.e. one projected value is +ve the other is -ve
                // check the avg projection bit
                bValue = getProbSetBitProj(i, aMaskedBit, bMaskedBit, aMaskedBitAvgProj, bMaskedBitAvgProj);
            }
            
            if (bValue == 1)
                cSignature = cSignature | (bValue<<i);
            
            aSignature = aSignature>>1;
            bSignature = bSignature>>1;
        }
        
        this.signature = cSignature;
        this.avgProjSignature = projSignatureSum(aSignature, bSignature, aAvgProjSignature, bAvgProjSignature);
    }
    
    long projSignatureSum(long fx, long fy, long gx, long gy) {
        long cSignature = 0;
        long value;
        float x;
        long fxi, fyi, gxi, gyi;
        long f_xor_y;
        
        for (int i=0; i < SIGNATURE_CODELEN; i++) {
            fxi = fx & 1l;
            fyi = fy & 1l;
            gxi = gx & 1l;
            gyi = gy & 1l;
            
            f_xor_y = fxi ^ fyi;
            if (f_xor_y == 0 && gxi == 1 && gyi == 1) {  // both of same sign and mod value higher than average
                value = 1; 
            }
            else {
                x = (float)Math.random();
                value = x < 0.5f? 0 : 1;
            }
            
            if (value == 1)
                cSignature = cSignature | (value<<i);
            
            fx = fx>>1;
            fy = fy>>1;
            gx = gx>>1;
            gy = gy>>1;
        }
        
        return cSignature;
    }
}
