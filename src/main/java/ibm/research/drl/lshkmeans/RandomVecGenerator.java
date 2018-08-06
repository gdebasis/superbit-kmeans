/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.research.drl.lshkmeans;
import java.io.*;
import java.util.*;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.MixtureMultivariateNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author dganguly
 */
public class RandomVecGenerator {
    Properties prop;
    int numPoints;
    int numGaussians;
    int numDimensions;
    MixtureMultivariateNormalDistribution mixtureDist;
    
    final int SEED = 110781;
    
    public RandomVecGenerator(String propFile) throws Exception {
        prop = new Properties();
        prop.load(new FileReader(propFile));         
        
        numPoints = Integer.parseInt(prop.getProperty("syntheticdata.numsamples"));
        numGaussians = Integer.parseInt(prop.getProperty("syntheticdata.numgaussians"));
        numDimensions = Integer.parseInt(prop.getProperty("vec.numdimensions"));
        
        List<Pair<Double,MultivariateNormalDistribution>> components = new ArrayList<>();
        for (int i=0; i < numGaussians; i++) {
            components.add(new Pair(new Double(1/(double)numGaussians), genRandom(i)));
        }

        RandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(SEED);
        this.mixtureDist = new MixtureMultivariateNormalDistribution(rg, components);           
    }
    
    MultivariateNormalDistribution genRandom(int id) {
        double[] meanvec = initRandomMean();
        
        System.out.println("Generating random cov matrix...");
        
        double[][] cov = initRandomCovMatrix();
        MultivariateNormalDistribution dist = new MultivariateNormalDistribution(meanvec, cov);
        return dist;
    }

    double[][] initRandomDiagonalMatrix(int n) {
        double[][] d = new double[n][n];
        for (int i=0; i < n; i++) {
            while (d[i][i] == 0) d[i][i] = Math.random();
        }
        return d;
    }
    
    double[][] initRandomMatrix(int n) {
        double[][] cov = new double[n][n];
        for (int i=0; i < n; i++) {
            for (int j=0; j < n; j++) {
                double x = Math.random();
                cov[i][j] = x;
            }
        }
        return cov;
    }
    
    double[][] transpose(double[][] m, int n) {
        double[][] tran = new double[n][n];
        for (int i=0; i < n; i++) {
            for (int j=0; j < n; j++) {
                tran[j][i] = m[i][j];
            }
        }
        return tran;
    }
    
    double[][] initRandomCovMatrix() {
        double[][] Q = initRandomMatrix(numDimensions);
        double[][] Q_tran = transpose(Q, numDimensions);
        double[][] D = initRandomDiagonalMatrix(numDimensions);
        
        double[][] A = new double[numDimensions][numDimensions];
        double[][] B = new double[numDimensions][numDimensions];
        
        // Q^T x D x Q is always invertible!
        for (int i=0; i < numDimensions; i++) {
            for (int j=0; j < numDimensions; j++) {
                for (int k=0; k < numDimensions; k++) {
                    B[i][j] += Q_tran[i][k] * D[k][j]; 
                }
            }
        }
        for (int i=0; i < numDimensions; i++) {
            for (int j=0; j < numDimensions; j++) {
                for (int k=0; k < numDimensions; k++) {
                    A[i][j] += B[i][k] * Q[k][j]; 
                }
            }
        }
        
        return A;
    }
    
    double[] initRandomMean() {
        
        double[] means = new double[numDimensions];
        for (int i=0; i < numDimensions; i++) {
            double x = Math.random();
            means[i] = x;
        }
        return means;
    }

    Vec sample() {
        // choose a component at random
        int componentId = (int)(Math.random()*numGaussians);
        return new Vec(componentId, mixtureDist.getComponents().get(componentId).getSecond().sample());
    }

    public final String randomSamplesFileName() {
        String fileName = prop.getProperty("datafile");
        return fileName;
    }

    public void generateSamples() throws Exception {        
        FileWriter fw = new FileWriter(randomSamplesFileName());
        BufferedWriter bw = new BufferedWriter(fw);
        
        System.out.println("Generating random samples...");
        for (int i=0; i < numPoints; i++) {
            if (i % 1000 == 0)
                System.out.format("Generated %d samples...\n", i);
            
            bw.write(sample().toString());
            bw.newLine();
        }
        
        bw.close();
        fw.close();        
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[1];
            System.out.println("Usage: java RandomVecGenerator <prop-file>");
            args[0] = "init_synthetic.properties";
        }

        try {
            RandomVecGenerator rvgen = new RandomVecGenerator(args[0]);
            rvgen.generateSamples();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
