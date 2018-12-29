/*
 * Created by JFormDesigner on Mon Nov 20 11:15:44 IST 2017
 */

package interfaces;

import net.miginfocom.swing.MigLayout;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Query;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Garvita Bajaj
 */
public class TaskRequest extends JFrame {

	public static TaskRequest tr=null;

	public enum Sensors_available{ACCELEROMETER("Accelerometer"),GPS("GPS"),MICROPHONE("Microphone"),WiFi("WiFi"), GYROSCOPE("Gyroscope"), HEARTRATE("Heart-Rate"),BAROMETER("Barometer");
		 String key;

		 Sensors_available(String key) { this.key = key; }

		 @Override
		    public String toString() {
		        return key;
		    }
		};

	public enum Contexts_available{INVEHICLE(0),ONFOOT(1),ONBICYCLE(2),RUNNING(3), STILL(4), WALKING(5),UNKNOWN(6);
		Integer key;

		Contexts_available(Integer key) { this.key = key; }

//		@Override
//		public Integer toInteger() {
//			return key;
//		}
	};

	public static TaskRequest getInstance(){
		if(tr==null)
			tr=new TaskRequest();
		tr.setVisible(true);
		return tr;
	}

	public TaskRequest() {
		initComponents();
		comboBox1.setModel(new DefaultComboBoxModel<>(Sensors_available.values()));
		comboBox2.setModel(new DefaultComboBoxModel<>(Contexts_available.values()));
	}

	private void issueTaskRequest(ActionEvent e) {
		Query q = new Query();
		q.setFromTime();
		q.setToTime(Integer.parseInt(duration.getText()));
		q.setMin(Integer.parseInt(count.getText()));
		q.setMax(q.getMin());
		q.setLatitude(Double.parseDouble(lat.getText()));
		q.setLongitude(Double.parseDouble(lon.getText()));
		q.setSensorName(comboBox1.getSelectedItem().toString());
		System.out.println(type.getText().toString());
		q.setType(type.getText().toString());
		q.setActivity(comboBox2.getSelectedIndex());
		JSONObject jsonQuery=q.generateJSONQuery(q);
		try {
			q.sendQueryToServer(jsonQuery);
			JOptionPane.showInternalMessageDialog(tr.getContentPane(), "Task issued to server");
			dispose();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showInternalMessageDialog(tr.getContentPane(), "An exception occurred");
			dispose();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showInternalMessageDialog(tr.getContentPane(), "An exception occurred");
			dispose();
		}catch(Exception e4){
		e4.printStackTrace();
		JOptionPane.showInternalMessageDialog(tr.getContentPane(), "An exception occurred");
		dispose();
		}
		System.out.println(jsonQuery.toString());
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		label1 = new JLabel();
		comboBox1 = new JComboBox();
		label2 = new JLabel();
		duration = new JTextField();
		label3 = new JLabel();
		lat = new JTextField();
		label4 = new JLabel();
		lon = new JTextField();
		label5 = new JLabel();
		count = new JTextField();
		label6 = new JLabel();
		type = new JTextField();
		label7=new JLabel();
		comboBox2=new JComboBox();
		issueTask = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout(
			"hidemode 3",
			// columns
			"[fill]" +
			"[107,fill]",
			// rows
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
			"[]" +
				"[]"));

		//---- label1 ----
		label1.setText("Sensor Data required");
		contentPane.add(label1, "cell 0 0");
		contentPane.add(comboBox1, "cell 1 0");

		//---- label2 ----
		label2.setText("Duration");
		contentPane.add(label2, "cell 0 1");

		//---- duration ----
		duration.setToolTipText("in minutes");
		duration.setText("(in minutes)");
		contentPane.add(duration, "cell 1 1");

		//---- label3 ----
		label3.setText("Latitude");
		contentPane.add(label3, "cell 0 2");
		contentPane.add(lat, "cell 1 2");

		//---- label4 ----
		label4.setText("Longitude");
		contentPane.add(label4, "cell 0 3");
		contentPane.add(lon, "cell 1 3");

		//---- label5 ----
		label5.setText("Participants required");
		contentPane.add(label5, "cell 0 4");
		contentPane.add(count, "cell 1 4");

		//---- label6 ----
		label6.setText("Allocation Algo");
		contentPane.add(label6, "cell 0 5");
		contentPane.add(type, "cell 1 5");

		//---- label6 ----
		label7.setText("Participant Context");
		contentPane.add(label7, "cell 0 6");
		contentPane.add(comboBox2, "cell 1 6");

		//---- issueTask ----
		issueTask.setText("Generate Task Request");
		issueTask.addActionListener(e -> issueTaskRequest(e));
		contentPane.add(issueTask, "cell 1 7");
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JLabel label1;
	private JComboBox comboBox1;
	private JLabel label2;
	private JTextField duration;
	private JLabel label3;
	private JTextField lat;
	private JLabel label4;
	private JTextField lon;
	private JLabel label5;
	private JTextField count;
	private JLabel label6;
	private JTextField type;
	private JButton issueTask;
	private JLabel label7;
	private JComboBox comboBox2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
