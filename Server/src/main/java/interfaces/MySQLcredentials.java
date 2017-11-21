/*
 * Created by JFormDesigner on Tue Jul 04 21:54:01 IST 2017
 */

package interfaces;

import java.awt.Dialog;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import javax.swing.*;

import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import net.miginfocom.swing.*;
import com.ibatis.common.jdbc.ScriptRunner;

/**
 * @author Garvita Bajaj
 */
public class MySQLcredentials extends JDialog {
	public static MySQLcredentials sqlObject=null;

	public MySQLcredentials() {
		initComponents();
	}

	public static MySQLcredentials getInstance(){
		if(sqlObject==null){
			sqlObject=new MySQLcredentials();
		}
		sqlObject.setVisible(true);
		return sqlObject;
	}
	
	private void sendSQLinfo(ActionEvent e) {
		MainScreen.mysqlPort=sqlPort.getText();
		MainScreen.mysqlDB=sqlDB.getText();
		MainScreen.mysqlUser=sqlUser.getText();
		MainScreen.mysqlPwd=String.valueOf(sqlPwd.getPassword());
		MainScreen.connect=connectToDatabase();
	}

	private Connection connectToDatabase() {
		// Connect to the database using this dialog
		String port = sqlPort.getText();
		String db=sqlDB.getText();
		String username=sqlUser.getText();
		String password=String.valueOf(sqlPwd.getPassword());
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			String dbString="jdbc:mysql://localhost:"+port+"/"+db;
			Connection con=DriverManager.getConnection(dbString,username,password);
			JOptionPane.showInternalMessageDialog(sqlObject.getContentPane(), "Database connection established");
			System.out.println("Database connection established");
			dispose();
			return con;
		}
		catch(MySQLSyntaxErrorException e2){
			String aSQLScriptFilePath = "src/main/java/database/database.sql";
			String dbString="jdbc:mysql://localhost:"+port;
			try {
				Connection con = DriverManager.getConnection(dbString, username, password);
				ScriptRunner sr = new ScriptRunner(con, false, false);
				Reader reader = new BufferedReader(new FileReader(aSQLScriptFilePath));
				sr.runScript(reader);
				Connection con1=DriverManager.getConnection(dbString,username,password);
				JOptionPane.showInternalMessageDialog(sqlObject.getContentPane(), "Database connection established");
				System.out.println("Database connection established");
				dispose();
				return con1;
			} catch (Exception e) {
				System.err.println("Failed to Execute" + aSQLScriptFilePath
						+ " The error is " + e.getMessage());
			}
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
			JOptionPane.showInternalMessageDialog(sqlObject.getContentPane(),e1.getLocalizedMessage());}
		return null;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		ResourceBundle bundle = ResourceBundle.getBundle("properties");
		this.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		label1 = new JLabel();
		sqlPort = new JTextField("3306");
		label2 = new JLabel();
		sqlDB = new JTextField("mew");
		label3 = new JLabel();
		sqlUser = new JTextField("root");
		label4 = new JLabel();
		sqlPwd = new JPasswordField("root");
		button1 = new JButton();

		//======== this ========

		setLayout(new MigLayout(
				"hidemode 3",
				// columns
				"[113,fill]" +
				"[135,fill]",
				// rows
				"[]" +
				"[]" +
				"[]" +
				"[]" +
				"[]"));

		//---- label1 ----
		label1.setText(bundle.getString("MySQLcredentials.label1.text"));
		add(label1, "cell 0 0");
		add(sqlPort, "cell 1 0");

		//---- label2 ----
		label2.setText(bundle.getString("MySQLcredentials.label2.text"));
		add(label2, "cell 0 1");
		add(sqlDB, "cell 1 1");

		//---- label3 ----
		label3.setText(bundle.getString("MySQLcredentials.label3.text"));
		add(label3, "cell 0 2");
		add(sqlUser, "cell 1 2");

		//---- label4 ----
		label4.setText(bundle.getString("MySQLcredentials.label4.text"));
		add(label4, "cell 0 3");
		add(sqlPwd, "cell 1 3");

		//---- button1 ----
		button1.setText(bundle.getString("MySQLcredentials.button1.text"));
		button1.addActionListener(e -> sendSQLinfo(e));
		add(button1, "cell 1 4");
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JLabel label1;
	private JTextField sqlPort;
	private JLabel label2;
	private JTextField sqlDB;
	private JLabel label3;
	private JTextField sqlUser;
	private JLabel label4;
	private JPasswordField sqlPwd;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
