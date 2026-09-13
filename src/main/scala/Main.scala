

object Main extends App{

  val config = FilesConfig.loadConfigOrThrow("FilesProperties")
  //val processor = new Processor
  private val processor = new ProcessorGen

 processor.run(config)


}
