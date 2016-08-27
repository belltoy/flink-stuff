package com.jgrier.flinkstuff.metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics.{MetricFilter, ScheduledReporter}
import metrics_influxdb.InfluxdbReporter
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer
import metrics_influxdb.api.protocols.InfluxdbProtocols
import org.apache.flink.dropwizard.ScheduledDropwizardReporter
import org.apache.flink.metrics.MetricConfig

/**
  * InfluxDB Metrics Reporter for Apache Flink
  *
  * To use this add the following configuration to your flink-conf.yaml file and place the JAR containing
  * this class in your flink/lib directory:
  *
  * #==========================================
    # Metrics
    #==========================================
    metrics.reporters: influxdb
    metrics.reporter.influxdb.server: localhost
    metrics.reporter.influxdb.port: 8086
    metrics.reporter.influxdb.user: admin
    metrics.reporter.influxdb.password: admin
    metrics.reporter.influxdb.db: flink
    metrics.reporter.influxdb.class: com.jgrier.flinkstuff.metrics.InfluxDbReporter
    metrics.reporter.influxdb.interval: 1 SECONDS

    # metrics format: host.process_type.process_id.job_name.task_name.index

    metrics.scope.jm: <host>.jobmanager.1.jobmanager.jobmanager.1
    metrics.scope.jm.job: <host>.jobmanager.1.<job_name>.jobmanager.1
    metrics.scope.tm: <host>.taskmanager.<tm_id>.taskmanager.taskmanager.1
    metrics.scope.tm.job: <host>.taskmanager.<tm_id>.<job_name>.taskmanager.1
    metrics.scope.tm.task: <host>.taskmanager.<tm_id>.<job_name>.<task_name>.<subtask_index>
    metrics.scope.tm.operator: <host>.taskmanager.<tm_id>.<job_name>.<operator_name>.<subtask_index>
  */
class InfluxDbReporter extends ScheduledDropwizardReporter {
  override def getReporter(metricConfig: MetricConfig): ScheduledReporter = {

    val server = metricConfig.getString("server", "localhost")
    val port = metricConfig.getInteger("port", 8086)
    val user = metricConfig.getString("user", "admin")
    val password = metricConfig.getString("password", "admin")
    val db = metricConfig.getString("db", "flink")

    InfluxdbReporter.forRegistry(registry)
      .protocol(InfluxdbProtocols.http(server, port, user, password, db))
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .filter(MetricFilter.ALL)
      .skipIdleMetrics(false)
      .transformer(new CategoriesMetricMeasurementTransformer("host", "process_type", "process_id", "job_name", "task_name", "index"))
      .build()
  }
}
