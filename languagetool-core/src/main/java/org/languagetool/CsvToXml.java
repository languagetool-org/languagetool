package org.languagetool;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CsvToXml {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Choose interface (To choose command line type 'cl', to choose UI type 'ui'): ");
    String screen = scanner.nextLine();

    if ("cl".equals(screen)) {
      System.out.print("Enter a file name: ");
      String filename = scanner.nextLine();
      System.out.print("Enter character to seperate csv file: ");
      String splitBy = scanner.next();
      csvToBitext(filename, splitBy, "");
    } else if ("ui".equals(screen)) {
      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);
      JFrame frame = new JFrame("User Interface");
      frame.setPreferredSize(new Dimension(400, 100));
      frame.setLayout(new FlowLayout());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JLabel text = new JLabel("Split words by:");
      JTextField split = new JTextField(5);
      JButton btnFile = new JButton("Select Csv File");
      JButton btnOk = new JButton("Ok");
      JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
      btnFile.addActionListener((ActionEvent ae) -> {
        jfc.setDialogTitle("Select csv file");
        jfc.setAcceptAllFileFilterUsed(false);
//                FileNameExtensionFilter filter = new FileNameExtensionFilter("csv");
//                jfc.addChoosableFileFilter(filter);

        int returnValue = jfc.showOpenDialog(null);
//                if (returnValue == JFileChooser.APPROVE_OPTION) {
//                    
//                }
      });
      btnOk.addActionListener((ActionEvent ae) -> {
        csvToBitext(jfc.getSelectedFile().getPath(), split.getText(), "");
      });
      frame.add(btnFile);
      frame.add(text);
      frame.add(split);
      frame.add(btnOk);
      frame.pack();
      frame.setVisible(true);
    }
  }

  public static void csvToBitext(String csvFile, String cvsSplitBy, String line) {

    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

      Document doc = docBuilder.newDocument();
      Element rootElement = doc.createElement("example");
      doc.appendChild(rootElement);

      Element example00 = doc.createElement("example00");
      rootElement.appendChild(example00);

      Element example01 = doc.createElement("example01");
      rootElement.appendChild(example01);

      example00.setAttribute("lang", "en");

      example01.setAttribute("lang", "ru");

      while ((line = br.readLine()) != null) {

        String[] x = line.split(cvsSplitBy);

        Element example000 = doc.createElement("example000");
        example000.appendChild(doc.createTextNode(x[4]));
        example00.appendChild(example000);

        Element example010 = doc.createElement("example010");
        example010.appendChild(doc.createTextNode(x[5]));
        example01.appendChild(example010);
      }

      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(new File("file.xml"));

      // Output to console for testing
//            StreamResult result = new StreamResult(System.out);
      transformer.transform(source, result);

      System.out.println("File saved!");

    } catch (IOException | ParserConfigurationException ex) {
    } catch (TransformerConfigurationException ex) {
    } catch (TransformerException ex) {
    }
  }
}
