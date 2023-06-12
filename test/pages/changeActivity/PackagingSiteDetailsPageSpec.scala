package pages.changeActivity

import pages.behaviours.PageBehaviours

class PackagingSiteDetailsPageSpec extends PageBehaviours {

  "PackagingSiteDetailsPage" - {

    beRetrievable[Boolean](PackagingSiteDetailsPage)

    beSettable[Boolean](PackagingSiteDetailsPage)

    beRemovable[Boolean](PackagingSiteDetailsPage)
  }
}
