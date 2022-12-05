package GetDrugsDf
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
    drugsDataRootUrl + "index.php?sort=DatePublishedU+desc&start=0&a=&search_field=-&m1=-1&m2=-1&sold_as_ecstasy=both&datefield=tested&max=14712"

  lazy val doc = browser.get(allTestsUrl)

  def getAllDrugTests: Iterable[Row] =
    val mainTable = doc >> element("#MainResults") >> element("tbody")
    mainTable.children.map(getTableRow)

  def getTableRow(
      rowElement: scalascraper.model.Element
  ): Row =
    val sampleNameElement = (rowElement >> element(".sample-name"))

    val singleDrugTestPage = browser.get(getDrugTestUrl(sampleNameElement))

    val detailsModule = singleDrugTestPage >> element(".DetailsModule")

    val rightTbody =
      detailsModule >> element(
        ".TabletDataRight"
      ) >> element("tbody")

    val leftTbody = detailsModule >> element(
      ".TabletDataLeft"
    ) >> element("tbody")

    val id = leftTbody.select(":eq(1)").head.text

    val soldAs =
      (sampleNameElement >?> text(".sold-as")).getOrElse("")

    val sampleName =
      (sampleNameElement >?> text("a")).getOrElse("")

    val substances =
      (rowElement >?> texts(".Substance li")).getOrElse(List())

    val amounts = (rowElement >?> texts(".Amounts li")).getOrElse(List())

    val testDate = rightTbody.select(":eq(1)").head.text

    val srcLocation = getDrugTestTbodyAttribute(rightTbody, 2)

    val submitterLocation = getDrugTestTbodyAttribute(rightTbody, 3)

    val colour = getDrugTestTbodyAttribute(rightTbody, 4)

    val size = getDrugTestTbodyAttribute(rightTbody, 5)

    val row = Row(
      id,
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
    println(row)
    row

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
