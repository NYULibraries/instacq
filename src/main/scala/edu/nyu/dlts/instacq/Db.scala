package edu.nyu.dlts.instacq

import com.typesafe.config.Config
import java.util.{UUID, Date}
import java.sql.Timestamp
import scala.slick.driver.PostgresDriver.simple._
import scala.collection.mutable.{MutableList, Map}

class Db(conf: Config){
  val connection = Database.forURL((conf.getString("db.url") + ":" + conf.getString("db.port") + "/" + conf.getString("db.name")), driver = "org.postgresql.Driver", user = conf.getString("db.user"), password = conf.getString("db.pass"))	

  val accounts = TableQuery[Accounts]
  val crawls = TableQuery[Crawls]
  val images = TableQuery[Images]
  val comments = TableQuery[Comments]

  class Accounts(tag: Tag) extends Table[(UUID, String, String, String)](tag, "ACCOUNTS"){
    def id = column[UUID]("ID", O.PrimaryKey)
    def userId = column[String]("USER_ID")
    def userName = column[String]("USER_NAME")
    def userFullName = column[String]("USER_FULL_NAME")
    def * = (id, userId, userName, userFullName)  	
  }

  class Crawls(tag: Tag) extends Table[(UUID, UUID, Timestamp)](tag, "CRAWLS"){
    def id = column[UUID]("ID", O.PrimaryKey)
    def accountId = column[UUID]("ACCOUNT_ID")
    def crawlDate = column[Timestamp]("CRAWL_DATE")
    def * = (id, accountId, crawlDate)
    def account = foreignKey("ACC_FK", accountId, accounts)(_.id)
  }

  class Comments(tag: Tag) extends Table[(UUID, UUID, String, Timestamp, String, String, String, String)](tag, "COMMENTS"){
    def id = column[UUID]("ID", O.PrimaryKey)
    def imageId = column[UUID]("IMAGE_ID")
    def commentId = column[String]("COMMENT_ID")
    def createdDate = column[Timestamp]("CREATED_DATE")
    def comment = column[String]("COMMENT", O.DBType("varchar(4000)"))
    def userId = column[String]("USER_ID")
    def userName = column[String]("USER_NAME")
    def userFullName = column[String]("USER_FULL_NAME")
    def * = (id, imageId, commentId, createdDate, comment, userId, userName, userFullName)
    def image = foreignKey("IMG_FK", imageId, images)(_.id)
  }

  class Images(tag:Tag) extends Table[(UUID, UUID, UUID, String, String, String, Timestamp, Boolean)](tag, "IMAGES"){
    def id = column[UUID]("ID", O.PrimaryKey)
    def accountId = column[UUID]("ACCOUNT_ID")
    def crawlId = column[UUID]("CRAWL_ID")
    def mediaId = column[String]("MEDIA_ID")
    def imageUrl = column[String]("STANDARD_URL")
    def caption = column[String]("CAPTION", O.DBType("varchar(4000)"))
    def createdDate = column[Timestamp]("CREATED_DATE")
    def isDownloaded = column[Boolean]("IS_DOWNLOADED")  
    def * = (id, accountId, crawlId, mediaId, imageUrl, caption, createdDate, isDownloaded)
    def account = foreignKey("ACC_FK", accountId, accounts)(_.id)
    def crawl = foreignKey("CRL_FK", crawlId, crawls)(_.id)
  }

  //comment functions
  def addComment(map: Map[String, String]){
    connection.withSession{implicit session =>
      comments += (
        UUID.randomUUID,
        UUID.fromString(map("imageId")), 
        map("commentId"),
        timestamp(map("createdTime")),
        map("text"),
        map("userId"),
        map("username"),
        map("userFullName")
      )
    }
  }

    def getCommentIds(): MutableList[String] = {
    val ids = new MutableList[String]
    connection.withSession{implicit session =>
      val q = for(c <- comments) yield c.commentId
      q.foreach{id => ids += id}
    }
    ids
  }

  //acount functions
  def addAccount(map: Map[String, String]){
    connection.withSession{ implicit session =>
      accounts += (UUID.randomUUID, map("id"), map("username"), map("fullName"))
    }
  }

  def getAccountIds(): MutableList[UUID] ={
    val uuids = new MutableList[UUID]

    connection.withSession{ implicit session =>
      val query = for(a <- accounts) yield a.id
      query foreach{q => uuids += q}    
    }
    uuids
  }

    def getAccounts(): MutableList[Map[String, String]] = {
    val list = new MutableList[Map[String, String]]

    connection.withSession{ implicit session =>
      accounts foreach{ case (id, userId, userName, userFullName) =>
        val map = Map.empty[String, String]
        map("id") = id.toString
        map("userId") = userId
        map("userName") = userName
        list += map
      }
    }

    list
  }

  //image functions
  def addImage(map: Map[String, String]){
    connection.withSession{implicit session =>
      images += (
        UUID.randomUUID, 
        UUID.fromString(map("accountId")),
        UUID.fromString(map("crawlId")),
        map("mediaId"),
        map("imageUrl"),
        map("caption"),
        timestamp(map("createdTime")),
        true
      )
    }
  }

  def getImageIds(): MutableList[String] = {
    val ids = new MutableList[String]
    connection.withSession{implicit session =>
      val q = for(i <- images) yield i.mediaId
      q foreach{q => ids += q}
    }
    ids
  }
  
  def getAllImageIds(): Map[UUID, String] = {
    val map = Map.empty[UUID, String]
    connection.withSession{implicit session => 
      val q = for(i <- images) yield (i.id, i.mediaId)
      for((i,j) <- q) map(i) = j    
    }
    map
  }

  def getImageIds(num: Int): Map[UUID, String] = {
    val map = Map.empty[UUID, String]
    connection.withSession{implicit session => 
      val q = for(i <- images.take(num)) yield (i.id, i.mediaId)
      for((i,j) <- q) map(i) = j    
    }
    map
  }

  //db functions
  def createTables(){
    connection.withSession{implicit session =>
      (accounts.ddl ++ crawls.ddl ++ images.ddl ++ comments.ddl).create
    }
  }

  def dropTables(){
    connection.withSession{implicit session =>
      (accounts.ddl ++ crawls.ddl ++ images.ddl ++ comments.ddl).drop
    }
  }

  //crawl functions
  def addCrawl(crawlId: UUID, uId: UUID){
    connection.withSession{ implicit session =>
      crawls += (crawlId, uId, timestamp)
    }
  }

  def getCrawls(): List[Map[String,String]] = {
    val cids = new MutableList[Map[String,String]]
    connection.withSession{ implicit session =>
      crawls.foreach{c =>
	val map = Map.empty[String, String]
	map("id") = c._1.toString
	map("date") = c._3.toString
	map("images") = images.filter(_.crawlId === c._1).list.size.toString
	
	val q = for{
	  co <- comments
	  i <- images 
	  if co.imageId === i.id && i.crawlId === c._1 
 	} yield (co.id)
	
	map("comments") = q.list.size.toString
	cids += map
      }
    }
    cids.toList
  }

  def deleteCrawl(id: UUID): Unit = {
    connection.withSession{ implicit session =>
      images.filter(_.id === id).delete
    }
  }
  //misc functions 
  def timestamp(): Timestamp = {new Timestamp(new Date().getTime())}
  def timestamp(time: String): Timestamp = {new Timestamp(new Date(time.toLong * 1000l).getTime)}
}
