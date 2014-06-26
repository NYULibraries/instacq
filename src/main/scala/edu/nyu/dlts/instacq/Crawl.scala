package edu.nyu.dlts.instacq

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID
import java.io.File
import org.slf4j.LoggerFactory


class Crawl(){

  val session = new Session
  session.logger.info("Starting instacq crawl")
  
  def crawlImages{
    val mediaIds = session.db.getImageIds
    var imageCount = 0

    session.db.getAccounts.foreach{account => 
      session.logger.info("new image crawl started")
      val userUUID = UUID.fromString(account("id"))
      val crawlUUID = UUID.randomUUID
      val crawlDir = new File(session.conf.getString("instag.data_dir"), crawlUUID.toString)
      session.db.addCrawl(crawlUUID, userUUID)    
      crawlDir.mkdir

      val images = session.requests.getImagesById(account("userId"))
      images.foreach{image =>
	if(! mediaIds.contains(image._1)){
          session.logger.info("adding image: " + image._1)
          
          val mediaMap = session.requests.getImageById(image._1)
	  mediaMap("accountId") = account("id")
          mediaMap("crawlId") = crawlUUID.toString
          mediaMap("mediaId") = image._1
          mediaMap("imageUrl") = image._2		    
      
	  writeFile(image._2, crawlDir)		    
          session.db.addImage(mediaMap)
          imageCount += 1
	}
      }
    
      if(crawlDir.list().length == 0){
	crawlDir.delete
	session.db.deleteCrawl(crawlUUID)
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
            comment("imageId") = imageUUID.toString
            session.db.addComment(comment)
            commentCount += 1
          }
	}
      }
    }
    session.logger.info("comment crawl complete")
    session.logger.info(commentCount + " comments added")
  }

  def writeFile(url: String, dir: File){
    val file = new File(dir, url.split("/").last)
    new ImageDownload(url, file, session.client)
  }
}

object Crawl extends App{
  val crawl = new Crawl
  crawl.crawlImages
  crawl.updateComments
}
