package edu.nyu.dlts.instacq

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

import java.util.ArrayList
import java.util.List

import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils

class ImageDownload(url: String, file: java.io.File, client: CloseableHttpClient){
  val get = new HttpGet(url)
  val response = client.execute(get)
  val entity = response.getEntity
  val is = new BufferedInputStream(entity.getContent)
  val fos = new FileOutputStream(file)
  val buffer = new Array[Byte](1024)
  var len = 0

  while(len != -1){
    len = is.read(buffer)
    if(len != -1) fos.write(buffer,0,len)
  }

  EntityUtils.consume(entity)
  response.close
  fos.close
  is.close
}

