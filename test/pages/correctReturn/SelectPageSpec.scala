package pages.correctReturn

import models.correctReturn.Select
import pages.behaviours.PageBehaviours

class SelectSpec extends PageBehaviours {

  "SelectPage" - {

    beRetrievable[Select](SelectPage)

    beSettable[Select](SelectPage)

    beRemovable[Select](SelectPage)
  }
}
