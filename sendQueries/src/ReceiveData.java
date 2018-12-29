import com.rabbitmq.client.*;
import jdk.nashorn.internal.runtime.ECMAException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class ReceiveData {

    ConnectionFactory factory;
    Connection connection;
    Channel channel;
    Thread subscribeThread;
    public ReceiveData() {
        try {
            factory = new ConnectionFactory();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void subscriber(){
        try {
            subscribeThread = new Thread(() -> {
            while (true) {
                try {
                    connection = factory.newConnection();
                    channel = connection.createChannel();
                    channel.basicQos(0);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare();
                    channel.queueBind(q.getQueue(), "exchange.ps.selection", Constants.PROVIDER);
                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(q.getQueue(), true, consumer);
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    System.out.println("Listening to messages on " + Constants.PROVIDER);

                    String message = new String(delivery.getBody());

                    final JSONObject jsonObject = new JSONObject(message);

                    if (jsonObject.has("TYPE") && !jsonObject.has("Query")) {
                        System.out.println("creating the response file now");
                        Thread processData = new Thread(() -> {
                            try {
                                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
                                Random rand = new Random();
                                int n = rand.nextInt(500) + 1;
                                try {
                                    File file;
                                    BufferedWriter writer;
                                    String queryNo, sensorData;
                                    queryNo = jsonObject.getString("queryNo");
                                    sensorData = jsonObject.getString("sensorData");
                                    File directory = new File("D:\\Mew\\ReceivedFiles\\" + queryNo);
                                    if (!directory.exists()) {
                                        directory.mkdirs();
                                    }
                                    file = new File(directory, "response" + n + ".csv");
                                    if (file.exists()) {
                                        file.delete();
                                        try {
                                            file.createNewFile();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    writer = new BufferedWriter(new FileWriter(file, false));
                                    writer.write(sensorData);
                                    writer.flush();
                                    writer.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        processData.start();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

//                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true  );
                }
        });

        }catch (Exception e){
            e.printStackTrace();
        }
    subscribeThread.start();
    System.out.println("Subscriber started");
    }

    public void stop(){
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
