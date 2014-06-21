package edu.nyu.dlts.instacq

import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients
import java.util.UUID
import java.io.File
import org.slf4j.LoggerFactory


class Crawl(){

  val logger = LoggerFactory.getLogger(classOf[Crawl])
  val conf = ConfigFactory.load()
  val db = new Db(conf)
  val client = HttpClients.createDefault()
  val requests = new Requests(client, conf)
  val mediaIds = db.getImageIds
  var count = 0

  logger.info("Starting instacq crawl")

  db.getAccounts.foreach{account => 
    logger.info("new image crawl started")
    val userUUID = UUID.fromString(account("id"))
    val crawlUUID = UUID.randomUUID
    val crawlDir = new File(conf.getString("instag.data_dir"), crawlUUID.toString)
    db.addCrawl(crawlUUID, userUUID)    
    crawlDir.mkdir

    val images = requests.getImagesById(account("userId"))
    images.foreach{image =>
      if(! mediaIds.contains(image._1)){
        logger.info("adding image: " + image._1)
        count += 1
        val mediaMap = requests.getImageById(image._1)
	mediaMap("accountId") = account("id")
        mediaMap("crawlId") = crawlUUID.toString
        mediaMap("mediaId") = image._1
        mediaMap("imageUrl") = image._2		    
        writeFile(image._2, crawlDir)		    
        db.addImage(mediaMap)
        count += 1
      }
    }
    
    if(crawlDir.list().length == 0){
      crawlDir.delete
      db.deleteCrawl(crawlUUID)
    }
    logger.info("new image crawl complete")
  }

  var commentCount = 0
  val images = db.getAllImageIds
  val commentIds = db.getCommentIds()

  logger.info("new comment crawl started")
  images.foreach{image => 
    val imageUUID = image._1
    val imageId = image._2
    val comments = requests.getCommentsByMediaId(imageId)
    if(comments.size > 0){
      comments.foreach{comment =>
        if(! commentIds.contains(comment("commentId"))){
          logger.info("adding comment: " + comment("commentId"))
          comment("imageId") = imageUUID.toString
          db.addComment(comment)
          commentCount += 1
        }
      }
    }
  }
  logger.info("new comment crawl complete")
  logger.info("crawl complete, " + count + " images and " + commentCount + " comments added to collection")

  def writeFile(url: String, dir: File){
    val file = new File(dir, url.split("/").last)
    new ImageDownload(url, file, client)
  }
}

object Crawl extends App{new Crawl}
