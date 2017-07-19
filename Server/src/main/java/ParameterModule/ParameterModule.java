/*
 * Created by JFormDesigner on Thu Jul 06 00:05:27 IST 2017
 */

package ParameterModule;

import java.awt.*;
import java.util.ResourceBundle;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import interfaces.MainScreen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.miginfocom.swing.*;

/**
 * @author Garvita Bajaj
 */
public class ParameterModule extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ParameterModule() {
		initComponents();
	}

	public static ParameterModule paramAdd=null;

	private void listColumnNames(){
		try {
			DefaultTableModel dtm = new DefaultTableModel(0, 3);
			// add header of the table
			String header[] = new String[] { "Column Name", "Column Type" };
			dtm.setColumnIdentifiers(header);
			table1.setModel(dtm);
			Connection connect=MainScreen.connect;
			PreparedStatement fetch=connect.prepareStatement("SELECT `COLUMN_NAME`,`DATA_TYPE` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='"+MainScreen.mysqlDB+"' AND `TABLE_NAME`='nodes';");
			ResultSet rs=fetch.executeQuery();
			while (rs.next()) {
				dtm.addRow(new Object[] { rs.getString(1), rs.getString(2)});
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	private void fetchSQLColumns(ActionEvent e) {
		listColumnNames();
	}

	private void addParameter(ActionEvent e) {
		//create a new panel
		//this should open a new dialogbox with fields to enter
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				AddParamDialog panel = AddParamDialog.getInstance(paramAdd);
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
		// fetch the column name and columnType from the dialog box displayed
	}

	private void refreshList(ActionEvent e) {
		//call the fetch parameters function again -> might require revalidate() and paint()
		listColumnNames();
	}

	private void deleteParameter(ActionEvent e) {
		//open an input dialog to get the column name from the user
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				DeleteParamDialog panel = DeleteParamDialog.getInstance(paramAdd);
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		ResourceBundle bundle = ResourceBundle.getBundle("properties");
		existingColumns = new JButton();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		addNewParams = new JButton();
		refresh = new JButton();
		deleteParam = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout(
			"hidemode 3",
			// columns
			"[152,fill]" +
			"[145,fill]" +
			"[fill]",
			// rows
			"[]" +
			"[248]" +
			"[]"));

		//---- existingColumns ----
		existingColumns.setText(bundle.getString("AddParameters.existingColumns.text"));
		existingColumns.addActionListener(e -> fetchSQLColumns(e));
		contentPane.add(existingColumns, "cell 0 0 3 1");

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(table1);
		}
		contentPane.add(scrollPane1, "cell 0 1 3 1");

		//---- addNewParams ----
		addNewParams.setText(bundle.getString("AddParameters.addNewParams.text"));
		addNewParams.addActionListener(e -> addParameter(e));
		contentPane.add(addNewParams, "cell 0 2");

		//---- refresh ----
		refresh.setText(bundle.getString("AddParameters.refresh.text"));
		refresh.addActionListener(e -> refreshList(e));
		contentPane.add(refresh, "cell 1 2,align center center,grow 0 0");

		//---- deleteParam ----
		deleteParam.setText(bundle.getString("AddParameters.deleteParam.text"));
		deleteParam.addActionListener(e -> deleteParameter(e));
		contentPane.add(deleteParam, "cell 2 2");
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JButton existingColumns;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JButton addNewParams;
	private JButton refresh;
	private JButton deleteParam;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	public static ParameterModule getInstance() {
		if(paramAdd==null){
			paramAdd=new ParameterModule();
		}
		paramAdd.setVisible(true);
		return paramAdd;
	}
}
