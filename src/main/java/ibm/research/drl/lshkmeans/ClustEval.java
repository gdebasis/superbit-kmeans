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
import java.io.*;
import java.util.*;

class RefLabelFreq implements Comparable<RefLabelFreq> {

    String refLabel;
    int freq;

    public RefLabelFreq(String refLabel) {
        this.refLabel = refLabel;
        freq = 0;

    }

    @Override
    public int compareTo(RefLabelFreq o) {
        return -1 * Integer.compare(freq, o.freq); // descending
    }
}

class RefLabelFreqs {

    String predictedLabel;
    HashMap<String, RefLabelFreq> refLabels;
    List<RefLabelFreq> sortedList;

    public RefLabelFreqs(String predictedLabel) {
        refLabels = new HashMap<>();
        this.predictedLabel = predictedLabel;
    }

    void add(String refClassLabel) {
        RefLabelFreq rlf = refLabels.get(refClassLabel);
        if (rlf == null) {
            rlf = new RefLabelFreq(refClassLabel);
        }
        rlf.freq++;
        refLabels.put(refClassLabel, rlf);
    }

}

class ClustEval {

    public ClustEval(String propFileName) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.load(new FileReader(propFileName));
    }

    static void readLines(String fileName, List<String> classLabels, List<String> refLabels) throws Exception {
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);

        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            classLabels.add(tokens[0]);
            refLabels.add(tokens[1]);
        }

        br.close();
        fr.close();
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("usage: ClustEval <file each line containing <clustering o/p> <tab> <ref o/p>>");
            return;
        }

        List<String> classLabels = new ArrayList<>(), refLabels = new ArrayList<>();
        readLines(args[0], classLabels, refLabels);

        int numInstances = classLabels.size();
        assert (numInstances == refLabels.size());
        
        long tp = 0, fp = 0, fn = 0, tn = 0;

        for (int i = 0; i < numInstances - 1; i++) {
            String l_i = classLabels.get(i);
            String r_i = refLabels.get(i);

            for (int j = i + 1; j < numInstances; j++) {
                String l_j = classLabels.get(j);
                String r_j = refLabels.get(j);

                boolean labelsAgree = l_i.equals(l_j);  
                boolean refClassesAgree = r_i.equals(r_j);

                if (labelsAgree && refClassesAgree) {
                    tp++;
                }
                else if (labelsAgree && !refClassesAgree) {
                    fp++;
                }
                else if (!labelsAgree && refClassesAgree) {
                    fn++;
                }
                else { 
                    tn++;
                }
            }
        }

        float acc = (tp + tn) / (float) (tp + fp + tn + fn);
        float prec = (tp) / (float) (tp + fp);
        float recall = (tp) / (float) (tp + fn);
        float fscore = 2 * prec * recall / (float) (prec + recall);
        float jac = tp / (float) (fp + fn + tp);

        System.out.println("Jaccard\tF-score\tRI\tRecall\tPrecision");
        System.out.println(jac + "\t" + fscore + "\t" + acc + "\t" + recall + "\t" + prec);        
    }
}

