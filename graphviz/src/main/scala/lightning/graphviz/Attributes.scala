package lightning.renderer.graphviz

case class Attributes(list: List[Attribute] = Nil) {
  def +(attribute: Attribute): Attributes = new Attributes(attribute :: list)
  def +(attribute: Option[Attribute]): Attributes = attribute.fold(this) { this + _ }
  override def toString() = "[" + (list.reverse map { _.toString } mkString(", ")) + "]"
}