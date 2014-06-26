package edu.nyu.dlts.instacq

import org.slf4j.LoggerFactory

class CreateTables{
  val logger = LoggerFactory.getLogger(classOf[CreateTables])
  val session = new Session
  session.db.createTables
  logger.info("Instacq database tables created")
  println("Instacq database tables created")
}

object CreateTables extends App{new CreateTables}

class DropTables{
  val logger = LoggerFactory.getLogger(classOf[DropTables])
  val session = new Session
  session.db.dropTables
  logger.info("Instacq database tables dropped")
  println("Instacq database tables dropped")
}

object DropTable extends App{new DropTables}

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
  println("added instagram account " + users(select.toInt)("id"))
}

object AddAccountToDB extends App{new AddAccountToDB}

object Report extends App{
  val session = new Session
  println("\n+--------------------------------------+-----------------------+------------+--------------+")
  println("|               Crawl                  |         Date          | num images | num comments |")
  println("+--------------------------------------+-----------------------+------------+--------------+")
  session.db.getCrawls.foreach{c =>
    println("| " + c("id") + " | " + c("date") + " |     " + c("images") + "    |     " + c("comments") + "     |")
  }
  println("+--------------------------------------+-----------------------+------------+--------------+\n")
}
