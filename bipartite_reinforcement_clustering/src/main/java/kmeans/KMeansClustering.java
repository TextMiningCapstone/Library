package kmeans;

import dataStructure.SparseVector;
import utils.IndexReader;

import java.util.*;

/**
 * Created by kanghuang on 9/10/15.
 */
public class KMeansClustering {
    HashMap<Integer, SparseVector> docIndex;
    ArrayList<SparseVector> centroids;
    ArrayList<HashSet<Integer>> groups;
    ArrayList<HashSet<Integer>> newGroups;
    int K;
    int maximumIter = 50;
    int dimension;
    int change; // stop criterion
    public KMeansClustering(String fileName, int k){
        IndexReader reader = new IndexReader();
        //reader.setVocabDir(Utils.testVocab);
        //reader.setDocDir(Utils.testDocVec);
        docIndex = reader.loadDocIndex();
        dimension = docIndex.get(0).getDimension();
        groups = new ArrayList<>();
        this.K = k;
    }

    public KMeansClustering(HashMap<Integer, SparseVector> index, int k){
        docIndex = index;
        dimension = docIndex.get(0).getDimension();
        groups = new ArrayList<>();
        this.K = k;
    }

    public void init(){

    }
    public double computeSimilarity(SparseVector inv1, SparseVector inv2){
        return cosine(inv1, inv2);
    }
//    public double computeSimilarity(SparseVector inv1, SparseVector inv2){
//        //System.out.println(euclidean(inv1, inv2));
//        return euclidean(inv1, inv2);
//    }
    public void initialize(){
        smartInitialize();
        //randomInitialize();
    }

    private void randomInitialize(){
        centroids = new ArrayList<>();
        for (int i = 0; i < K; i++){
            SparseVector vector = new SparseVector(dimension);
            for (int j = 0; j < dimension; j++){
                vector.add(j, Math.random() * 5000);
            }
            centroids.add(vector);
        }
    }

    private void smartInitialize(){
        int n = docIndex.size();
        centroids = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        HashSet<Integer> initCentroids = new HashSet<>();
        int initSeed = random.nextInt(n);
        double nearestPoint[] = new double[n];
        int sum = 0;
        for (int j = 0; j < n; j++){
            SparseVector centroidK = docIndex.get(j);
            nearestPoint[j] = Math.max(computeSimilarity(docIndex.get(initSeed), centroidK), nearestPoint[j]);
         //   sum += nearestPoint[j] * nearestPoint[j];
        }
        initCentroids.add(initSeed);
        for (int i = 1; i < K; i++){
            //double sum = 0;
            double minDist = Double.MAX_VALUE;
            int candidate = -1;
            for (int j = 0; j < n; j++){
                if (minDist > nearestPoint[j]){
                    minDist = nearestPoint[j];
                    candidate = j;
                }
            }
            initCentroids.add(candidate);
//            sum *= Math.random();
//            for (int j = 0; j < n; j++) {
//                double load =  nearestPoint[j] * nearestPoint[j];
//                if (sum < load) {
//                    candidate = j;
//                    initCentroids.add(j);
//                    break;
//                }
//                sum -= load;
//            }
            SparseVector keyCentroid = docIndex.get(candidate);
           // Double maxDist = Double.MIN_VALUE;
            //sum = 0;
            for (int j = 0; j < n; j++){
                SparseVector centroidK = docIndex.get(j);
                nearestPoint[j] = Math.max(computeSimilarity(keyCentroid, centroidK), nearestPoint[j]);
             //   sum += nearestPoint[j] * nearestPoint[j];
            }
        }
        for (Integer docID : initCentroids){
          //  System.out.print(docID + " ");
            centroids.add(docIndex.get(docID));
        }
        //System.out.println();

    }
    public double cosine(SparseVector inv1, SparseVector inv2){
        return inv1.dot(inv2)/inv1.norm()/inv2.norm();
    }
    public double euclidean(SparseVector inv1, SparseVector inv2){
        inv1.minus(inv2);
        return Math.sqrt(inv1.norm());
    }
    public void run(){
        int iter = 0;
        initialize();
        do{

            if (assignDataPoint())
                break;
            updateCentroids();
            iter++;
            //System.out.println(iter);
           // printProgress(iter);

        }while((iter < maximumIter));
        System.out.println(iter + "iterations finished");
    }

