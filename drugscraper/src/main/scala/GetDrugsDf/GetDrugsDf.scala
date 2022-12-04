package GetDrugsDf

import org.apache.spark.sql.{SparkSession, Dataset, Encoder, DataFrame}
import DrugScrape.DrugTestRow

object getDrugsDf {
  lazy val allDrugs: Seq[DrugTestRow] = DrugScrape.getAllDrugTests
  val spark =
    SparkSession.builder.appName("GetDrugsDf").master("local[*]").getOrCreate()

  def getDirtyDf =
    import spark.implicits.localSeqToDatasetHolder
    import scala3encoders.given
    extension [T: Encoder](seq: Seq[T])
      def toDS: Dataset[T] =
        localSeqToDatasetHolder(seq).toDS
    val ds: Dataset[DrugTestRow] = allDrugs.toDS

}
