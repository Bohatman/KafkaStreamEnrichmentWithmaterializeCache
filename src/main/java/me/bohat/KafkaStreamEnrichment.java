package me.bohat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.ksql.api.client.BatchedQueryResult;
import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaStreamEnrichment {
    public static String KSQLDB_SERVER_HOST = "localhost";
    public static int KSQLDB_SERVER_HOST_PORT = 8088;
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ClientOptions options = ClientOptions.create()
                .setHost(KSQLDB_SERVER_HOST)
                .setPort(KSQLDB_SERVER_HOST_PORT)
                .setExecuteQueryMaxResultRows(1);
        Client client = Client.create(options);
        long id = 10000;
        long i = 0;
        Stopwatch timer = Stopwatch.createStarted();
        for (; i < 10000L; i++) {
            getValue(client,id + i).get("id2");
        }
        System.out.println("Message: " + i);
        System.out.println("Time: " + timer.stop());

        client.close();
    }
    public static Map getValue(Client client,long id) throws ExecutionException, InterruptedException {
        String query = "SELECT * FROM PRODUCT_TABLE WHERE ID = " + id + ";";
        Map result = new HashMap();
        client.executeQuery(query).get().forEach(row -> {
            result.put("id2",row.getLong(1));
            result.put("name2",row.getString(2));
            result.put("description2",row.getString(3));
            result.put("price2",row.getDouble(4));
        });
        return result;
    }
    public static Properties getKafkaStreamConfig(){
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "Mark");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(SchemaRegistryClientConfig.CLIENT_NAMESPACE+ "url","http://localhost:8081");
        return  config;
    }
}
