import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class CreateManyQueues {

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            createAndWriteToAMQPQueue();
        }
    }

    private static void createAndWriteToAMQPQueue() {
        ConnectionFactory factory = new ConnectionFactory();
        Connection conn = null;
        Channel channel = null;
        try {
            factory.setUri("amqp://guest:guest@localhost:5672/entities");
            conn = factory.newConnection();
            channel = conn.createChannel();

            String queueName = "source-" + System.currentTimeMillis();
            channel.queueDeclare(queueName, false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
            System.out.println(" Sent '" + message + "'");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


        try {
            channel.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }
}
