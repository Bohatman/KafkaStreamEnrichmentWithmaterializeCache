Test result select data from materialized cache (Ksql table) by java ksql-api-client

Docker file: https://github.com/confluentinc/cp-all-in-one/tree/6.1.1-post/cp-all-in-one

Data in table: 9760282 message (All is unique)

Message schema:

```json
{
  "connect.name": "ksql.product",
  "fields": [
    {
      "name": "id",
      "type": "long"
    },
    {
      "name": "name",
      "type": "string"
    },
    {
      "name": "description",
      "type": "string"
    },
    {
      "name": "price",
      "type": "double"
    }
  ],
  "name": "product",
  "namespace": "ksql",
  "type": "record"
}
```

Ksql statement:

```sql
CREATE TABLE PRODUCT_TABLE WITH (KAFKA_TOPIC='PRODUCT_TABLE', PARTITIONS=1, REPLICAS=1) 
AS SELECT TOPIC_TABLE.ID ID, 
	LATEST_BY_OFFSET(TOPIC_TABLE.NAME) KSQL_COL_0,  
	LATEST_BY_OFFSET(TOPIC_TABLE.DESCRIPTION) KSQL_COL_1,  
	LATEST_BY_OFFSET(TOPIC_TABLE.PRICE) KSQL_COL_2 
	FROM TOPIC_TABLE TOPIC_TABLE GROUP 
	BY TOPIC_TABLE.ID EMIT CHANGES;
```

Result:

| Query time | Time      |
| ---------- | --------- |
| 100        | 1.277 s   |
| 1000       | 3.394 s   |
| 10000      | 20.96 s   |
| 100000     | 2.954 min |

Average:  **353.5630234** query per second

If add more partition speed up  duration {number of partition}x shorter (KSQL node,Broker node must relate with number of partition)

Ref: https://www.confluent.io/blog/how-real-time-stream-processing-safely-scales-with-ksqldb/
