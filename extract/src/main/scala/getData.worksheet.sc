import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog._
import org.apache.spark.sql.Row
import org.jsoup.Connection

object DrugScrape {
  lazy val browser = new JsoupBrowser {
    override def requestSettings(conn: Connection): Connection =
      conn.timeout(30 * 60000)
  }
  val drugsDataRootUrl = "https://www.drugsdata.org/"
  val allTestsUrl =
    drugsDataRootUrl + "index.php?sort=DatePublishedU+desc&start=0&a=&search_field=-&m1=-1&m2=-1&sold_as_ecstasy=both&datefield=tested&max=2000"

  lazy val doc = browser.get(allTestsUrl)

  def getAllDrugTests: Iterable[Row] =
    val mainTable = doc >> element("#MainResults") >> element("tbody")
    mainTable.children.map(getTableRow)

  def getTableRow(
      rowElement: scalascraper.model.Element
  ): Row =
    val sampleNameElement = (rowElement >> element(".sample-name"))

    val singleDrugTestPage =
      browser.get(getDrugTestUrl(sampleNameElement))

    val tbody =
      singleDrugTestPage >> element(".DetailsModule") >> element(
        ".TabletDataRight"
      ) >> element("tbody")

    val soldAs =
      (sampleNameElement >?> text(".sold-as")).getOrElse("")
    val sampleName: String =
      (sampleNameElement >?> element("a")).map(_.text).getOrElse("")
    val substances = (rowElement >?> texts(".Substance")).getOrElse(List())
    val amounts = (rowElement >?> texts(".Amounts")).getOrElse(List())
    val testDate: String = tbody.select(":eq(1)").head.text
    val srcLocation: String = getDrugTestTbodyAttribute(tbody, 2)
    val submitterLocation: String = getDrugTestTbodyAttribute(tbody, 3)
    val colour: String = getDrugTestTbodyAttribute(tbody, 4)
    val size: String = getDrugTestTbodyAttribute(tbody, 5)

    Row(
      soldAs,
      sampleName,
      substances,
      amounts,
      testDate,
      srcLocation,
      submitterLocation,
      colour,
      size
    )

  private def getDrugTestUrl(
      sampleNameElementInRow: scalascraper.model.Element
  ): String =
    drugsDataRootUrl + (sampleNameElementInRow >> element("a"))
      .attr("href")

  private def getDrugTestTbodyAttribute(
      tbody: scalascraper.model.Element,
      index: Int
  ): String =
    tbody.select(s":eq($index)").head.children.tail.head.text

}

DrugScrape.getAllDrugTests
