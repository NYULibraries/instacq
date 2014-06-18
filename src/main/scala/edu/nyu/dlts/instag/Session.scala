package edu.nyu.dlts.instag

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger
import org.apache.log4j.{PropertyConfigurator, LogManager, Level}

class Session{
  val conf = ConfigFactory.load()
  val client = HttpClients.createDefault()
  val db = new Db(conf)
  val requests = new Requests(client, conf)
  val logger = Logger(LoggerFactory.getLogger(classOf[Session]))
  PropertyConfigurator.configure("log4j.properties")
}
