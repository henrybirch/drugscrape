import org.apache.spark.sql.SparkSession
val spark =
  SparkSession
    .builder()
    .appName("GetDrugsDf")
    .master("local[1]")
    .getOrCreate()

spark.read.parquet("/tmp/output.parquet").head()
