import zio.test._

object MySpec extends ZIOSpecDefault {
  override def spec: Spec[Any, Any] = suite("MySpec")(
    test("example test that succeeds") {
      val obtained = 42
      val expected = 42
      assertTrue(obtained == expected)
    }
  )
}
