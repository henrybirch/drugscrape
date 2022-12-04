import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog._

val browser = JsoupBrowser()
val doc = browser.get(
  "https://www.drugsdata.org/index.php?sort=DatePublishedU+desc&start=5000a=&search_field=-&m1=-1&m2=-1&sold_as_ecstasy=both&datefield=tested&max=10"
)

val mainTable =
  doc >> element("#MainResults") >> element("tbody")

def getTableRow(
    rowElement: scalascraper.model.Element
): Map[String, Any] =
  val sampleNameElement = rowElement >> element(".sample-name")

  val testPageUrl =
    "https://www.drugsdata.org/" + (sampleNameElement >> element("a")).attr(
      "href"
    )
  val testPage = browser.get(testPageUrl)

  val detailsModule = testPage >> element(".DetailsModule")
  val tabletDataRight =
    testPage >> element(".DetailsModule") >> element(".TabletDataRight")

  val tbody = tabletDataRight >> element("tbody")

  Map(
    "soldAs" -> (sampleNameElement >> element(".sold-as")).text,
    "sampleName" -> (sampleNameElement >> element("a")).text,
    "substances" -> (for li <- (rowElement >> element(".Substance")).children
    yield li.text),
    "amounts" -> (for li <- (rowElement >> element(".Amounts")).children
    yield li.text),
    "testDate" -> tbody.select(":eq(1)").head.text,
    "srcLocation" -> tbody.select(":eq(2)").head.children.tail.head.text,
    "submitterLocation" -> tbody.select(":eq(3)").head.children.tail.head.text,
    "colour" -> tbody.select(":eq(4)").head.children.tail.head.text,
    "size" -> tbody.select(":eq(5)").head.children.tail.head.text
  )

val res = mainTable.children.map(getTableRow)
res.getClass
// println(res(0)("sampleName"))
res.head("soldAs")
