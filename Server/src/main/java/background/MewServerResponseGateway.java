package background;

import com.rabbitmq.client.ConnectionFactory;
import interfaces.RabbitMQConfigurations;

public class MewServerResponseGateway{

		private static MewServerResponseGateway mewServer = null ;

		private MewServerResponseGateway() {
		}
		
		public static MewServerResponseGateway getInstance() {
			if(mewServer == null) {
				return new MewServerResponseGateway();
			}
			return mewServer;
		}

	public boolean publishQueryToProviders(String msg, String routingKey){
		try{
		ConnectionFactory factory=RabbitMQConfigurations.conFact;
		com.rabbitmq.client.Connection connection = factory.newConnection();
		com.rabbitmq.client.Channel channel = connection.createChannel();
		String message = msg;
		channel.basicPublish(RabbitMQConfigurations.SELECTION_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
		channel.close();
		connection.close();
		return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

}


