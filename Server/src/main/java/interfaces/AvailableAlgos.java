/*
 * Created by JFormDesigner on Fri Nov 17 16:34:44 IST 2017
 */

package interfaces;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ParameterModule.ParameterModule;
import net.miginfocom.swing.*;

/**
 * @author Garvita Bajaj
 */
public class AvailableAlgos extends JFrame {
	
	public static AvailableAlgos availAlgos=null;
	
	public static AvailableAlgos getInstance() {
		if(availAlgos==null){
			availAlgos=new AvailableAlgos();
		}
		availAlgos.setVisible(true);
		return availAlgos;
	}
	
	public AvailableAlgos() {
		initComponents();
		try {
			DefaultTableModel dtm = new DefaultTableModel(0, 3);
			// add header of the table
			String header[] = new String[] { "Algorithms Available" };
			dtm.setColumnIdentifiers(header);
			table1.setModel(dtm);
			String path=Paths.get(".").toAbsolutePath().normalize().toString()+"\\src\\main\\java\\algos\\";
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();

			    for (int i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			        System.out.println(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length()-5));
			        dtm.addRow(new Object[] { listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length()-5)});
			      } 
			    }
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		scrollPane2 = new JScrollPane();
		table1 = new JTable();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout(
			"hidemode 3",
			// columns
			"[fill]" +
			"[fill]",
			// rows
			"[]" +
			"[]" +
			"[]"));

		//======== scrollPane2 ========
		{
			scrollPane2.setViewportView(table1);
		}
		contentPane.add(scrollPane2, "cell 0 0");
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JScrollPane scrollPane2;
	private JTable table1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
