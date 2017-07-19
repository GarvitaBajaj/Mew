/*
 * Created by JFormDesigner on Thu Jul 06 20:13:25 IST 2017
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
public class DeleteParamDialog extends JDialog {
	public DeleteParamDialog(Frame owner) {
		super(owner);
		initComponents();
	}

	public DeleteParamDialog(Dialog owner) {
		super(owner);
		initComponents();
	}
	
	public static DeleteParamDialog deleteParamdialog;


	private void deleteColumn(ActionEvent e) {
		Set<String> fixed = new HashSet<String>(Arrays.asList("DeviceID","battery","linkspeed","AccRunning","AccPower","GPSRunning","GPSPower","GyrRunning","GyrPower","SensorsAvailable","servicingTask","old_lat","old_lon","new_lat","new_lon","providerMode","MicPower","WiFiPower"));
		Pattern p = Pattern.compile("[^a-zA-Z0-9_]");
		Matcher m = p.matcher(textField1.getText());
		boolean b = m.find();
		if(textField1.getText().equals("") || b){
			JOptionPane.showMessageDialog(deleteParamdialog, "Please enter valid values");
			dispose();
			return;
		}
		if(fixed.contains(textField1.getText())){
			JOptionPane.showMessageDialog(deleteParamdialog, "Can't delete a predefined column");
			dispose();
			return;
		}
		try {
			Statement command=MainScreen.connect.createStatement();
			System.out.println("ALTER TABLE nodes DROP COLUMN "+textField1.getText()+";");
			String statement="ALTER TABLE nodes DROP COLUMN "+textField1.getText()+";";
			System.out.println(statement);
			command.executeUpdate(statement);
			command.close();
			JOptionPane.showMessageDialog(deleteParamdialog, "Column deleted");
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(deleteParamdialog, e1.getLocalizedMessage());
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
		textField1 = new JTextField();
		buttonBar = new JPanel();
		okButton = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			// JFormDesigner evaluation mark
			dialogPane.setBorder(new javax.swing.border.CompoundBorder(
				new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
					"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
					javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
					java.awt.Color.red), dialogPane.getBorder())); dialogPane.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, 170},
					{24, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)contentPanel.getLayout()).setHGap(5);
				((TableLayout)contentPanel.getLayout()).setVGap(5);

				//---- label1 ----
				label1.setText(bundle.getString("DeleteParamDialog.label1.text"));
				label1.setHorizontalAlignment(SwingConstants.LEFT);
				contentPanel.add(label1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.CENTER, TableLayoutConstraints.CENTER));
				contentPanel.add(textField1, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
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
				okButton.addActionListener(e -> deleteColumn(e));
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
	private JTextField textField1;
	private JPanel buttonBar;
	private JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables


	public static DeleteParamDialog getInstance(Frame owner) {
		if(deleteParamdialog==null){
			deleteParamdialog=new DeleteParamDialog(owner);
		}
		deleteParamdialog.setVisible(true);
		return deleteParamdialog;
	}
}
