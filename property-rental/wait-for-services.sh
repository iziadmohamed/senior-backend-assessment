#!/bin/sh

echo "Waiting for PostgreSQL..."
until nc -z postgres 5432; do
  sleep 2
done

echo "Waiting for Elasticsearch..."
until nc -z elasticsearch 9200; do
  sleep 2
done

echo "Waiting for Kafka..."
until nc -z kafka 9092; do
  sleep 2
done

echo "All services are up. Starting application..."
exec java -jar /app.jar
