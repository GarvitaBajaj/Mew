/*
 * Created by JFormDesigner on Thu Jul 06 21:43:40 IST 2017
 */

package algos;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import info.clearthought.layout.*;
import static java.nio.file.StandardCopyOption.*;
/**
 * @author Garvita Bajaj
 */
public class UploadAlgoFile extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public UploadAlgoFile() {
		initComponents();
	}

	public static UploadAlgoFile uploadAlgo;

	public static UploadAlgoFile getInstance(){
		if(uploadAlgo==null){
			uploadAlgo=new UploadAlgoFile();
		}
		uploadAlgo.setVisible(true);
		return uploadAlgo;
	}
	private void browseForAlgo(ActionEvent e) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Files","java");
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(filter);
		fc.showOpenDialog(this);
		try {
			textField1.setText(fc.getSelectedFile().getAbsolutePath());
		}catch(Exception e1){
			JOptionPane.showMessageDialog(getParent(), e1.getLocalizedMessage());
		}
	}

	private void uploadFile(ActionEvent e) {
		// read the file and save it to the algos package
		Pattern p = Pattern.compile("[^a-zA-Z0-9]");
		Matcher m = p.matcher(textField2.getText());
		if(textField1.getText().equals("")||textField2.getText().equals("")){
			JOptionPane.showMessageDialog(uploadAlgo, "Please select a file and provide a label");	
		}
		else if(m.find()==true){
			//check that the label name does not contain alphanumeric characters
			JOptionPane.showMessageDialog(uploadAlgo, "Label can not contain special characters (only alphanumeric characters allowed)");	
		}
		else{
			String path = Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\main\\java\\algos\\";
			File source=new File(textField1.getText());
			String fileName="";
			try {
				fileName=new java.io.File(textField1.getText()).getName();
				Files.copy(source.toPath(),new java.io.File(path+fileName).toPath(),REPLACE_EXISTING);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			//run the jar command line to add this file to the algos package
			//use this label and filename to overwrite AlgoFactory.java
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path+"AlgoFactory.java")));         
				FileWriter fw = new FileWriter(path+"tempAlgoFactory.java", true);
				BufferedWriter bw = new BufferedWriter(fw);
				String line;
				int lineNumber=0;
				while ((line = br.readLine()) != null) {
					lineNumber++;
					if(lineNumber==10){
						//insert the code to add another if condition
						String lineNew="if(\""+textField2.getText()+"\".equalsIgnoreCase(type)){ ";
						bw.write(lineNew);
						bw.newLine();
						bw.write("Class<?> t = null;");
						bw.newLine();
						bw.write("try{");
						bw.newLine();
						bw.write("t = Class.forName(\"algos."+	fileName.substring(0, fileName.length() - 5)+"\");");
						bw.newLine();
						bw.write("return (Algo) t.getConstructor(JSONObject.class).newInstance(query);");
						bw.newLine();
						bw.write("} catch (Exception e) {");
						bw.newLine();
						bw.write("e.printStackTrace();");
						bw.newLine();
						bw.write("}");
						bw.newLine();
						bw.write("return null;");
						bw.newLine();
						bw.write("}");
					}
					System.out.println(line);
					bw.write(line);
					bw.newLine();
				}
				br.close();
				bw.flush();
				bw.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try{
//				boolean success = (new File(path+"AlgoFactory.java")).delete();
//				if(success)System.out.println("File deleted succesfully");
//				boolean rename=new File(path+"tempAlgoFactory.java").renameTo(new File(path+"AlgoFactory.java"));
//				if(rename)System.out.println("File renamed successfully");
				dispose();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		ResourceBundle bundle = ResourceBundle.getBundle("properties");
		label1 = new JLabel();
		textField1 = new JTextField();
		button1 = new JButton();
		label2 = new JLabel();
		textField2 = new JTextField();
		button2 = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{228, 143},
			{39, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//---- label1 ----
		label1.setText(bundle.getString("UploadAlgoFile.label1.text"));
		contentPane.add(label1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		contentPane.add(textField1, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- button1 ----
		button1.setText(bundle.getString("UploadAlgoFile.button1.text"));
		button1.addActionListener(e -> browseForAlgo(e));
		contentPane.add(button1, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- label2 ----
		label2.setText(bundle.getString("UploadAlgoFile.label2.text"));
		contentPane.add(label2, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		contentPane.add(textField2, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- button2 ----
		button2.setText(bundle.getString("UploadAlgoFile.button2.text"));
		button2.addActionListener(e -> uploadFile(e));
		contentPane.add(button2, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JLabel label1;
	private JTextField textField1;
	private JButton button1;
	private JLabel label2;
	private JTextField textField2;
	private JButton button2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
