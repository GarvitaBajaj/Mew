/*
 * Created by JFormDesigner on Tue Nov 21 13:06:47 IST 2017
 */

package interfaces;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.*;

/**
 * @author Garvita Bajaj
 */
public class WorkerList extends JFrame {
	public WorkerList() {
		initComponents();
		populateTable();
	}

	public void populateTable(){
		if(MainScreen.connect==null)
			MainScreen.showScreenMySQL();
		else{
			try {
				DefaultTableModel dtm = new DefaultTableModel(0, 3);
				// add header of the table
				String header[] = new String[] { "S. No.", "Worker ID" };
				dtm.setColumnIdentifiers(header);
				table1.setModel(dtm);
				Connection connect=MainScreen.connect;
				PreparedStatement fetch=connect.prepareStatement("SELECT DISTINCT(DEVICEID) FROM MEW.NODES WHERE PROVIDERMODE=1;");
				ResultSet rs=fetch.executeQuery();
				int i=1;
				while (rs.next()) {
					dtm.addRow(new Object[] {i, rs.getString(1)});
					i++;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}


	public static WorkerList wl=null;

	public static WorkerList getInstance(){
		if(wl==null){
			wl=new WorkerList();
		}
		wl.setVisible(true);
		return wl;
	}

	private void refreshWorkerList(ActionEvent e) {
		populateTable();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		button1 = new JButton();

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
				"[]" +
				"[]" +
				"[]"));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(table1);
		}
		contentPane.add(scrollPane1, "cell 0 0");

		//---- button1 ----
		button1.setText("Refresh");
		button1.addActionListener(e -> refreshWorkerList(e));
		contentPane.add(button1, "cell 0 1");
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JScrollPane scrollPane1;
	private JTable table1;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
