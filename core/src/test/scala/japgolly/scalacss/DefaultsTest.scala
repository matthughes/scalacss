package japgolly.scalacss

import utest._
import TestUtil._

object DefaultsTest extends TestSuite {

  object DevDefaults extends Defaults {
    override def devMode = true
  }

  object ProdDefaults extends Defaults {
    override def devMode = false
  }

  override val tests = TestSuite {
    'dev  - Dev .test()
    'prod - Prod.test()
  }

  class SharedStyleModule(implicit reg: mutable.Register) extends mutable.StyleSheet.Inline {
    import dsl._
    implicit def compose = Compose.trust
    val header = style(backgroundColor("#333"))
    val footer = style(backgroundColor("#666"))
  }

  // ===================================================================================================================
  object Dev {
    import DevDefaults._

    object SS extends StyleSheet.Inline {
      import dsl._
      val style1 = style(
        margin(12 px),
        marginLeft(6 px)
      )
      val style2 = style(
        cursor.pointer,
        cursor.zoomIn
      )
      val shared = new SharedStyleModule
    }

    implicit def env = Env.empty
    val css = SS.render

    def norm(css: String) = css.trim

    def test(): Unit =
      assertEq(norm(css), norm(
        """
          |.scalacss-0001 {
          |  margin: 12px;
          |  margin-left: 6px;
          |}
          |
          |.scalacss-0002 {
          |  cursor: pointer;
          |  cursor: -webkit-zoom-in;
          |  cursor: -moz-zoom-in;
          |  cursor: -o-zoom-in;
          |  cursor: zoom-in;
          |}
          |
          |.scalacss-0003 {
          |  background-color: #333;
          |}
          |
          |.scalacss-0004 {
          |  background-color: #666;
          |}
        """.stripMargin))
  }

  // ===================================================================================================================
  object Prod {
    import ProdDefaults._

    object SS extends StyleSheet.Inline {
      import dsl._
      val style1 = style(
        margin(12 px),
        marginLeft(6 px)
      )
      val style2 = style(
        cursor.pointer,
        cursor.zoomIn
      )
      val shared = new SharedStyleModule
    }

    implicit def env = Env.empty
    val css = SS.render

    def test(): Unit =
      assertEq(css,
        "._0{margin:12px;margin-left:6px}" +
        "._1{cursor:pointer;cursor:-webkit-zoom-in;cursor:-moz-zoom-in;cursor:-o-zoom-in;cursor:zoom-in}" +
        "._2{background-color:#333}" +
        "._3{background-color:#666}"
      )
  }
}
