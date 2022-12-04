import scalaz.MonadState.Get
@main def hello: Unit =
  GetDrugsDf.GetDrugsDf.writeDirtyDfToParquet(
    "../data/extract_output.parquet"
  )
