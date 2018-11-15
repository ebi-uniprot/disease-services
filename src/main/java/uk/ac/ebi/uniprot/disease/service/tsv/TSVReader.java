package uk.ac.ebi.uniprot.disease.service.tsv;

import uk.ac.ebi.uniprot.disease.utils.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author sahmad
 */
public class TSVReader implements Closeable {
    private Scanner tsvReader;
    private String peekRecord;
    private boolean isHeader = true;


    public TSVReader(String fileName) throws FileNotFoundException {
        this.tsvReader = new Scanner(new File(fileName), StandardCharsets.UTF_8.name());
        this.peekRecord = null;
    }

    public boolean hasMoreRecord() {
        if(this.peekRecord != null){
            return true;
        }
        if(!this.tsvReader.hasNextLine()){
            return false;
        }

        String nextRecord = this.tsvReader.nextLine().trim();

        if(nextRecord.isEmpty() || this.isHeader){ // skip the header(first line) and empty line
            this.isHeader = false;
            return hasMoreRecord();
        }

        this.peekRecord = nextRecord;
        return true;
    }

    public List<String> getRecord(){
        if(hasMoreRecord()){
            StringTokenizer tokenizer = new StringTokenizer(this.peekRecord, Constants.TAB);
            List<String> tokens = getTokens(tokenizer);
            this.peekRecord = null;
            return tokens;
        } else {
            return new ArrayList<>();
        }
    }

    public void close() {
        this.tsvReader.close() ;
    }

    private List<String> getTokens(StringTokenizer tokenizer) {
        List<String> tokens = new ArrayList<>();
        while(tokenizer.hasMoreElements()){
            tokens.add(tokenizer.nextElement().toString());
        }

        return tokens;
    }
}