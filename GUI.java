import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

import java.util.Objects;
import java.util.Vector;

public class GUI {
    private static ScoreDoc[] mhits = new ScoreDoc[100];
    private static int count = 0;
    private static Vector<String> history = new Vector<String>();

    public static void main(String args[]) {
        String index = "index";

        try {

            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            // IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            JFrame frame = new JFrame("Information Retrieval 2021 AM:2278");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200,800);

            // frame.setLayout(null);
            JPanel searchBar = new JPanel();

            JLabel textLabel = new JLabel("Search:");
            textLabel.setBounds(10, 10, 90, 20);
            JTextField text = new JTextField(20);
            text.setBounds(100, 10, 400, 20);

            JLabel include = new JLabel("Fill these for special search:");
            include.setBounds(10, 40, 500, 20);
            JLabel titleLabel = new JLabel("Title:");
            titleLabel.setBounds(10, 70, 90, 20);
            JTextField titleField = new JTextField(20);
            titleField.setBounds(100, 70, 200, 20);

            JLabel authorsLabel = new JLabel("Authors:");
            authorsLabel.setBounds(310, 70, 90, 20);
            JTextField authorsField = new JTextField(20);
            authorsField.setBounds(400, 70, 200, 20);
            
            JLabel institutionsLabel = new JLabel("Institutions:");
            institutionsLabel.setBounds(610, 70, 90, 20);
            JTextField institutionsField = new JTextField(20);
            institutionsField.setBounds(700, 70, 200, 20);

            JLabel[] hit = new JLabel[10];
            JLabel[] hitTitle = new JLabel[10];
            JLabel[] hitText = new JLabel[10];
            JLabel[] hitAuthors = new JLabel[10];
            JLabel[] hitInstitutions = new JLabel[10];
            JLabel[] hitDate = new JLabel[10];
            JLabel[] hitID = new JLabel[10];
            for (int i = 0; i < hit.length; i++) {
                hit[i] = new JLabel("");
                hitTitle[i] = new JLabel("");
                // hitText[i] = new JLabel("text of the paper");
                hitAuthors[i] = new JLabel("");
                hitInstitutions[i] = new JLabel("");
                hitDate[i] = new JLabel("");
                hitID[i] = new JLabel("");

                hit[i].setBounds(10, 100 + i * 60, 1170, 20);
                hitTitle[i].setBounds(10, 120 + i * 60, 1170, 20);
                hitAuthors[i].setBounds(10, 140 + i * 60, 390, 20);
                hitInstitutions[i].setBounds(410, 140 + i * 60, 290, 20);
                hitDate[i].setBounds(710, 140 + i * 60, 90, 20);
                hitID[i].setBounds(910, 140 + i * 60, 290, 20);

                frame.add(hit[i]);
                frame.add(hitTitle[i]);
                frame.add(hitAuthors[i]);
                frame.add(hitInstitutions[i]);
                frame.add(hitDate[i]);
                frame.add(hitID[i]);
            }

            JLabel hitShow = new JLabel("");
            JComboBox historyList = new JComboBox(history);
            historyList.setBounds(600,10,200,20);
            historyList.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox box = (JComboBox)e.getSource();
                    String historyQuery = (String)box.getSelectedItem();
                    text.setText(historyQuery);
                    titleField.setText("");
                    authorsField.setText("");
                    institutionsField.setText("");
                }
            });
            frame.add(historyList);

            JButton searchButton = new JButton("Search");
            searchButton.setBounds(1000, 10, 170, 40);
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean add = true;
                    for ( int i = 0; i < history.size(); i++) {
                        if ( text.getText().equals(history.get(i))) { add = false; break;}
                    }
                    if ( add ) { history.add(text.getText());}
                    mhits = searchFiles(text.getText(), titleField.getText(), authorsField.getText(), institutionsField.getText(), reader, searcher, analyzer);
                    
                    count = 0;
                    hitShow.setText("Showing " + count + "-" + count + 10 + " out of " + mhits.length + " papers.");
                    for (int i = 0; i < hit.length; i++) {
                        hit[i].setText("");
                        hitTitle[i].setText("");
                        hitAuthors[i].setText("");
                        hitInstitutions[i].setText("");
                        hitDate[i].setText("");
                        hitID[i].setText("");

                        if ( i<mhits.length && !Objects.isNull(mhits[i])) {
                            try {
                                Document hitDoc = searcher.doc(mhits[i].doc);
                                hit[i].setText("Paper " + i);
                                hitTitle[i].setText("Title: " + hitDoc.get("title"));
                                hitAuthors[i].setText("Authors: "+hitDoc.get("authors"));
                                hitInstitutions[i].setText("Institutions: " + hitDoc.get("institutions"));
                                hitDate[i].setText("Date: "+hitDoc.get("date"));
                                hitID[i].setText(hitDoc.get("paper_id"));
                            } catch (IOException ex) {}
                        }
                    }
                }
            });

            frame.add(textLabel);
            frame.add(text);
            frame.add(include);
            frame.add(titleLabel);
            frame.add(titleField);
            frame.add(authorsLabel);
            frame.add(authorsField);
            frame.add(institutionsLabel);
            frame.add(institutionsField);
            frame.add(searchButton);

            JPanel navigation = new JPanel();
            JButton nextButton = new JButton("Next");
            JButton prevButton = new JButton("Previous");
            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ( count + 10 < mhits.length ) {
                        count += 10;
                        hitShow.setText("Showing " + count + "-" + (count + 10) + " out of " + mhits.length + " papers.");
                        for (int i = 0; i < hit.length; i++) {
                            hit[i].setText("");
                            hitTitle[i].setText("");
                            hitAuthors[i].setText("");
                            hitInstitutions[i].setText("");
                            hitDate[i].setText("");
                            hitID[i].setText("");

                            if ( i + count < mhits.length && !Objects.isNull(mhits[i + count])) {
                                try {
                                    Document hitDoc = searcher.doc(mhits[i + count].doc);
                                    hit[i].setText("Paper " + i);
                                    hitTitle[i].setText("Title: " + hitDoc.get("title"));
                                    hitAuthors[i].setText("Authors: "+hitDoc.get("authors"));
                                    hitInstitutions[i].setText("Institutions: " + hitDoc.get("institutions"));
                                    hitDate[i].setText("Date: "+hitDoc.get("date"));
                                    hitID[i].setText(hitDoc.get("paper_id"));
                                } catch (IOException ex) {}
                            }
                        }
                    }
                }
            });
            
            prevButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ( count -10 >= 0 ) {
                        count -= 10;
                        hitShow.setText("Showing " + count + "-" + count + 10 + " out of " + mhits.length + " papers.");
                        for (int i = 0; i < hit.length; i++) {
                            hit[i].setText("");
                            hitTitle[i].setText("");
                            hitAuthors[i].setText("");
                            hitInstitutions[i].setText("");
                            hitDate[i].setText("");
                            hitID[i].setText("");

                            if ( i + count < mhits.length && !Objects.isNull(mhits[i + count])) {
                                try {
                                    Document hitDoc = searcher.doc(mhits[i + count].doc);
                                    hit[i].setText("Paper " + i);
                                    hitTitle[i].setText("Title: " + hitDoc.get("title"));
                                    hitAuthors[i].setText("Authors: "+hitDoc.get("authors"));
                                    hitInstitutions[i].setText("Institutions: " + hitDoc.get("institutions"));
                                    hitDate[i].setText("Date: "+hitDoc.get("date"));
                                    hitID[i].setText(hitDoc.get("paper_id"));
                                } catch (IOException ex) {}
                            }
                        }
                    }
                }
            });
            navigation.add(prevButton);
            navigation.add(nextButton);
            navigation.add(hitShow);

            JPanel display = new JPanel();

            frame.getContentPane().add(searchBar);
            frame.getContentPane().add(BorderLayout.SOUTH, navigation);
            frame.getContentPane().add(BorderLayout.CENTER, display);
            frame.setVisible(true);
        } catch (IOException e) {
            System.out.println("Could not open Directory");
            System.out.println("Caught " + e.getClass() +", " + e.getMessage());
        }
    }

    private static ScoreDoc[] searchFiles(String text, String title, String authors, String institutions, DirectoryReader reader, IndexSearcher searcher, Analyzer analyzer) {

        String queries = null;
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        int hitsPerPage = 10;

        
        if (!title.equals("")) { title = "title:" + title; }
        if (!authors.equals("")) { authors = "authors:" + authors; }
        if (!institutions.equals("")) { institutions = "institutions:" + institutions; }
    
        // If query has text, add OR;
        if ( !text.equals("")) {
            text = "text:" + text;
            if (!title.equals("")) { title = " OR " + title; }
            if (!authors.equals("")) { authors = " OR " + authors; }
            if (!institutions.equals("")) { institutions = " OR " + institutions; }
        }

        ScoreDoc[] hits = new ScoreDoc[100];

       
        QueryParser parser = new QueryParser(text, analyzer);
        String specialQuery = text + title + authors + institutions; 
        try {
            Query query = parser.parse(specialQuery);
            try {

                hits = searcher.search(query, 100).scoreDocs;
                // reader.close();
            } catch (IOException e) {System.out.println("Error Searching Docs.");}

        } catch (ParseException e) {System.out.println("Error Parsing Docs.");}
    

        return hits;
    } 
}