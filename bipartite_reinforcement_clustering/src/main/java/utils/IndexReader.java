package utils;

import com.google.common.io.LineReader;
import dataStructure.SparseVector;
import org.apache.mahout.math.Vector.Element;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by kanghuang on 9/10/15.
 */
public class IndexReader {
    int docDim = 0;
    String docVecDir = Utils.docVec;
    String vocabDir = Utils.vocab;
    public void setDocDir(String filePath){
        docVecDir = filePath;
    }

    public void setVocabDir(String filePath){
        vocabDir = filePath;
    }

    public HashMap<Integer, SparseVector> loadDocIndex(){
        HashMap<Integer, SparseVector> invertedIndex = new HashMap<>();
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(vocabDir));
            String line = null;
            int dimension = 0;
            while((line = lnr.readLine())!= null){
                dimension = lnr.getLineNumber();
            }
            //System.out.println(dimension);
            int docID = 0;
            LineReader lr = new LineReader(new FileReader(new File(docVecDir)));

            while((line = lr.readLine())!= null){
                String[] parts = line.split(" ");
                SparseVector docs = new SparseVector(dimension);
                for (String part : parts){
                    String[] index = part.split(":");
                    docs.add(Integer.parseInt(index[0]), Integer.parseInt(index[1]));
                }
                invertedIndex.put(docID++, docs);
            }
            docDim = docID;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return invertedIndex;
    }

    public void loadDF(){

    }

    public HashMap<Integer, SparseVector> docIndex2InvList(HashMap<Integer, SparseVector> docIndex){
        HashMap<Integer, SparseVector> wordIndex = new HashMap<>();
        for (int i = 0; i < docIndex.size(); i++){
            SparseVector vec = docIndex.get(i);
            Iterator<Element> iter = vec.getIterator();
            while(iter.hasNext()){
                Element elem =iter.next();
                SparseVector v = null;
                if (!wordIndex.containsKey(elem.index())){
                    v = new SparseVector(docDim);
                }
                else{
                    v = wordIndex.get(elem.index());
                }
                v.add(i, elem.get());
                wordIndex.put(elem.index(), v);
            }
        }
        return wordIndex;
    }

    public static void main(String[] args){
        HashMap<Integer, SparseVector> docIndex;
        HashMap<Integer, SparseVector> wordIndex;
        IndexReader reader = new IndexReader();
        reader.setDocDir(Utils.testDocVec);
        reader.setVocabDir(Utils.testVocab);
        docIndex = reader.loadDocIndex();
        wordIndex = reader.docIndex2InvList(docIndex);
        for (int i = 0; i < wordIndex.size(); i++){
            System.out.println(wordIndex.get(i).toString());
        }
    }
}
