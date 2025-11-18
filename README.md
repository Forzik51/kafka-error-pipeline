# kafka-error-pipeline
Event-driven pipeline that reads application logs, filters out error events with Kafka Streams, and sends alerts (e.g. to Slack) with a dead-letter queue for failed notifications.
