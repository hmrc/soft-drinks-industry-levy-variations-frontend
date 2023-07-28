package controllers

class SelectChangeControllerISpec extends ControllerITTestHelper {

  val route = "/select-change"

  s"GET $route" - {
    "should render the select change page" - {
      "that includes the option to correct a return" - {
        "when the user has variable returns" in {
        }
      }

      "that does not include the option to correct a return" - {
        "when the user has no variable returns" in {

        }
      }
    }
  }

}
