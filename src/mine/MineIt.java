/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mine;

import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;
import rita.wordnet.*;
import java.io.*;

/**
 *
 * @author wella
 */
public class MineIt {
    //private final String[] unnecessaryKeywords = {"the", "a", "or", "and", "nor", "an"};
    private ArrayList<String> synonymsOfKeyword = new ArrayList<String>(5);
    private ArrayList<String> extractedTexts = new ArrayList<String>(10);
    private int ctr = 0;
    private String keyword;
    private ContentReader reader;
    private LinkedList<String> listResult;

    public MineIt() {
        keyword = "";
        reader = new ContentReader();
        listResult = new LinkedList<String>();
    }

    public ArrayList<String> getExtractedTexts() {
        return extractedTexts;
    }

    public void setExtractedTexts(ArrayList<String> extractedTexts) {
        this.extractedTexts = extractedTexts;
    }

    public ArrayList<String> getSynonymsOfKeyword() {
        return synonymsOfKeyword;
    }

    public void setSynonymsOfKeyword(ArrayList<String> synonymsOfKeyword) {
        this.synonymsOfKeyword = synonymsOfKeyword;
    }

    public void setSynonymsOfKeyword(String[] synonymsOfKeyword) {
        this.synonymsOfKeyword.addAll(Arrays.asList(synonymsOfKeyword));
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LinkedList<String> getListResult() {
        return listResult;
    }

    public void setListResult(LinkedList<String> listResult) {
        this.listResult = listResult;
    }

    public ContentReader getReader() {
        return reader;
    }

    public void setReader(ContentReader reader) {
        this.reader = reader;
    }

    private String[] getRelatedWords(String keyword) {
        RiWordnet wordnet = new RiWordnet(null);
        String[] synonyms;
        if(wordnet.exists(keyword)) {
            synonyms = wordnet.getAllSynonyms(keyword, wordnet.getBestPos(keyword)); //magbutang lang siguro ug para option sa user noh like max search or normal search. ang mas search kay allsynonyms ang normal search kay allsynsets. :D
        }
        else{
            synonyms = new String[1];
            synonyms[0] = keyword;
        }
        return synonyms;
    }

    //should write the contents of the documents in a textfile and feed to markov model
    public void writeToFile() {
        FileWriter fw;
        try {
            fw = new FileWriter(new java.io.File("src/ExtractedTexts.txt"));
            for(String s : extractedTexts) {
               fw.write(s);
               String marker = "\n~***~\n";
               fw.append(marker);
            }
            fw.close();
        }
        catch(IOException e) { e.printStackTrace(); }
        
    }

    //extract the contents to be written
    private void extract(String path) {
        String content;
        if(path.endsWith(".doc") || path.endsWith(".docx")) {
            content = reader.readDocFile(path);
            listResult.add(path);
            extractedTexts.add(content);
        }
        else if(path.endsWith(".xls") || path.endsWith(".xlsx")) {
            content = reader.readExcelFile(path);
            listResult.add(path);
            extractedTexts.add(content);
        }
        else if(path.endsWith(".ppt") || path.endsWith(".pptx")) {
            content = reader.readPPTFile(path);
            listResult.add(path);
            extractedTexts.add(content);
        }
        else if(path.endsWith(".pdf") && !path.startsWith("http")) {
            content = reader.readPDFFile(path);
            listResult.add(path);
            extractedTexts.add(content);
        }
        else if(path.startsWith("http")) {
            content = reader.readWebText(path);
            listResult.add(path);
            extractedTexts.add(content);
        }
    }

    //extracting the path from the database, exract and writetoFile are called here
    public void extractContents() {
        Connection con = null;
        try {
          Class.forName("com.mysql.jdbc.Driver");
          con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mineitup","root","1234");
          if(!con.isClosed()) {
              System.out.println("Successfully connected to MySQL server using TCP/IP...");
              String query = "SELECT * FROM sourcePath";
              Statement st = con.createStatement(); //creates the java statement
              ResultSet rs = st.executeQuery(query); // execute the query, and get a java resultset
              while (rs.next()) // iterate through the java resultset
              {
                //int id = rs.getInt("id");
                String path = rs.getString("path");
                extract(path);
              }
              st.close();
              con.close();
            }
        }
        catch (SQLException s){
            System.out.println("SQL statement is not executed!");
        }
        catch (Exception e){
          e.printStackTrace();
        }
        writeToFile();
    }

    public void searchKeywordOccurence(String keyword) {
        this.extractedTexts.clear();
        this.listResult.clear();
        Connection con = null;
        try {
          Class.forName("com.mysql.jdbc.Driver");
          con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mineitup","root","1234");
          if(!con.isClosed()) {
              System.out.println("Successfully connected to MySQL server using TCP/IP...");
              String query = "SELECT * FROM sourcePath";
              Statement st = con.createStatement(); //creates the java statement
              ResultSet rs = st.executeQuery(query); // execute the query, and get a java resultset
              while (rs.next()) // iterate through the java resultset
              {
                //int id = rs.getInt("id");
                String path = rs.getString("path");
                processKeywordInFile(keyword, path); //if mo-return siya ug true, store the id somewhere for data mining algo to be used
              }
              st.close();
              con.close();
            }
        }
        catch (SQLException s){
            System.out.println("SQL statement is not executed!");
        }
        catch (Exception e){
          e.printStackTrace();
        }
        displayListResult(listResult);
    }
    private boolean contentContainsKeyword(String content, ArrayList<String> keywords) {
        boolean ok = false;
        for(String s: keywords) {
            if(content.toLowerCase().contains(s)) {
                ok = true;
                break;
            }
        }
        return ok;
    }
    //returns false if the file given by path does not contain keyword, true otherwise
    private boolean processKeywordInFile(String keyword, String path) {
        String content;
        boolean ok = false;
        this.setSynonymsOfKeyword(getRelatedWords(keyword));
        this.synonymsOfKeyword.add(keyword);
        if(path.endsWith(".doc") || path.endsWith(".docx")) {
            content = reader.readDocFile(path);
            if(contentContainsKeyword(content, synonymsOfKeyword)) {
                listResult.add(path);
                extractedTexts.add(content);
                ok = true;
            }
        }
        else if(path.endsWith(".xls") || path.endsWith(".xlsx")) {
            content = reader.readExcelFile(path);
            if(contentContainsKeyword(content, synonymsOfKeyword)) {
                listResult.add(path);
                extractedTexts.add(content);
                ok = true;
            }
        }
        else if(path.endsWith(".ppt") || path.endsWith(".pptx")) {
            content = reader.readPPTFile(path);
            if(contentContainsKeyword(content, synonymsOfKeyword)) {
                listResult.add(path);
                extractedTexts.add(content);
                ok = true;
            }
        }
        else if(path.endsWith(".pdf") && !path.startsWith("http")) {
            content = reader.readPDFFile(path);
            if(contentContainsKeyword(content, synonymsOfKeyword)) {
                listResult.add(path);
                extractedTexts.add(content);
                ok = true;
            }
        }
        else if(path.startsWith("http")) {
            content = reader.readWebText(path);
            if(contentContainsKeyword(content, synonymsOfKeyword)) {
                listResult.add(path);
                extractedTexts.add(content);
                ok = true;
            }
        }
        return ok;
    }
    //displays all the elements in the listResult
    public void displayListResult(LinkedList<String> listResult) {
        String list = "";
        System.out.println("Search Results");
        ListIterator<String> iterator = listResult.listIterator();
        while(iterator.hasNext()) {
            list += iterator.next() + "\n";
            System.out.println(list);
        }
        String res = listResult.size() + " Search Result(s)";
        javax.swing.JOptionPane.showMessageDialog(null, list, res, javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}
