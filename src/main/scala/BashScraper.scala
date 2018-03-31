import java.io.{FileNotFoundException, FileWriter}

import com.typesafe.config.ConfigFactory
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.collection.parallel.ParSeq
import scala.io.Source
import scala.util.{Failure, Success, Try}

object BashScraper {

  def getBashLatestPosts(pagesCount: Int): ParSeq[BashEntry] = {
    (1 to pagesCount).par.flatMap { pageNr =>
      val bashUrl = s"http://bash.org.pl/latest/?page=$pageNr"
      val html = Try(Source.fromURL(bashUrl).mkString)
      html match {
        case Success(x) => parseHtml(x)
        case Failure(ex: FileNotFoundException) =>
          println(s"Page $bashUrl does not exist"); ParSeq.empty
        case Failure(ex) => throw ex
      }
    }
  }

  private def parseHtml(html: String): ParSeq[BashEntry] = {
    val parsed = Jsoup.parse(html)
    val posts = parsed.select(".q.post")
    posts.asScala.map { post =>
      BashEntry(
        post.attr("id").substring(1).toLong,
        post.select(".points").text.toLong,
        post.select("div.quote.post-content.post-body").text
      )
    }.par
  }

  def createJson(entries: ParSeq[BashEntry]): String = {
    entries.map { entry =>
      s"""{
          "id": ${entry.id},
          "points": ${entry.points},
          "content": ${entry.content}
}"""
    }.mkString("[", ",", "]")
  }

  def writeToFile(entries: String): Unit = {
    val fileLocation = ConfigFactory.load().getString("outputFileLocation")
    val fw = new FileWriter(fileLocation, true)
    fw.write(entries)
    fw.close()
    println(s"You file was saved to: $fileLocation")
  }

  def printStats(postsCount: Int, pagesCount: Int, startNanoTime: Long, endNanoTime: Long): Unit = {
    println(s"Number of collected posts: $postsCount")
    val downloadingTotalTime = (endNanoTime - startNanoTime) / Math.pow(10, 9)
    println(s"Average time of downloading a page: ${downloadingTotalTime / pagesCount} seconds")
    println(s"Average time of downloading a post: ${downloadingTotalTime / postsCount} seconds")
  }

}

case class BashEntry(id: Long, points: Long, content: String)

