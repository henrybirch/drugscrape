import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog._

object DrugScrape {
  lazy val browser = JsoupBrowser()
  val drugsDataRootUrl = "https://www.drugsdata.org/"
  val allTestsUrl =
    drugsDataRootUrl + "index.php?sort=DatePublishedU+desc&start=0&a=&search_field=-&m1=-1&m2=-1&sold_as_ecstasy=both&datefield=tested&max=15000"

  lazy val doc = browser.get(allTestsUrl)

  def getAllDrugTests: Seq[Map[String, Any]] =
    val mainTable = doc >> element("#MainResults") >> element("tbody")
    mainTable.children.map(getTableRow).toSeq

  def getTableRow(rowElement: scalascraper.model.Element): Map[String, Any] =
    val sampleNameElement = rowElement >> element(".sample-name")
    val singleDrugTestPage =
      browser.get(getDrugTestUrl(sampleNameElement))
    val tbody =
      singleDrugTestPage >> element(".DetailsModule") >> element(
        ".TabletDataRight"
      ) >> element("tbody")

    Map(
      "soldAs" -> (sampleNameElement >> element(".sold-as")).text,
      "sampleName" -> (sampleNameElement >> element("a")).text,
      "substances" -> (for li <- (rowElement >> element(".Substance")).children
      yield li.text),
      "amounts" -> (for li <- (rowElement >> element(".Amounts")).children
      yield li.text),
      "testDate" -> tbody.select(":eq(1)").head.text,
      "srcLocation" -> getDrugTestTbodyAttribute(tbody, 2),
      "submitterLocation" -> getDrugTestTbodyAttribute(tbody, 3),
      "colour" -> getDrugTestTbodyAttribute(tbody, 4),
      "size" -> getDrugTestTbodyAttribute(tbody, 5)
    )

  private def getDrugTestUrl(
      sampleNameElementInRow: scalascraper.model.Element
  ): String =
    drugsDataRootUrl + (sampleNameElementInRow >> element("a")).attr("href")

  private def getDrugTestTbodyAttribute(
      tbody: scalascraper.model.Element,
      index: Int
  ): String =
    tbody.select(s":eq($index)").head.children.tail.head.text

}
