instacq
=======
a zero-frills instagram acquisition tool

prerequisites 
-------------

* postgresql database (9.1 or greater)
* Java (7 or greater)
* SBT (tested on v0.13.5)
* An instagram client_id


set up
------
1. Create a new database schema in Postgres for Instacq
2. cd into the Instacq directory
3. Copy src/main/resources/application.conf_template /src/main/resources/application.conf.
4. Edit src/main/resources/application.conf, insert your client_id, data_dir(where you want images stored), and postgres connection info.
5. Edit src/main/resources/logback.xml, insert the location where Instacq should write its log file.
6. Initialize the database system by running sbt and selecting the initDB class from the list:
> $ sbt run <br />
>  [sbt info]<br />
> Multiple main classes detected, select one to run:<br /><br />
>  [1] edu.nyu.dlts.instag.initDB<br />
>  [2] edu.nyu.dlts.instag.AddAccountToDB<br />
>  [3] edu.nyu.dlts.instag.Crawl<br /><br />
> Enter number: 1<br /><br />

7. Add an instagram user to the system:
> $ sbt run <br />
>  [sbt info]<br />
> Multiple main classes detected, select one to run:<br /><br />
>  [1] edu.nyu.dlts.instag.initDB<br />
>  [2] edu.nyu.dlts.instag.AddAccountToDB<br />
>  [3] edu.nyu.dlts.instag.Crawl<br /><br />
> Enter number: 2<br /><br />
>[info] Running edu.nyu.dlts.instag.AddAccountToDB<br />
>enter name to search for: mennerich<br />
>1       Don Mennerich [mennerich]<br />
>enter the number of the account to add: 1<br />
>[success] Total time: 19 s, completed Jun 18, 2014 1:07:33 AM<br /><br />

8. Run a crawl:
> $ sbt run <br />
>  [sbt info]<br />
> Multiple main classes detected, select one to run:<br /><br />
>  [1] edu.nyu.dlts.instag.initDB<br />
>  [2] edu.nyu.dlts.instag.AddAccountToDB<br />
>  [3] edu.nyu.dlts.instag.Crawl<br /><br />
> Enter number: 3<br /><br />
