package pages.$packageName$
import models.LitresInBands
import pages.behaviours.PageBehaviours

class HowMany$className$PageSpec extends PageBehaviours {

  "HowMany$className$Page" - {

    beRetrievable[LitresInBands](HowMany$className$Page)

    beSettable[LitresInBands](HowMany$className$Page)

    beRemovable[LitresInBands](HowMany$className$Page)
  }
}
