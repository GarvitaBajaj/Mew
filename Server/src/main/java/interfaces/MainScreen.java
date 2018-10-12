/*
 * Created by JFormDesigner on Tue Jul 04 20:08:54 IST 2017
 */

package interfaces;

import ParameterModule.ParameterModule;
import algoHelpers.UploadAlgoFile;
import net.miginfocom.swing.MigLayout;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * @author Garvita Bajaj
 */
public class MainScreen extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String mysqlPort,mysqlDB,mysqlUser,mysqlPwd;
	public static Connection connect;
	public static MainScreen main;
	public static ApplicationContext schedulerContext;
	public MainScreen() {
		initComponents();
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				if(connect!=null){
					try {
						connect.close();
						System.out.println("Database connection closed");
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				e.getWindow().dispose();
			}
		});
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				main = new MainScreen();
				main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				try 
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				main.pack();
				main.setTitle("Mew");
				main.setVisible(true);
			}
		});
		schedulerContext = loadSpringContext();
	}

	private static ApplicationContext loadSpringContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("file:src/main/java/beans/beanConfiguration.xml");
		context.start();
		System.out.println("Context"+ context.getBeanDefinitionCount());
		return context;
	}

	public static void showScreenMySQL(){
		//open the frame to get the SQL configuration file
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
				MySQLcredentials panel = MySQLcredentials.getInstance();
				panel.setTitle("Initialize Database configuration");
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setLocationRelativeTo(main);
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});		
	}

	private void initializeDB(ActionEvent e) {
		showScreenMySQL();
	}

	private void getRabbitMQConf(ActionEvent e) {
		//open the frame to get the SQL configuration file
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
				if(connect==null){
					JOptionPane.showMessageDialog(null, "Please initialize the SQL configurations before this");
					return;
				}
				RabbitMQConfigurations panel=RabbitMQConfigurations.getInstance();
				//				RabbitMQConfigurations panel=new RabbitMQConfigurations();
				panel.setTitle("Initialize Communication Module");
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
	}

	private void addNewParameters(ActionEvent e) {
		if(connect==null){
			JOptionPane.showMessageDialog(main, "Please initialize MySQL configurations");
			//			dispose();
			return;
		}
		//this should open a new frame which will show a table
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
				ParameterModule panel = ParameterModule.getInstance();
				panel.setTitle("Update task parameters");
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
	}

	private void addAlgo(ActionEvent e) {
		//open a dialog to enter the file location
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
				UploadAlgoFile panel = new UploadAlgoFile();
				panel.setTitle("Add a new algorithm");
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
	}

	private void listWorkers(ActionEvent e) {
		//display a dialog with the output of select statement
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
				WorkerList panel = new WorkerList();
				panel.setTitle("Available workers");
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
	}

	private void listAvailableAlgos(ActionEvent e) {
		//this should open a new frame which will show a table
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
				AvailableAlgos panel = AvailableAlgos.getInstance();
				panel.setTitle("List of available algorithms");
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
	}

	private void issueTaskRequest(ActionEvent e) {
		//create a JSON query with different parameters using a dialog box
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
				TaskRequest panel = TaskRequest.getInstance();
				panel.setTitle("Generate a new task");
				panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				panel.pack();
				panel.setVisible(true);
				panel.setResizable(false);
			}
		});
	}

	//	private void setupDB(ActionEvent e) {
	//		//open a dialog to enter the file location
	//				EventQueue.invokeLater(new Runnable()
	//				{
	//					@Override
	//					public void run()
	//					{
	//						try 
	//						{
	//							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	//						} catch (Exception e) {
	//							e.printStackTrace();
	//						}
	//						MySQLcredentials panel = new MySQLcredentials();
	//						panel.setTitle("Select the file");
	//						panel.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
	//						panel.pack();
	//						panel.setVisible(true);
	//						panel.setResizable(false);
	//					}
	//				});
	//	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		ResourceBundle bundle = ResourceBundle.getBundle("properties");
		label1 = new JLabel();
		label7 = new JLabel();
		button2 = new JButton();
		label8 = new JLabel();
		button3 = new JButton();
		label2 = new JLabel();
		newParameters = new JButton();
		button1 = new JButton();
		button4 = new JButton();
		availAlgos = new JButton();
		issueTask = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout(
				"hidemode 3",
				// columns
				"[131,fill]" +
				"[135,fill]",
				// rows
				"[27]" +
				"[]" +
				"[]" +
				"[26]" +
				"[24]" +
				"[]" +
				"[]" +
				"[]" +
				"[]"));

		//---- label1 ----
		label1.setText(bundle.getString("MainScreen.label1.text"));
		contentPane.add(label1, "cell 0 0 2 1");

		//---- label7 ----
		label7.setText(bundle.getString("MainScreen.label7.text"));
		contentPane.add(label7, "cell 0 2");

		//---- button2 ----
		button2.setText(bundle.getString("MainScreen.button2.text"));
		button2.addActionListener(e -> initializeDB(e));
		contentPane.add(button2, "cell 1 2");

		//---- label8 ----
		label8.setText(bundle.getString("MainScreen.label8.text"));
		contentPane.add(label8, "cell 0 3");

		//---- button3 ----
		button3.setText(bundle.getString("MainScreen.button3.text"));
		button3.addActionListener(e -> getRabbitMQConf(e));
		contentPane.add(button3, "cell 1 3");

		//---- label2 ----
		label2.setText(bundle.getString("MainScreen.label2.text"));
		contentPane.add(label2, "cell 0 4");

		//---- newParameters ----
		newParameters.setText(bundle.getString("MainScreen.newParameters.text"));
		newParameters.addActionListener(e -> addNewParameters(e));
		contentPane.add(newParameters, "cell 0 5");

		//---- button1 ----
		button1.setText(bundle.getString("MainScreen.button1.text"));
		button1.addActionListener(e -> addAlgo(e));
		contentPane.add(button1, "cell 1 5");

		//---- button4 ----
		button4.setText(bundle.getString("MainScreen.button4.text"));
		button4.addActionListener(e -> listWorkers(e));
		contentPane.add(button4, "cell 0 6");

		//---- availAlgos ----
		availAlgos.setText(bundle.getString("MainScreen.availAlgos.text"));
		availAlgos.addActionListener(e -> listAvailableAlgos(e));
		contentPane.add(availAlgos, "cell 1 6");

		//---- issueTask ----
		issueTask.setText(bundle.getString("MainScreen.issueTask.text"));
		issueTask.addActionListener(e -> issueTaskRequest(e));
		contentPane.add(issueTask, "cell 1 7");
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JLabel label1;
	private JLabel label7;
	private JButton button2;
	private JLabel label8;
	private JButton button3;
	private JLabel label2;
	private JButton newParameters;
	private JButton button1;
	private JButton button4;
	private JButton availAlgos;
	private JButton issueTask;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
