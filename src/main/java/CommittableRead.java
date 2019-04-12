import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.amqp.*;
import akka.stream.alpakka.amqp.javadsl.AmqpSink;
import akka.stream.alpakka.amqp.javadsl.AmqpSource;
import akka.stream.alpakka.amqp.javadsl.CommittableReadResult;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CommittableRead {
    public static void main(String[] args) throws InterruptedException {
        final ActorSystem system;
        system = ActorSystem.create();

        final ActorMaterializer materializer = ActorMaterializer.create(system);

        AmqpUriConnectionProvider amqpUriConnectionProvider = new
                AmqpUriConnectionProvider("amqp://guest:guest@localhost:5672/entities");

        final AmqpConnectionProvider connectionProvider = new
                AmqpCachedConnectionProvider(amqpUriConnectionProvider, false);
        final String queueName = "source-" + System.currentTimeMillis();
        final QueueDeclaration queueDeclaration = QueueDeclaration.create(queueName);
        writeToAMQPSource(materializer, connectionProvider, queueName, queueDeclaration);


        System.out.println("Ready + handling");

        Thread.sleep(1000);

        final Integer bufferSize = 10;
        final Source<CommittableReadResult, NotUsed> amqpSource =
                AmqpSource.committableSource(
                        NamedQueueSourceSettings.create(connectionProvider, queueName)
                                .withDeclaration(queueDeclaration),
                        bufferSize);

        final CompletionStage<List<ReadResult>> result =
                amqpSource
                        .mapAsync(1, CommittableRead::businessLogic)
                        .mapAsync(1, cm -> cm.ack(false).thenApply(unused -> cm.message()))
                        .take(10)
                        .runWith(Sink.seq(), materializer);

    }

    private static void writeToAMQPSource(ActorMaterializer materializer, AmqpConnectionProvider connectionProvider, String queueName, QueueDeclaration queueDeclaration) {
        final Sink<ByteString, CompletionStage<Done>> amqpSink =
                AmqpSink.createSimple(
                        AmqpWriteSettings.create(connectionProvider)
                                .withRoutingKey(queueName)
                                .withDeclaration(queueDeclaration));

        final List<String> input = Arrays.asList("one", "two", "three", "four", "five");
        CompletionStage<Done> writing =
                Source.from(input).map(ByteString::fromString).runWith(amqpSink, materializer);
    }

    private static CompletionStage<CommittableReadResult> businessLogic(CommittableReadResult msg) {
        System.out.println("Message is: " + msg.message().bytes().utf8String());
        return CompletableFuture.completedFuture(msg);
    }
}
