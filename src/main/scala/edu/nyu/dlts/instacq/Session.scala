package edu.nyu.dlts.instacq

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import org.slf4j.LoggerFactory

class Session{
  val conf = ConfigFactory.load()
  val client = HttpClients.createDefault()
  val db = new Db(conf)
  val requests = new Requests(client, conf)
  val logger = LoggerFactory.getLogger(classOf[Crawl])
}
