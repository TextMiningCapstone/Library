import dataStructure.SparseVector;
import kmeans.KMeansClustering;
import org.apache.mahout.math.Vector.Element;
import utils.IndexReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by kanghuang on 9/11/15.
 */
public class BiClustering {
    HashMap<Integer, String> wordMapper;
    HashMap<Integer, SparseVector> docIndex;
    HashMap<Integer, SparseVector> wordIndex;
    HashMap<Integer, SparseVector> compressedDocIndex;    // compressed representation
    HashMap<Integer, SparseVector> compressedWordIndex;   // compressed representation
    ArrayList<HashSet<Integer>> docGroups;  //document clusters
    ArrayList<HashSet<Integer>> wordGroups; //word clusters
    static int K1 = 100, K2 = 200;
    public BiClustering (){
        IndexReader reader = new IndexReader();
        compressedDocIndex = docIndex = reader.loadDocIndex();
        wordIndex = reader.docIndex2InvList(docIndex);
    }

    public void setK(int k1, int k2){
        K1 = k1;
        K2 = k2;
    }

    public void updateWordIndex(){
        compressedWordIndex = new HashMap<>();
        int groupID = 0;
        for (HashSet<Integer> group : docGroups){
            if (group == null){
               groupID++;
                continue;
            }
            //docID is the id of document
            for (Integer docID : group){
                // the vector is compressed representation
                SparseVector docVec = compressedDocIndex.get(docID);
                Iterator<Element> wordIter = docVec.getIterator();
                while(wordIter.hasNext()){
                    Element word = wordIter.next();
                    int wordGroupID = word.index();
                    SparseVector compressedWordVec = null;
                    for (Integer wordID : wordGroups.get(wordGroupID)) {
                        double tf = wordIndex.get(wordID).getValue(docID);
                        if (!compressedWordIndex.containsKey(wordID)) {
                            compressedWordVec = new SparseVector(docGroups.size());
                            compressedWordIndex.put(wordID, compressedWordVec);
                        } else {
                            compressedWordVec = compressedWordIndex.get(wordID);
                        }
                        double weight = compressedWordVec.getValue(groupID);
                        compressedWordVec.add(groupID, weight + tf);
                        compressedWordIndex.put(wordID, compressedWordVec);
                    }
                }
            }
            groupID++;
        }
    }

    public void updateDocIndex(){
        compressedDocIndex = new HashMap<>();
        int groupID = 0;
        for (HashSet<Integer> group : wordGroups){
            if (group == null){
                groupID++;
                continue;
            }
            //wordID = termID
            for (Integer wordID : group){
                SparseVector wordVec = compressedWordIndex.get(wordID);
                Iterator<Element> docIter= wordVec.getIterator();
                while(docIter.hasNext()){
                    Element doc = docIter.next();
                    int docGroupID = doc.index();
                    SparseVector compressedDocVec = null;
                    //docID = groupDocID
                    for (Integer docID : docGroups.get(docGroupID)) {
                        double tf = docIndex.get(docID).getValue(wordID);
                        if (!compressedDocIndex.containsKey(docID)) {
                            compressedDocVec = new SparseVector(wordGroups.size());
                            compressedDocIndex.put(docID, compressedDocVec);
                        } else {
                            compressedDocVec = compressedDocIndex.get(docID);
                        }
                        double weight = compressedDocVec.getValue(groupID);
                        compressedDocVec.add(groupID, weight + tf);
                        compressedDocIndex.put(docID, compressedDocVec);
                    }
                }
            }
            groupID++;
        }
    }
    public void run(){
        //int K1 = 50, K2 = 200;
        int iter = 0;
        KMeansClustering km = new KMeansClustering(docIndex, K1);
        km.run();
        docGroups = km.getClusters();
        wordGroups = new ArrayList<>();
        for (int i = 0; i < wordIndex.size(); i++){
            HashSet<Integer> wc = new HashSet<>();
            wc.add(i);
            wordGroups.add(wc);
        }
        ArrayList<HashSet<Integer>> prevDocGroups;
        ArrayList<HashSet<Integer>> prevWordGroups;
        do{
            prevDocGroups = docGroups;
            prevWordGroups = wordGroups;
            updateWordIndex();
            km = new KMeansClustering(compressedWordIndex, K2);
            km.run();
            wordGroups = km.getClusters();
            updateDocIndex();
            km = new KMeansClustering(compressedDocIndex, K1);
            km.run();
            docGroups = km.getClusters();
            System.out.println(iter);
        }while(!check(prevDocGroups, docGroups) && !check(prevWordGroups, wordGroups) && iter++ < 20);
        try {
            writeToFile(docGroups, "document-cluster.txt");
            writeToFile(wordGroups, "word-cluster.txt");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("fail to write to file !!!");
        }
    }

    // check whether two groups are identical. In other word, it is the most strict stop criterion
    public boolean check(ArrayList<HashSet<Integer>> groups, ArrayList<HashSet<Integer>> newGroups){
        int i = 0;
        if (groups.size() == 0 || groups.size() != newGroups.size()){
            return false;
        }
        for (i = 0; i < groups.size(); i++){
            int j = 0;
            try {
                if (!((groups.get(i) == null && newGroups.get(i) == null) || (groups.get(i) != null && newGroups.get(i) != null && groups.get(i).size() == newGroups.get(i).size()))) {
                    return false;
                }
            }
            catch (Exception IndexOutOfBoundsException){
                System.out.println();
            }

            HashSet<Integer> group = groups.get(i);
            HashSet<Integer> newGroup = newGroups.get(i);

            if (group == null)
                continue;

            for (Integer val : newGroup){
                if (!group.contains(val))
                    return false;
            }
        }
        return true;
    }

    public void writeToFile(ArrayList<HashSet<Integer>> groups, String fileName) throws IOException {
        int clusterID = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
        for (HashSet<Integer> group : groups){
            if (group == null){
                continue;
            }
            for (Integer docID : group) {
                bw.write(docID + " " + (clusterID) + "\n");
            }
            clusterID++;
        }
        bw.close();
    }
    public static void main(String[] args){
        long startTime = System.currentTimeMillis();
        BiClustering bc = new BiClustering();
        bc.run();
        long endTime = System.currentTimeMillis();
        System.out.println("Running time:" + (endTime - startTime) * 1.0 / 1000 / 60);
    }
}
