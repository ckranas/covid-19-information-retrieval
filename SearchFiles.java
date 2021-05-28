import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchFiles {

    public static void main(String[] args) {
        searchFiles(args);
    }
    public static String searchFiles(String[] args) {

        if (args.length <= 0 ) {
            System.out.println("No search query given.");
            System.exit(0);
        }

        // Index Path
        String index = "index";

        String title = "";
        String authors = "";
        String institutions = "";
        String text = "";

        String json = "[";

        String queries = null;
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        int hitsPerPage = 10;

        // Search query  options
        for(int i = 0;i < args.length;i++) {
            if ("-title".equals(args[i])) {
                title = " title:"+args[i+1];
                i++;
            } else if ("-authors".equals(args[i])) {
                authors = "authors:"+args[i+1];
                i++;
            } else if ("-institutions".equals(args[i])) {
                institutions = "institutions:"+args[i+1];
                i++;
            } else if ("-text".equals(args[i])) {
                text = "text:"+args[i+1];
                i++;
            } else if ("-paging".equals(args[i])) {
                hitsPerPage = Integer.parseInt(args[i+1]);
                if (hitsPerPage <= 0) {
                    System.err.println("There must be at least 1 hit per page.");
                    System.exit(1);
                }
                i++;
            }
        }
        
        // If query has text, add OR;
        if ( !text.equals("")) {
            if (!title.equals("")) { title = " OR " + title; }
            if (!authors.equals("")) { authors = " OR " + authors; }
            if (!institutions.equals("")) { institutions = " OR " + institutions; }
        }

        try {

            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            // IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            // BufferedReader in = null;

            QueryParser parser = new QueryParser(text, analyzer);
            String specialQuery = text + title + authors + institutions; 
            System.out.println(specialQuery);
            try {
                Query query = parser.parse(specialQuery);
                ScoreDoc[] hits = searcher.search(query, 100).scoreDocs;
                for (int i = 0; i < hits.length; i++) {
                    Document hitDoc = searcher.doc(hits[i].doc);
                    json += "{" + 
                    " \"title\":\"" + hitDoc.get("title") + "\"," +
                    " \"authors\":\"" + hitDoc.get("authors") + "\"," +
                    " \"date\":\"" + hitDoc.get("date") + "\"," +
                    " \"institutions\":\"" + hitDoc.get("institutions") + "\"," +
                    " \"paper_id\":\"" + hitDoc.get("paper_id") + "\"," +
                    " \"text\":\"" + hitDoc.get("text") + "\"" +
                    "},";
                }
            } catch (ParseException e) {System.out.println("Error Parsing Docs.");}
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not open Directory");
            System.out.println("Caught " + e.getClass() +", " + e.getMessage());
        }

        json += "]";
        return(json);
    }
}