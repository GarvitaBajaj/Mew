import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class PublishQueries extends TimerTask{

    static File file;
    static Random random=new Random();
    static ConnectionFactory factory;
    static Connection connection;
    static Channel channel;
    static BufferedWriter writer;
    static List<String> lines=new ArrayList<>();
//    static int provider=0;
    static int _queriesPublished=0;
    static boolean _newQueries;
    static InputStreamReader instream;
    static ReceiveData rd;
//    static BufferedReader buffreader;

    PublishQueries(){
        //initialize the lines here
    }

    public static void main(String[] args){
        file=new File(new File(".").getAbsolutePath(),"queriesIssued.csv");
        try {
            factory = new ConnectionFactory();
            factory.setRequestedHeartbeat(60);
            factory.setNetworkRecoveryInterval(30000);
            String uri = "amqp://test:test123"+"@"+Constants.SERVER_ADDRESS+":5672/%2f";
            try {
                factory.setAutomaticRecoveryEnabled(true);
                factory.setUri(uri);
            } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
                e1.printStackTrace();
            }
            connection=factory.newConnection();
            channel=connection.createChannel();
            channel.confirmSelect();
            channel.exchangeDeclare(Constants.EXCHANGE_NAME,Constants.EXCHANGE_TYPE,true);
            channel.queueBind(Constants.QUEUE_NAME,Constants.EXCHANGE_NAME, Constants.ROUTING_KEY);

            if(!file.exists()) {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.newLine();
            writer.flush();
            _newQueries=true;
            System.out.println("Generating new queries");
            TimerTask timerTask = new PublishQueries();
            Timer timer=new Timer(true);
            timer.scheduleAtFixedRate(timerTask, 0, Constants.QUERY_TIME); //one query every 10 minutes
                        rd=new ReceiveData();
            rd.subscriber();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Timer task started at: "+new Date());
        publishQuery();
        _queriesPublished++;
        if(_queriesPublished==Constants.NOOFQUERIES) {
            cancel();
            System.out.println("Timer task finished at: "+new Date());
            rd.stop();
            try {
                if(writer!=null)
                    writer.close();
                if(channel!=null)
                    channel.close();
                if(instream!=null)
                    instream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    private static void publishQuery() {
        try{
                System.out.println("trying to generate a query");
                generateQuery();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void generateQuery() {
        Query query = new Query();
        JSONObject jsonQuery = null;
//        if (provider == Constants.PROVIDERS.length)
//            provider = 0;
        query.setLatitude(28.545);
        query.setLongitude(77.273);
        query.setRequesterID(Constants.PROVIDER);
//        String requesterID=Constants.PROVIDER;
//        provider++;
//        query.setMin(random.nextInt(Constants.MAX_PROVIDERS - Constants.MIN_PROVIDERS + 1) + Constants.MIN_PROVIDERS);
//        query.setMax(query.getMin());
        query.setMax(Constants.MIN_PROVIDERS);
        query.setMin(Constants.MIN_PROVIDERS);
        query.setFromTime(System.currentTimeMillis() + Constants.DELAY);    //Start query 5 minutes from now
        query.setToTime(query.getFromTime() + Constants.QUERY_DURATION);
        query.setSensorName("Accelerometer");
        query.setType("broadcast");
        jsonQuery = Query.generateJSONQuery(query);
        String main = "{\"Query\":" + jsonQuery.toString() + "}";
        System.out.println(main);
        RabbitMQConfigurations publishResource= new RabbitMQConfigurations();
        try {
            //writer.newLine();
            writer.write(jsonQuery.toString());
            writer.flush();
            publishResource.addMessageToQueue(main,"queries");
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
