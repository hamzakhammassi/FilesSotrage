import com.typesafe.config.{Config, ConfigFactory}
import pureconfig._
import pureconfig.generic.ProductHint

import pureconfig.generic.auto._

case class FilesConfig(workingDirectories: String)

object FilesConfig {
  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(identity))

  def loadConfigOrThrow(baseConf: String, config: Config = ConfigFactory.load()): FilesConfig =
    ConfigSource.fromConfig(config.getConfig(baseConf)).loadOrThrow[FilesConfig]
}
