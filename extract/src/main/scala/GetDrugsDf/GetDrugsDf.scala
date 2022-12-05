package GetDrugsDf

import org.apache.spark.sql.{SparkSession, Dataset, Encoder, DataFrame, Row}
import org.apache.spark.sql.types.{
  StructType,
  StructField,
  StringType,
  ArrayType
}

object GetDrugsDf {
  val spark =
    SparkSession
      .builder()
      .appName("GetDrugsDf")
      .master("local[1]")
      .getOrCreate()

  lazy val allDrugs =
    spark.sparkContext.parallelize(DrugScrape.getAllDrugTests.toSeq)

  def writeDirtyDfToParquet(path: String) =
    val dirtyDf = getDirtyDf
    dirtyDf.write.mode("overwrite").parquet(path)

  private def getDirtyDf: DataFrame =
    spark.createDataFrame(allDrugs, getDrugsDfSchema)

  private def getDrugsDfSchema: StructType = StructType(
    Array(
      StructField("id", StringType),
      StructField("soldAs", StringType),
      StructField("sampleName", StringType),
      StructField("substances", ArrayType(StringType)),
      StructField("amounts", ArrayType(StringType)),
      StructField("testDate", StringType),
      StructField("srcLocation", StringType),
      StructField("submitterLocation", StringType),
      StructField("colour", StringType),
      StructField("size", StringType)
    )
  )

}
