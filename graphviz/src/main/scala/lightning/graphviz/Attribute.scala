package lightning.renderer.graphviz

case class Attribute(name: String, value: String) {
  override def toString() = s"$name=$value"
}