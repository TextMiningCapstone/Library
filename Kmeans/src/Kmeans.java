import org.apache.mahout.math.*;
import org.apache.mahout.math.Vector;


import java.io.IOException;
import java.util.*;


/**
 * Created by dongnanzhy on 1/28/16.
 */
public class Kmeans {
    private int num_cluster;
    private int num_iterations;
    private double diff;

    public Kmeans () {
        this.num_cluster = 67;
        this.num_iterations = 5000;
        this.diff = 0.000001;
    }

    public Kmeans (int num_cluster, int num_iterations, double diff) {
        this.num_cluster = num_cluster;
        this.num_iterations = num_iterations;
        this.diff = diff;
    }

    public void findCluster (boolean isDev, boolean isKmeansPlus) throws IOException {
        Matrix docs = matrixIO.read(isDev);
        Matrix initCluster = isKmeansPlus? init_plus(docs) : init(docs);
        Matrix cluster = calculate(docs, initCluster);
        Vector labels = assignLabel(docs, cluster);
        matrixIO.write(labels, isDev);
    }

    private Matrix calculate (Matrix docs, Matrix initCluster) {
        int iter = 0;
        Matrix curCluster = initCluster;
        Matrix prevCluster = new SparseMatrix(initCluster.rowSize(), initCluster.columnSize());
        prevCluster.assign(0);
        while (Math.abs(prevCluster.minus(curCluster).zSum()) > diff && iter < num_iterations) {
            HashMap<Integer, ArrayList<Integer>> hm = new HashMap<Integer, ArrayList<Integer>>();
            for (int k = 0; k < num_cluster; k++) {
                ArrayList<Integer> lst = new ArrayList<Integer>();
                hm.put(k, lst);
            }
            // find closest centroids and assign
            for (int i = 0; i < docs.rowSize(); i++) {
                double maxScore = Double.MIN_VALUE;
                int maxIndex = -1;
                for (int k = 0; k < num_cluster; k++) {
                    Vector v1 = docs.viewRow(i);
                    Vector v2 = curCluster.viewRow(k);
                    double score = v1.normalize().dot(v2.normalize());
                    if (score > maxScore) {
                        maxScore = score;
                        maxIndex = k;
                    }
                }
                ArrayList<Integer> lst = hm.get(maxIndex);
                lst.add(i);
                hm.put(maxIndex, lst);
            }
            // update previous cluster to the value of current cluster
            prevCluster.assign(curCluster);
            // update centroid of current cluster
            for (int k = 0; k < num_cluster; k++) {
                ArrayList<Integer> lst = hm.get(k);
                Vector v = new DenseVector(docs.columnSize());
                v.assign(0);
                for (int docid : lst) {
                    v = v.plus(docs.viewRow(docid));
                }
                v = v.divide(lst.size());
                curCluster.assignRow(k, v);
            }
            iter++;
        }
        return curCluster;
    }
    private Matrix init (Matrix docs) {
        int num_docs = docs.rowSize();
        ArrayList<Integer> lst = new ArrayList<Integer>();
        for (int i = 0; i < num_docs; i++) {
            lst.add(i);
        }
        Collections.shuffle(lst);
        Matrix initClusters = new SparseMatrix(num_cluster, docs.columnSize());
        initClusters.assign(0);
        for (int i = 0; i < num_cluster; i++) {
            initClusters.assignRow(i, docs.viewRow(lst.get(i)));
        }
        return initClusters;
    }
    private Matrix init_plus (Matrix docs) {
        // Choose one centroid uniformly at random from among the data points.
        int num_docs = docs.rowSize();
        Random rn = new Random();
        int firstDocid = rn.nextInt(num_docs);
        Matrix initClusters = new SparseMatrix(num_cluster, docs.columnSize());
        initClusters.assign(0);
        initClusters.assignRow(0, docs.viewRow(firstDocid));
        // choose following centroids
        Vector weights = new DenseVector(num_docs);
        weights.assign(0);
        for (int k = 1; k < num_cluster; k++) {
            for (int i = 0; i < docs.rowSize(); i++) {
                double maxScore = Double.MIN_VALUE;
                for (int j = 0; j < k; j++) {
                    Vector v1 = docs.viewRow(i);
                    Vector v2 = initClusters.viewRow(j);
                    double score = v1.normalize().dot(v2.normalize());
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
//                if (maxScore < 0.00001) {
//                    System.out.println("@@@@@");
//                }
//                double weight = (maxScore == 1) ? 0 : 1/maxScore;
                double weight = 1-maxScore;
                weights.set(i, weight);
            }
            int newCenter = weightRandom(weights);
            initClusters.assignRow(k, docs.viewRow(newCenter));
        }
        return initClusters;
    }
    private int weightRandom (Vector weights) {
        double totalWeight = weights.zSum();
        int randomIndex = -1;
        double random = Math.random() * totalWeight;
        for (int i = 0; i < weights.size(); i++)
        {
            random -= weights.get(i);
            if (random <= 0.0d)
            {
                randomIndex = i;
                break;
            }
        }
        return randomIndex;
    }
    private Vector assignLabel (Matrix docs, Matrix cluster) {
        Vector v = new DenseVector(docs.rowSize());
        for (int i = 0; i < docs.rowSize(); i++) {
            double maxScore = Double.MIN_VALUE;
            int maxIndex = -1;
            for (int k = 0; k < num_cluster; k++) {
                Vector v1 = docs.viewRow(i);
                Vector v2 = cluster.viewRow(k);
                double score = v1.normalize().dot(v2.normalize());
                if (score >= maxScore) {
                    maxScore = score;
                    maxIndex = k;
                }
            }
            v.set(i, maxIndex);
        }
        return v;
    }


    public static void main(String[] args) throws IOException {

        Kmeans rst =new Kmeans();
        rst.findCluster(true, true);
        //matrixIO.readGoldStand();
//        double[][] ddd = new double[3][5];
//        double[] d1 = {1,0,0,0,0};
//        double[] d2 = {0,1,0,0,0};
//        double[] d3 = {0,0,1,0,0};
//        ddd[0] = d1; ddd[1] = d2; ddd[2] = d3;
//        Matrix m = new SparseMatrix(3,5);
//        m.assign(ddd);
//        Matrix mm = rst.init(m);
//        //Matrix m = matrixIO.read(true);
//        Vector v = mm.viewRow(0); Vector v2 = mm.viewRow(1);
//        System.out.println(mm.rowSize()); System.out.println(v2.get(0));
    }
}
