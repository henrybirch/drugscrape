import scalaz.MonadState.Get
@main def hello: Unit =
  GetDrugsDf.GetDrugsDf.writeDirtyDfToParquet(
    "data/output.parquet"
  )