    public void printProgress(int num){
        System.out.println(num + " " + newGroups.size());
        System.out.println("Centroid Information:");
        for (int i = 0; i < centroids.size(); i++){
            System.out.println(centroids.get(i).toString());
        }
        System.out.println("Group Information:");
        for (int i = 0; i < K; i++){
            if (newGroups.get(i) != null) {
                System.out.print(i + ":" + newGroups.get(i).size() + "\n");
                HashSet<Integer> group = newGroups.get(i);
                for(Integer id : group){
                    System.out.print(id + " ");
                }
                System.out.println();
            }
        }
        System.out.println();

    }

    public boolean exactCheck(){
        int i = 0;
        if (groups.size() == 0 || groups.size() != newGroups.size()){
            return false;
        }
        for (i = 0; i < K; i++){
            int j = 0;
            try {
                if (!((groups.get(i) == null && newGroups.get(i) == null) || (groups.get(i).size() == newGroups.get(i).size()))) {
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

    public boolean approximateCheck(){
//        if (groups.size() == 0 || groups.size() != newGroups.size()){
//            return false;
//        }
//        HashMap<Integer, Integer> relation= new HashMap<>();
//        int groupID = 0;
//        for (HashSet<Integer> group : groups){
//            if (group == null) continue;
//            for (Integer id : group) {
//                relation.put(id, groupID);
//            }
//            groupID++;
//        }
//        int match = 0;
//        groupID = 0;
//        for (HashSet<Integer> group : newGroups) {
//            if (group == null) continue;
//            for (Integer id : group) {
//                if (relation.get(id).equals(groupID)){
//                    match++;
//                }
//            }
//            groupID++;
//        }
        double volatileRatio = change * 1.0 / docIndex.size();
        if (volatileRatio < .005)
            return true;
        else
            return false;
    }
    public void updateCentroids(){
        //centroids = new ArrayList<>();
        int centroidID = 0;
        for (HashSet group : groups){
            if (group != null) {
                Iterator<Integer> id = group.iterator();
                SparseVector newCentroid = docIndex.get(id.next()).copy();
                for (int i = 1; i < group.size(); i++) {
                    SparseVector doc = docIndex.get(id.next());
                    newCentroid.plus(doc);
                }
                newCentroid.scale(1. / group.size());
                centroids.set(centroidID, newCentroid);
            }
            centroidID++;
        }
    }

    public boolean assignDataPoint(){
        newGroups = new ArrayList<>();
        change = 0;
        HashMap<Integer, HashSet<Integer>> updateGroups = new HashMap<>();
        for (int  docID = 0;  docID < docIndex.size(); docID++) {
            SparseVector doc = docIndex.get(docID);
            int centroidID = 0;
            int closestCentroid = 0;
            double maxDist = Double.MIN_VALUE;
            for (SparseVector center : centroids) {
                double dist = computeSimilarity(center, doc);
                if (maxDist < dist) {
                    maxDist = dist;
                    closestCentroid = centroidID;
                }
                centroidID += 1;
            }
            HashSet<Integer> docs = null;
            if (updateGroups.get(closestCentroid) == null){
                docs = new HashSet();
                updateGroups.put(closestCentroid, docs);
            }
            else{
                docs = updateGroups.get(closestCentroid);
            }
            docs.add(docID);
            updateGroups.put(closestCentroid, docs);
            //newGroups.put(closestCentroid, docs);
            if (!doc.checkGroup(closestCentroid)){
                doc.setGroup(closestCentroid);
                change++;
            }
        }
        for (int i = 0; i < updateGroups.size(); i++){
            newGroups.add(updateGroups.get(i));
        }
        if (approximateCheck())
            return true;
        groups.clear();
        for (int i = 0; i < K; i++ ) {
            groups.add(updateGroups.get(i));
        }
        return false;
    }

    public ArrayList<HashSet<Integer>> getClusters(){
        return groups;
    }
    public static void main(String[] args){
        IndexReader reader = new IndexReader();
        HashMap<Integer, SparseVector> docIndex = reader.loadDocIndex();
        HashMap<Integer, SparseVector> wordIndex = reader.docIndex2InvList(docIndex);
        KMeansClustering km = new KMeansClustering(wordIndex, 10);
        km.run();
    }
}
