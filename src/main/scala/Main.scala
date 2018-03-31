object Main extends App {

  print("Enter the number of pages you want to download: ")
  val pages = scala.io.StdIn.readInt()

  val scrapingStartTime = System.nanoTime()
  val latestPosts = BashScraper.getBashLatestPosts(pages)
  val scrapingEndTime = System.nanoTime()
  val jsonWithPosts = BashScraper.createJson(latestPosts)
  BashScraper.writeToFile(jsonWithPosts)
  BashScraper.printStats(latestPosts.size, pages, scrapingStartTime, scrapingEndTime)

}