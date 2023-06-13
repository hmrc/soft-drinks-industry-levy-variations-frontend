package viewmodels.summary.updateRegisteredDetails

import controllers.updateRegisteredDetails.routes
import models.{CheckMode, UserAnswers}
import pages.updateRegisteredDetails.PackingSiteDetailsRemovePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PackingSiteDetailsRemoveSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PackingSiteDetailsRemovePage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "updateRegisteredDetails.packingSiteDetailsRemove.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.PackingSiteDetailsRemoveController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("updateRegisteredDetails.packingSiteDetailsRemove.change.hidden"))
          )
        )
    }
}
