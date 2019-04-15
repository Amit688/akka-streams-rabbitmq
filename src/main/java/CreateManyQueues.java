import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

class Task implements Runnable
{
    private int id;
    Task(int id) {
        this.id = id;
    }
    @Override
    public void run() {
        System.out.println("Starting to run task " + this.id);
        createAndWriteToAMQPQueue();
        System.out.println("Task " + this.id + " is DONE!");
    }

    private static void createAndWriteToAMQPQueue() {
        ConnectionFactory factory = new ConnectionFactory();
        Connection conn = null;
        Channel channel = null;
        try {
            factory.setUri("amqp://guest:guest@localhost:5672/entities");
            conn = factory.newConnection();
            channel = conn.createChannel();

            Random r = new Random();
            String queueName = "source-" + System.currentTimeMillis() + "-" + r.nextInt(1000000);
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
public class CreateManyQueues {

    static final int MAX_THREADS = 32;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
        for (int i = 0; i < 100000; i++) {
            Runnable r = new Task(i);
            pool.execute(r);
        }
    }


}
