/*
 * Created by JFormDesigner on Tue Jul 04 20:27:37 IST 2017
 */

package interfaces;

import beans.Scheduler;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import messageHandling.LeaveListener;
import messageHandling.LeaveProvider;
import messageHandling.UpdateResources;
import net.miginfocom.swing.MigLayout;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Garvita Bajaj
 */
public class RabbitMQConfigurations extends JFrame {

	private static final long serialVersionUID = 1L;
	//	public static ApplicationContext schedulerContext;
	protected final String SERVER_RESOURCES_QUEUE = "queue.ps.resources";
	public String rabbitUser, rabbitPwd, rabbitPort;
	public static ConnectionFactory conFact=null;
	protected final String SERVER_QUERY_QUEUE = "queue.ps.queries";

	protected final String resourcesRoutingKey = "resources";

	protected final String queriesRoutingKey = "queries";

	public static String SELECTION_EXCHANGE_NAME = "exchange.ps.selection";

	protected static String RESOURCE_QUERY_EXCHANGE_NAME = "exchange.ps.resquery";
	private BlockingDeque<Pair<String, String>> queue = new LinkedBlockingDeque< Pair<String, String> >();

	public RabbitMQConfigurations() {
		initComponents();
	}

	public static RabbitMQConfigurations rabbitObject=null;

	public static RabbitMQConfigurations getInstance(){
		if(rabbitObject==null){
			rabbitObject=new RabbitMQConfigurations();
		}
		return rabbitObject;
	}
//	public ConnectionFactory conFactory;


	private void initializeRabbitMQ(ActionEvent e) {
		try {
			defineConFactory();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		dispose();
	}

	public void addMessageToQueue(Map<String,Object> msg, String routing_key) throws Exception{
		Pair<String,String> resourcePair=new Pair<>(new JSONObject(msg).toString(),routing_key);
		queue.add(resourcePair);
		publishThread();
	}

	public void publishThread() throws Exception{
		try {
			Connection connection = conFact.newConnection();
			Channel ch = connection.createChannel();
			ch.confirmSelect();
			ch.exchangeDeclare(RESOURCE_QUERY_EXCHANGE_NAME, "topic",true);
			Pair<String,String> message = queue.takeFirst();
			String routingKey = message.getRight();
			ch.basicPublish(RESOURCE_QUERY_EXCHANGE_NAME, routingKey , null, message.getLeft().getBytes());
			ch.waitForConfirmsOrDie(30000);
			ch.close();
			connection.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Bean
	public ConnectionFactory defineConFactory() throws Exception{
		rabbitUser=rabbitmquser.getText();
		rabbitPwd=String.valueOf(rabbitmqpwd.getPassword());
		conFact = new ConnectionFactory();
		conFact.setHost("localhost");
		conFact.setUsername(rabbitUser);
		conFact.setPassword(rabbitPwd);
		startListening();
		return conFact;
	}

	public void startListening() throws Exception{
		com.rabbitmq.client.Connection connection;
		try {
			connection = conFact.newConnection();
			Channel channel = connection.createChannel();
			channel.exchangeDeclare(RESOURCE_QUERY_EXCHANGE_NAME, "topic",true);
			channel.exchangeDeclare(SELECTION_EXCHANGE_NAME, "topic",true);
			DeclareOk q = channel.queueDeclare(SERVER_RESOURCES_QUEUE, true, false, false, null);
			String queueName = q.getQueue();
			channel.queueBind(queueName, RESOURCE_QUERY_EXCHANGE_NAME, resourcesRoutingKey);
			System.out.println("Resource Queue Created");
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope,
						AMQP.BasicProperties properties, byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					final JSONObject jsonMsgs=new JSONObject(message);
					int msgType=jsonMsgs.getInt("TYPE");
					//					channel.basicAck(envelope.getDeliveryTag(), true);
					switch(msgType) {

					case(1):
						UpdateResources resProcess=new UpdateResources(jsonMsgs);
					resProcess.start();
					break;
					case(4):
						LeaveProvider exitProvider=new LeaveProvider(jsonMsgs);
					exitProvider.start();
					break;
					case(2):
						Thread t=new Thread() {
						public void run(){
							String command="UPDATE nodes SET providerMode=? where DeviceID=?";
							try {
								PreparedStatement preparedStatement = MainScreen.connect.prepareStatement(command);
								preparedStatement.setBoolean(1, true);
								preparedStatement.setString(2, jsonMsgs.getString("NODE"));
								preparedStatement.executeUpdate();
								preparedStatement.close();
							}catch(SQLException e){
								e.printStackTrace();
							}
						}
					};
					t.start();
					break;
					case(3):
						LeaveListener leave=new LeaveListener(jsonMsgs);
					leave.start();
					break;
					case(5):
						Thread t1=new Thread() {
						public void run(){
							String command="UPDATE nodes SET servicingTask=false where DeviceID=?";
							try {
								PreparedStatement preparedStatement =  MainScreen.connect.prepareStatement(command);
								preparedStatement.setString(1, jsonMsgs.getString("NODE"));
								preparedStatement.executeUpdate();
								preparedStatement.close();
							}catch(SQLException e){
								e.printStackTrace();
							}
						}
					};
					t1.start();
					break;
					case(6):
						Thread t2=new Thread() {
						public void run(){
							String command="delete from queries where queryID=? AND providerID=?";
							try {
								PreparedStatement preparedStatement =  MainScreen.connect.prepareStatement(command);
								preparedStatement.setString(1, jsonMsgs.getString("QUERY"));
								preparedStatement.setString(2, jsonMsgs.getString("NODE"));
								preparedStatement.executeUpdate();
								preparedStatement.close();
							}catch(SQLException e){
								System.out.println("@#$#@$@#$@#$@#$@#$%$@%#@$!#$");
								e.printStackTrace();
							}
						}
					};
					t2.start();
					break;
					case(7):
						Thread t3=new Thread() {
						public void run(){
							String command="update queries set serviced=1 where queryID=? AND providerID=?";
							try {
								PreparedStatement preparedStatement =  MainScreen.connect.prepareStatement(command);
								preparedStatement.setString(1, jsonMsgs.getString("QUERY"));
								preparedStatement.setString(2, jsonMsgs.getString("NODE"));
								preparedStatement.executeUpdate();
								preparedStatement.close();
							}catch(SQLException e){
								System.out.println("@#$#@$@#$@#$@#$@#$%$@%#@$!#$");
								e.printStackTrace();
							}
							String command1="delete from queries where queryID=? AND providerID=?";
							try {
								PreparedStatement preparedStatement =  MainScreen.connect.prepareStatement(command1);
								preparedStatement.setString(1, jsonMsgs.getString("QUERY"));
								preparedStatement.setString(2, jsonMsgs.getString("NODE"));
								preparedStatement.executeUpdate();
								preparedStatement.close();
							}catch(SQLException e){
								System.out.println("@#$#@$@#$@#$@#$@#$%$@%#@$!#$");
								e.printStackTrace();
							}
						}
					};
					t3.start();
					break;
					default:
						System.out.println("Resource type received: "+msgType);
					}

				}
			};
			channel.basicConsume(queueName, true, consumer);


			Channel channel2=connection.createChannel();
			DeclareOk q1 = channel2.queueDeclare(SERVER_QUERY_QUEUE, true, false, false, null);
			String queriesQueue = q1.getQueue();
			channel2.queueBind(queriesQueue, RESOURCE_QUERY_EXCHANGE_NAME, queriesRoutingKey);
			System.out.println("Queries Queue Created");
			Consumer consumer2 = new DefaultConsumer(channel2) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					JSONObject jsonMsgs = new JSONObject(message);

					JSONObject query = jsonMsgs.getJSONObject("Query");
					Long startTime = query.getLong("fromTime");

					Scheduler.queryQueue.put(startTime, query);
					System.out.println("Received '" + message + "'");

					Scheduler scheduler = MainScreen.schedulerContext.getBean(Scheduler.class);
					if(scheduler!=null){
						System.out.println("New query received");
						scheduler.schedule();
					}else{
						JOptionPane.showMessageDialog(null, "some problem");
					}
				}
			};
			channel2.basicConsume(queriesQueue, true, consumer2);
			JOptionPane.showInternalMessageDialog(rabbitObject.getContentPane(), "Server setup complete");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showInternalMessageDialog(rabbitObject.getContentPane(), "An exception occurred");
		}

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Garvita Bajaj
		ResourceBundle bundle = ResourceBundle.getBundle("properties");
		label1 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		rabbitmquser = new JTextField("test");
		label4 = new JLabel();
		rabbitmqpwd = new JPasswordField("test123");
		label5 = new JLabel();
		rabbitmqport = new JTextField("5671");
		button1 = new JButton();

