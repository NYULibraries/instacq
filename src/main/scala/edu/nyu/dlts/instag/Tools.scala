package edu.nyu.dlts.instag
import org.slf4j.LoggerFactory

object initDB{
  def main(args: Array[String]){
    val session = new Session
    session.db.createTables
  }
}


class AddAccountToDB{
  val logger = LoggerFactory.getLogger(classOf[AddAccountToDB])
  val session = new Session
  val in = readLine("enter name to search for: ")
  val users = session.requests.findUserByUsername(in)
  
  users.toList.sortBy(_._1).foreach{user =>
    println(user._1 + "\t" + user._2("fullName") + " [" + user._2("username") + "]")
  }
  
  val select = readLine("enter the number of the account to add: ")
  session.db.addAccount(users(select.toInt))
  logger.info("added instagram account " + users(select.toInt)("id"))
}

object AddAccountToDB extends App{new AddAccountToDB}
