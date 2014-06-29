package edu.nyu.dlts.instacq

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID
import java.io.File
import org.slf4j.LoggerFactory


class Crawl(){

  val session = new Session
  val crawlUUID = UUID.randomUUID
 
  session.logger.info("Starting instacq crawl")
  
  def crawlImages{
    val mediaIds = session.db.getImageIds
    var imageCount = 0

    session.db.getAccounts.foreach{account => 
      session.logger.info("new image crawl started")
      val userUUID = UUID.fromString(account("id"))
      session.db.addCrawl(crawlUUID, userUUID)    
      val images = session.requests.getImagesById(account("userId"))

      images.foreach{image =>
	if(! mediaIds.contains(image)){
          session.logger.info("adding image: " + image)
          var media = session.requests.getImageById(image)
          media += ("accountId" -> account("id"))          
          media += ("crawlId" -> crawlUUID.toString)
          media += ("mediaId" -> image)
          writeFile(media)
          session.db.addImage(media)
          imageCount += 1
	}
      }
    
 
      session.logger.info("image crawl complete")
      session.logger.info(imageCount + " images added")
    }
  }

  def updateComments{
    var commentCount = 0
    val images = session.db.getAllImageIds
    val commentIds = session.db.getCommentIds()

    session.logger.info("new comment crawl started")
    images.foreach{image => 
      val imageUUID = image._1
      val imageId = image._2
      val comments = session.requests.getCommentsByMediaId(imageId)
      if(comments.size > 0){
	comments.foreach{comment =>
          if(! commentIds.contains(comment("commentId"))){
            session.logger.info("adding comment: " + comment("commentId"))
            var c = comment ++ Map("imageId" -> imageUUID.toString)
            c += ("crawlId" -> crawlUUID.toString)
            session.db.addComment(c)
            commentCount += 1
          }
	}
      }
    }
    session.logger.info("comment crawl complete")
    session.logger.info(commentCount + " comments added")
  }

  def writeFile(media: Map[String, String]){
    val standardDir = new File(session.conf.getString("instag.data_dir"), "standard")
    val lowDir = new File(session.conf.getString("instag.data_dir"), "low")
    val thumbDir = new File(session.conf.getString("instag.data_dir"), "thumb")

    new ImageDownload(media("standardUrl"), new File(standardDir, media("standardUrl").split("/" ).last), session.client)
    new ImageDownload(media("lowUrl"), new File(lowDir, media("lowUrl").split("/" ).last), session.client)
    new ImageDownload(media("thumbUrl"), new File(thumbDir, media("thumbUrl").split("/" ).last), session.client)
  }
}

object Crawl extends App{
  val crawl = new Crawl
  crawl.crawlImages
  crawl.updateComments
}
