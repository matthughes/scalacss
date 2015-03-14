package japgolly.scalacss

import shapeless.Witness
import Style.{UnsafeExt, UnsafeExts}

object DSL {

  val zeroType = Witness(0)

  // -------------------------------------------------------------------------------------------------------------------
  // Implicits

  @inline implicit def ValueZero(z: zeroType.T): String = "0"

  @inline implicit final class DslInt(val self: Int) extends AnyVal {
    @inline private def unit(u: String): Value = self.toString + u

    /** Centimeters. */
    @inline def cm = unit("cm")

    /**
     * This unit represents the width, or more precisely the advance measure, of the glyph '0' (zero, the Unicode
     * character U+0030) in the element's font.
     */
    @inline def ch = unit("ch")

    /**
     * This unit represents the calculated font-size of the element.
     * If used on the `font-size` property itself, it represents the inherited `font-size` of the element.
     */
    @inline def em = unit("em")

    /**
     * This unit represents the x-height of the element's font.
     * On fonts with the 'x' letter, this is generally the height of lowercase letters in the font;
     * 1ex ≈ 0.5em in many fonts.
     */
    @inline def ex = unit("ex")

    /** Inches (1in = 2.54 cm). */
    @inline def inches = unit("inches")

    /** Millimeters. */
    @inline def mm = unit("mm")

    /** Picas (1pc = 12pt). */
    @inline def pc = unit("pc")

    /** Points (1pt = 1/72 of 1in). */
    @inline def pt = unit("pt")

    /**
     * Pixel. Relative to the viewing device.
     * For screen display, typically one device pixel (dot) of the display.
     * For printers and very high resolution screens one CSS pixel implies multiple device pixels, so that the number
     * of pixel per inch stays around 96.
     */
    @inline def px = unit("px")

    /**
     * This unit represents the `font-size` of the root element (e.g. the `font-size` of the `&lt;html&gt;` element).
     * When used on the `font-size` on this root element, it represents its initial value.
     */
    @inline def rem = unit("rem")

    /** 1/100th of the height of the viewport. */
    @inline def vh = unit("vh")

    /** 1/100th of the minimum value between the height and the width of the viewport. */
    @inline def vmin = unit("vmin")

    /** 1/100th of the maximum value between the height and the width of the viewport. */
    @inline def vmax = unit("vmax")

    /** 1/100th of the width of the viewport. */
    @inline def vw = unit("vw")

    /** Size as a percentage. */
    @inline def pct = unit("%")
  }

  @inline implicit final class DslAttr(val self: Attr) extends AnyVal {
    @inline def ~(value: Value): AV = AV(self, value)
  }

  @inline implicit final class DslAV(val self: AV) extends AnyVal {
    @inline def &(b: AV) : AVs = NonEmptyVector(self, b)
    @inline def &(b: AVs): AVs = self +: b
  }

  @inline implicit final class DslAVs(val self: AVs) extends AnyVal {
    @inline def &(b: AV) : AVs = self :+ b
    @inline def &(b: AVs): AVs = self ++ b
  }

  final class DslCond(val c: Cond) extends AnyVal {
    @inline def apply()             : ToStyle = new ToStyle(StyleS.empty)
    @inline def apply(h: AV, t: AV*): ToStyle = apply(NonEmptyVector(h, t: _*))
    @inline def apply(avs: AVs)     : ToStyle = (c, avs)
  }
  @inline implicit def DslCond[C <% Cond](x: C): DslCond = new DslCond(x)

  @inline implicit def CondPseudo(x: Pseudo): Cond = Cond(Some(x))

  final class ToStyle(val s: StyleS) extends AnyVal
  @inline implicit def ToStyleAV             (x: AV)        : ToStyle = ToStyleAVs(NonEmptyVector(x))
          implicit def ToStyleAVs            (x: AVs)       : ToStyle = new ToStyle(StyleS.data1(Cond.empty, x))
          implicit def ToStyleCAV [C <% Cond](x: (C, AV))   : ToStyle = new ToStyle(StyleS.data1(x._1, NonEmptyVector(x._2)))
          implicit def ToStyleCAVs[C <% Cond](x: (C, AVs))  : ToStyle = new ToStyle(StyleS.data1(x._1, x._2))
  @inline implicit def ToStyleUnsafeExt      (x: UnsafeExt) : ToStyle = ToStyleUnsafeExts(Vector1(x))
          implicit def ToStyleUnsafeExts     (x: UnsafeExts): ToStyle = new ToStyle(StyleS.empty.copy(unsafeExts = x))
  @inline implicit def ToStyleStyleS         (x: StyleS)    : ToStyle = new ToStyle(x)

  // -------------------------------------------------------------------------------------------------------------------
  // Explicits

  def style(className: String = null)(t: ToStyle*)(implicit c: Compose): StyleS =
    style(t: _*).copy(className = Option(className))

  def style(t: ToStyle*)(implicit c: Compose): StyleS =
    if (t.isEmpty) StyleS.empty
    else t.map(_.s).reduce(_ compose _)

  def unsafeExt(f: String => String)(t: ToStyle*)(implicit c: Compose): UnsafeExt =
    UnsafeExt(f, style(t: _*))

  def unsafeChild(n: String)(t: ToStyle*)(implicit c: Compose): Style.UnsafeExt =
    unsafeExt(_ + " " + n)(t: _*)
}