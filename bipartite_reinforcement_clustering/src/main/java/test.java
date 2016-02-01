import org.apache.mahout.math.RandomAccessSparseVector;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.HashMap;

/**
 * Created by kanghuang on 9/9/15.
 */

public class test {
    public static void main(String[] args){
        RandomAccessSparseVector ras1 = new RandomAccessSparseVector(10);
        RandomAccessSparseVector ras2 = new RandomAccessSparseVector(10);
        HashMap <Integer, RandomAccessSparseVector> table = new HashMap<>();
        ras1.set(5, 3);
        ras1.set(1, -1);
        table.put(1,ras1);
        ras2.set(5, 3);
        ras2.set(1, 1);
        //ras2 = table.get(1);
        //ras2.set(5,2);
        System.out.println(ras1.equals(ras2));
        INDArray nd = Nd4j.create(new float[]{1, 2, 3, 4}, new int[]{2, 2});
    }
}
