package dataStructure;

import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

import java.util.Iterator;

/**
 * Created by kanghuang on 9/10/15.
 */
public class SparseVector {
    //TreeMap<Integer, Integer> vector;
    Vector vector;
    int groupID;
    public SparseVector(int dimensions){
        vector = new RandomAccessSparseVector(dimensions);
    }
    public SparseVector(Vector vector){
        this.vector = new RandomAccessSparseVector(vector);
    }
    public void add(int index, double tf){
        vector.set(index, tf);
    }

    public double getValue(int index){
        return vector.getQuick(index);
    }
    public double dot(SparseVector other){
        return this.vector.dot(other.vector);
    }

    public double norm(){
        return vector.norm(2);
    }

    public void plus(SparseVector other){
        vector = vector.plus(other.vector);
    }

    public void minus(SparseVector other){ vector = vector.minus(other.vector);}

    public void scale(double scaling){
        vector = vector.divide(1/scaling);
    }

    public int getDimension(){
        return vector.size();
    }

    public String toString(){
        StringBuilder info = new StringBuilder();
        for (Element em : vector.all()){
            info.append(em.index() + ":" + em.get() + "\t");
        }
        return info.toString();
    }

    public boolean has(int index){
        return vector.get(index) != 0.0;
    }

    public Iterator getIterator(){
        return vector.nonZeroes().iterator();
    }

    public SparseVector copy(){
        return new SparseVector(vector.clone());
    }

    public boolean checkGroup(int groupID){
        return this.groupID == groupID;
    }

    public void setGroup(int groupID){
        this.groupID = groupID;
    }

}
