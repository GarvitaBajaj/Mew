/*
 * Created by JFormDesigner on Thu Jul 06 11:44:12 IST 2017
 */

package ParameterModule;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.*;
import info.clearthought.layout.*;
import interfaces.MainScreen;

/**
 * @author Garvita Bajaj
 */
public class AddParamDialog extends JDialog {
	public AddParamDialog(Frame owner) {
		super(owner);
		initComponents();
	}
	public String typeSelected;
	public static AddParamDialog addParamdialog;
	String[] columnTypes={};

	public AddParamDialog(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void typeAdded(ActionEvent e) {
		typeSelected=columnType.getSelectedItem().toString();
		switch(typeSelected){
		case("String"):
		case("Float"):
		case("BigInteger"):
		case("Decimal"):
		case("Double"):
		case("Character"):
			label3.setEnabled(true);
		length.setEnabled(true);
		break;
		case("Select"):
			typeSelected=null;
		default:
			label3.setEnabled(false);
			length.setEnabled(false);
			break;
		}
	}

	private void okButton(ActionEvent e) {
		int colLength = 0;
		String type = null;
		if(typeSelected==null || columnName.getText().equals("") || (length.isEnabled() && length.getText().equals(""))){
			JOptionPane.showMessageDialog(addParamdialog, "Please enter valid values");
			dispose();
			return;
		}
		Pattern p = Pattern.compile("[^a-zA-Z]");
		Matcher m = p.matcher(columnName.getText());
		boolean b = m.find();
		if (b){
			JOptionPane.showMessageDialog(addParamdialog, "Column names can only be alpha-numeric");
			dispose();
			return;
		}
		if(length.isEnabled()){
			try{
				colLength=Integer.parseInt(length.getText());
			}catch (Exception e1){
				JOptionPane.showMessageDialog(addParamdialog, "Please enter a valid integer for column length");
				dispose();
				return;
			}
		}
		switch(typeSelected){
		case("Integer"):
			type="int";
		break;
		case("BigInteger"):
			type="bigint"+"("+colLength+")";
		break;
		case("Decimal"):
			type="decimal"+"("+colLength+")";
		break;
		case("Double"):
			type="double"+"("+colLength+")";
		break;
		case("Float"):
			type="float"+"("+colLength+")";
		break;
		case("String"):
			type="varchar"+"("+colLength+")";
		break;
		case("Character"):
			type="char"+"("+colLength+")";
		break;
		case("Date"):
			type="date";
		break;
		case("Text"):
			type="text";
		break;
		case("Blob"):
			type="blob";
		break;
		case("Boolean"):
			type="tinyint(1)";
		break;
		case("Select"):
			JOptionPane.showMessageDialog(addParamdialog, "Please select a data type");
		dispose();
		break;
		}
		Object[] newType=new Object[]{columnName.getText(),type};
		try {
			Statement command=MainScreen.connect.createStatement();
			String statement="alter table nodes add "+newType[0]+" "+newType[1]+";";
			System.out.println(statement);
			command.executeUpdate(statement);
			command.close();
			JOptionPane.showMessageDialog(addParamdialog, "New column added");
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(addParamdialog, e1.getLocalizedMessage());
		}
		dispose();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		ResourceBundle bundle = ResourceBundle.getBundle("properties");
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		label1 = new JLabel();
		columnName = new JTextField();
		label2 = new JLabel();
		columnType = new JComboBox<>();
		label3 = new JLabel();
		length = new JTextField();
		buttonBar = new JPanel();
		okButton = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			// JFormDesigner evaluation mark
//			dialogPane.setBorder(new javax.swing.border.CompoundBorder(
//				new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
//					"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
//					javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
//					java.awt.Color.red), dialogPane.getBorder())); dialogPane.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new TableLayout(new double[][] {
					{143, 146},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)contentPanel.getLayout()).setHGap(5);
				((TableLayout)contentPanel.getLayout()).setVGap(5);

				//---- label1 ----
				label1.setText(bundle.getString("AddParamDialog.label1.text"));
				contentPanel.add(label1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				contentPanel.add(columnName, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label2 ----
				label2.setText(bundle.getString("AddParamDialog.label2.text"));
				contentPanel.add(label2, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- columnType ----
				columnType.setModel(new DefaultComboBoxModel<>(new String[] {
					"Select",
					"Boolean",
					"Character",
					"String",
					"Double",
					"Decimal",
					"Float",
					"Integer",
					"BigInteger",
					"Text",
					"Blob",
					"Date"
				}));
				columnType.addActionListener(e -> typeAdded(e));
				contentPanel.add(columnType, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label3 ----
				label3.setText(bundle.getString("AddParamDialog.label3.text"));
				label3.setEnabled(false);
				contentPanel.add(label3, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- length ----
				length.setEnabled(false);
				contentPanel.add(length, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.PREFERRED},
					{TableLayout.PREFERRED}}));
				((TableLayout)buttonBar.getLayout()).setHGap(5);
				((TableLayout)buttonBar.getLayout()).setVGap(5);

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(e -> okButton(e));
				buttonBar.add(okButton, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel label1;
	private JTextField columnName;
	private JLabel label2;
	private JComboBox<String> columnType;
	private JLabel label3;
	private JTextField length;
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public static AddParamDialog getInstance(Frame owner) {
		if(addParamdialog==null)
			addParamdialog=new AddParamDialog(owner);
		addParamdialog.setVisible(true);
		return addParamdialog;
	}
}
