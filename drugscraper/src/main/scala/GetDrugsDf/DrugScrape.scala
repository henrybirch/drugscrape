package GetDrugsDf
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

  case class DrugTestRow(
      soldAs: String,
      sampleName: String,
      substances: Iterable[String],
      amounts: Iterable[String],
      testDate: String,
      srcLocation: String,
      submitterLocation: String,
      colour: String,
      size: String
  )
  def getAllDrugTests: Seq[DrugTestRow] =
    val mainTable = doc >> element("#MainResults") >> element("tbody")
    mainTable.children.map(getTableRow).toSeq

  def getTableRow(
      rowElement: scalascraper.model.Element
  ): DrugTestRow =
    val sampleNameElement = rowElement >> element(".sample-name")
    val singleDrugTestPage =
      browser.get(getDrugTestUrl(sampleNameElement))
    val tbody =
      singleDrugTestPage >> element(".DetailsModule") >> element(
        ".TabletDataRight"
      ) >> element("tbody")

    val soldAs = (sampleNameElement >> element(".sold-as")).text
    val sampleName = (sampleNameElement >> element("a")).text
    val substances =
      (for li <- (rowElement >> element(".Substance")).children
      yield li.text)
    val amounts =
      (for li <- (rowElement >> element(".Amounts")).children
      yield li.text)
    val testDate = tbody.select(":eq(1)").head.text
    val srcLocation = getDrugTestTbodyAttribute(tbody, 2)
    val submitterLocation = getDrugTestTbodyAttribute(tbody, 3)
    val colour = getDrugTestTbodyAttribute(tbody, 4)
    val size = getDrugTestTbodyAttribute(tbody, 5)

    DrugTestRow(
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
    drugsDataRootUrl + (sampleNameElementInRow >> element("a")).attr("href")

  private def getDrugTestTbodyAttribute(
      tbody: scalascraper.model.Element,
      index: Int
  ): String =
    tbody.select(s":eq($index)").head.children.tail.head.text

}
