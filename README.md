# akka-streams-rabbitmq
Playing with RabbitMQ and Akka-Streams

Based on: 
* https://doc.akka.io/docs/alpakka/current/amqp.html
* https://www.rabbitmq.com/api-guide.html#publishing
* https://hub.docker.com/_/rabbitmq/

### 1. Run RabbitMQ
In order to run RabbitMQ, we will start a container for it and expose 5672 & 15672 ports:

`docker run -d --hostname my-rabbit --name some-rabbit -p 15672:15672 -p 5672:5672 rabbitmq:3-management`

Then you can open your browser and navigate to `http://localhost:15672/`

(default username & password are `guest`)

### 2. Run Mains