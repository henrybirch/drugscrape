import scalaz.MonadState.Get
@main def extract: Unit =
  GetDrugsDf.GetDrugsDf.writeDirtyDfToParquet(
    "../data/extract_output.parquet"
  )
