package pages.updateRegisteredDetails

import pages.behaviours.PageBehaviours

class PackingSiteDetailsRemovePageSpec extends PageBehaviours {

  "PackingSiteDetailsRemovePage" - {

    beRetrievable[Boolean](PackingSiteDetailsRemovePage)

    beSettable[Boolean](PackingSiteDetailsRemovePage)

    beRemovable[Boolean](PackingSiteDetailsRemovePage)
  }
}
