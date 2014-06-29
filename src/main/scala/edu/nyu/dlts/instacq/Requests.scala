package edu.nyu.dlts.instacq

import com.typesafe.config.Config
import java.io.{BufferedReader, StringReader}
import java.util.UUID
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.JsonNode
import scala.io.Source
import scala.annotation.tailrec

class Requests(client: CloseableHttpClient, conf: Config){
  
  def request(get: HttpGet): JsonNode = {
    val response = client.execute(get)
    val entity = response.getEntity
    val content = Source.fromInputStream(entity.getContent).mkString("")
    EntityUtils.consume(entity)
    response.close
    val mapper = new ObjectMapper
    val reader = new BufferedReader(new StringReader(content))
    mapper.readTree(reader)
  }	
  
  def getUserById(uId: String): Map[String, String] ={
    val get = new HttpGet(conf.getString("instag.endpoint") + "users/" + uId + "/?client_id=" + conf.getString("instag.client_id"))
    val root = request(get)
    var map = Map("uId" -> uId)
    map += ("uName" -> root.get("data").get("username").getTextValue)
    map += ("uFullName" -> root.get("data").get("full_name").getTextValue)
    map
  }

  def getImagesById(uId: String): List[String] = {
    val url = conf.getString("instag.endpoint") + "/users/" +  uId + "/media/recent?client_id=" + conf.getString("instag.client_id")
    var list = List.empty[String]
    getResult(url)

    @tailrec 
    def getResult(url :String): Unit = {
      val rootNode = request(new HttpGet(url))
      val pageNode = rootNode.get("pagination")
      val dataNode = rootNode.get("data")

      (0 to dataNode.size - 1).foreach{i => 
	list :::= List(dataNode.get(i).get("id").getTextValue)
      }
	
      if(pageNode.size == 2) getResult(pageNode.get("next_url").getTextValue)
    }

    list
  }

  def getImageById(id: String): Map[String, String] ={
    val get = new HttpGet(conf.getString("instag.endpoint") + "/media/" + id + "/?client_id=" + conf.getString("instag.client_id"))
    val rootNode = request(get)
    val dataNode = rootNode.get("data")	
    val imageNode = dataNode.get("images")

    var map = Map("createdTime" -> dataNode.get("created_time").getTextValue)
    map += ("standardUrl" -> imageNode.get("standard_resolution").get("url").getTextValue)
    map += ("lowUrl" -> imageNode.get("low_resolution").get("url").getTextValue)
    map += ("thumbUrl" -> imageNode.get("thumbnail").get("url").getTextValue)

    if(! dataNode.get("caption").isNull) 
      map += ("caption" -> dataNode.get("caption").get("text").getTextValue)
    else 
      map += ("caption" -> "")
    map
  }

  def getCommentsByMediaId(id: String): List[Map[String, String]] = {
    import scala.collection.JavaConversions._
    var list = new scala.collection.mutable.ListBuffer[Map[String, String]]
    val get = new HttpGet(conf.getString("instag.endpoint") + "/media/" + id + "/?client_id=" + conf.getString("instag.client_id"))
    val rootNode = request(get)  
    if(rootNode.get("meta").get("code").getIntValue == 200){
      val dataNode = rootNode.get("data").get("comments").get("data")
      dataNode.foreach{comment => 
        var map = Map("commentId" -> comment.get("id").getTextValue)
        map += ("text" -> comment.get("text").getTextValue)
        map += ("createdTime" -> comment.get("created_time").getTextValue)
        map += ("username" -> comment.get("from").get("username").getTextValue)
        map += ("userFullName" -> comment.get("from").get("full_name").getTextValue)
        map += ("userId" -> comment.get("from").get("id").getTextValue)
        list += map
      }
    }
    list.toList
  }

  def findUserByUsername(name: String): Map[Int, Map[String, String]] = {
    import scala.collection.JavaConversions._
    val users = scala.collection.mutable.Map.empty[Int, Map[String, String]]
    val get = new HttpGet(conf.getString("instag.endpoint") + "/users/search?q=" + name + "&client_id=" + conf.getString("instag.client_id"))
    val rootNode = request(get)
    var count = 1
    rootNode.get("data").foreach{user => 
      var map = Map("id" -> user.get("id").getTextValue)
      map += ("fullName" -> user.get("full_name").getTextValue)
      map += ("username" -> user.get("username").getTextValue)
      users(count) = map
      count += 1
    }
    users.toMap
  }
}
