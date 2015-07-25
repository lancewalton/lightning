package lightning.graphviz

case class Attribute(name: String, value: String) {
  override def toString() = s"$name=$value"
}