import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class RabbitMQConfigurations {
    ConnectionFactory conFact;

    public RabbitMQConfigurations(){
        conFact = new ConnectionFactory();
        conFact.setHost("localhost");
        conFact.setUsername("test");
        conFact.setPassword("test123");
    }
    private BlockingDeque<Pair<String, String>> queue = new LinkedBlockingDeque< Pair<String, String> >();

    public void addMessageToQueue(String msg, String routing_key) throws Exception{
        Pair<String,String> resourcePair=new Pair<>(msg,routing_key);
        queue.add(resourcePair);
        publishThread();
    }

    public void publishThread() throws Exception{
        try {
            System.out.println("Publishing the query");
            Connection connection = conFact.newConnection();
            Channel ch = connection.createChannel();
            ch.confirmSelect();
            ch.exchangeDeclare("exchange.ps.resquery", "topic",true);
            Pair<String,String> message = queue.takeFirst();
            String routingKey = message.getRight();
            ch.basicPublish("exchange.ps.resquery", routingKey , null, message.getLeft().getBytes());
            ch.waitForConfirmsOrDie(30000);
            ch.close();
            connection.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