		//======== this ========
		setTitle(bundle.getString("RabbitMQConfigurations.this.title"));
		Container contentPane = getContentPane();
		contentPane.setLayout(new MigLayout(
				"hidemode 3",
				// columns
				"[118,fill]" +
				"[134,fill]",
				// rows
				"[32]" +
				"[26]" +
				"[17]" +
				"[]" +
				"[]" +
				"[]"));

		//---- label1 ----
		label1.setText(bundle.getString("RabbitMQConfigurations.label1.text"));
		contentPane.add(label1, "cell 0 0 2 1");

		//---- label2 ----
		label2.setText(bundle.getString("RabbitMQConfigurations.label2.text"));
		contentPane.add(label2, "cell 0 1 2 1");

		//---- label3 ----
		label3.setText(bundle.getString("RabbitMQConfigurations.label3.text"));
		contentPane.add(label3, "cell 0 2");
		contentPane.add(rabbitmquser, "cell 1 2");

		//---- label4 ----
		label4.setText(bundle.getString("RabbitMQConfigurations.label4.text"));
		contentPane.add(label4, "cell 0 3");
		contentPane.add(rabbitmqpwd, "cell 1 3");

		//---- label5 ----
		label5.setText(bundle.getString("RabbitMQConfigurations.label5.text"));
		contentPane.add(label5, "cell 0 4");
		contentPane.add(rabbitmqport, "cell 1 4");

		//---- button1 ----
		button1.setText(bundle.getString("RabbitMQConfigurations.button1.text"));
		button1.addActionListener(e -> initializeRabbitMQ(e));
		contentPane.add(button1, "cell 1 5");
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Garvita Bajaj
	private JLabel label1;
	private JLabel label2;
	private JLabel label3;
	private JTextField rabbitmquser;
	private JLabel label4;
	private JPasswordField rabbitmqpwd;
	private JLabel label5;
	private JTextField rabbitmqport;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
