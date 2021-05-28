import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;

public class IndexFiles {
  
  /** Index all text files under a directory. */
  public static void main(String[] args) {

    String indexPath = "index";

    File folder = new File("filtered");
    File[] fileList = folder.listFiles();

    BufferedReader reader;
    String paper_id, title, authors, date, institutions, text;

    
    try {
      System.out.println("Indexing to directory '" + indexPath + "'...");
      Analyzer analyzer = new StandardAnalyzer();
      Directory directory = FSDirectory.open(Paths.get(indexPath));

      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      IndexWriter writer = new IndexWriter(directory, config);

      for ( File file : fileList) {
        paper_id = "";
        title = "";
        authors = "";
        date = "";
        institutions = "";
        text = "";
        
        if ( file.isFile()) {
          // System.out.println(file.getName());
            try {
            reader = new BufferedReader(new FileReader(folder + "/" + file.getName()));
            String line = reader.readLine();
            paper_id = line;
            line = reader.readLine();
            title = line;
            line = reader.readLine();
            authors = line;
            line = reader.readLine();
            date = line;
            line = reader.readLine();
            institutions = line;
            
            line = reader.readLine();
            while(line != null) {
              text += line;
              line = reader.readLine();
            }
            text += "\n" + paper_id +"\n" + title + "\n" + authors + "\n" + date + "\n" + institutions; 
            reader.close();

            Document doc = new Document();
            doc.add(new Field("paper_id", paper_id, TextField.TYPE_STORED));
            doc.add(new Field("title", title, TextField.TYPE_STORED));
            doc.add(new Field("authors", authors, TextField.TYPE_STORED));
            doc.add(new Field("date", date, TextField.TYPE_STORED));
            doc.add(new Field("institutions", institutions, TextField.TYPE_STORED));
            doc.add(new Field("text", text, TextField.TYPE_STORED));
            
            writer.addDocument(doc);
            // doc.close();  // throws error, cannot find symbol
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      }
      writer.close();
    } catch (IOException e) {
      System.out.println("Could not open Directory");
      System.out.println("Caught " + e.getClass() +", " + e.getMessage());
    }
  
    System.exit(0);

  }
}
