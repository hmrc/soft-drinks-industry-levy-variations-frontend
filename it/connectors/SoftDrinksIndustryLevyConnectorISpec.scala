package connectors

import cats.data.EitherT
import errors.{UnexpectedResponseFromSDIL, VariationsErrors}
import models.ReturnPeriod
import models.backend.{FinancialLineItem, OptPreviousSubmittedReturn, OptRetrievedSubscription, OptSmallProducer}
import org.scalatest.matchers.must.Matchers._
import repositories.SDILSessionKeys
import testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import uk.gov.hmrc.http.HeaderCarrier
import testSupport.SDILBackendTestData._

class SoftDrinksIndustryLevyConnectorISpec extends Specifications with TestConfiguration with ITCoreTestData {

  val sdilConnector = app.injector.instanceOf[SoftDrinksIndustryLevyConnector]
  implicit val hc: HeaderCarrier = new HeaderCarrier()

  "retrieveSubscription" - {
    "when the cache is empty" - {
      "and the backend call returns no subscription" - {
        "should return None" - {
          "when searching by utr" in {
            build
              .sdilBackend
              .retrieveSubscriptionNone("utr", UTR)

            val res = sdilConnector.retrieveSubscription(UTR, "utr")

            whenReady(res.value) { result =>
              result mustBe Right(None)
            }
          }

          "when searching by sdilRef" in {
            build
              .sdilBackend
              .retrieveSubscriptionNone("sdil", SDIL_REF)

            val res = sdilConnector.retrieveSubscription(SDIL_REF, "sdil")

            whenReady(res.value) { result =>
              result mustBe Right(None)
            }
          }
        }
      }

      "and the backend call returns a subscription" - {
        "should return the subscription" - {
          "when searching by utr" in {
            build
              .sdilBackend
              .retrieveSubscription("utr", UTR)

            val res = sdilConnector.retrieveSubscription(UTR, "utr")

            whenReady(res.value) { result =>
              result mustBe Right(Some(aSubscription))
            }
          }

          "when searching by sdilRef" in {
            build
              .sdilBackend
              .retrieveSubscription("sdil", SDIL_REF)

            val res = sdilConnector.retrieveSubscription(SDIL_REF, "sdil")

            whenReady(res.value) { result =>
              result mustBe Right(Some(aSubscription))
            }
          }
        }
      }

      "when the backend returns an internal error" - {
        "should return an UnexpectedResponseFromSDIL error" in {
          build
            .sdilBackend
            .retrieveSubscriptionError("sdil", SDIL_REF)

          val res = sdilConnector.retrieveSubscription(SDIL_REF, "sdil")

          whenReady(res.value) { result =>
            result mustBe Left(UnexpectedResponseFromSDIL)
          }
        }
      }
    }

    "when the cache is not empty" - {
      "should not make a backend call" - {
        "and return None when the cache has an empty subscription" - {
          "when searching by utr" in {
            val res = for {
              _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(None)))
              result <- sdilConnector.retrieveSubscription(UTR, "utr")
            } yield result

            whenReady(res.value) { result =>
              result mustBe Right(None)
            }
          }

          "when searching by sdilRef" in {
            val res = for {
              _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(SDIL_REF, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(None)))
              result <- sdilConnector.retrieveSubscription(SDIL_REF, "sdil")
            } yield result

            whenReady(res.value) { result =>
              result mustBe Right(None)
            }
          }
        }

        "and return the subscription when in the cache" - {
          "when searching by utr" in {
            val res = for {
              _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(Some(aSubscription))))
              result <- sdilConnector.retrieveSubscription(UTR, "utr")
            } yield result

            whenReady(res.value) { result =>
              result mustBe Right(Some(aSubscription))
            }
          }

          "when searching by sdilRef" in {
            val res = for {
              _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(SDIL_REF, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(Some(aSubscription))))
              result <- sdilConnector.retrieveSubscription(SDIL_REF, "sdil")
            } yield result

            whenReady(res.value) { result =>
              result mustBe Right(Some(aSubscription))
            }
          }
        }
      }
    }
  }

  "getPendingReturnsFromCache" - {
    "when the no pending returns in the cache" - {
      "should call the backend" - {
        "and return None when no pending returns" in {
          build
            .sdilBackend
            .no_returns_pending(UTR)

          val res = sdilConnector.getPendingReturnsFromCache(UTR)

          whenReady(res.value) { result =>
            result mustBe Right(List.empty)
          }
        }
        "and return the list of pending return when exist" in {
          build
            .sdilBackend
            .returns_pending(UTR)

          val res = sdilConnector.getPendingReturnsFromCache(UTR)

          whenReady(res.value) { result =>
            result mustBe Right(returnPeriods)
          }
        }

        "and return UnexpectedResponseFromSDIL when the backend returns an unexpectedResponse code" in {
          build
            .sdilBackend
            .returns_pending_error(UTR)

          val res = sdilConnector.getPendingReturnsFromCache(UTR)

          whenReady(res.value) { result =>
            result mustBe Left(UnexpectedResponseFromSDIL)
          }
        }
      }
    }

    "when a pending returns record is in the cache" - {
      "should read the value from the cache" - {
        "and return None when no pending returns" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.RETURNS_PENDING, List.empty[ReturnPeriod]))
            result <- sdilConnector.getPendingReturnsFromCache(UTR)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(List.empty)
          }
        }
        "and return the list of pending return when exist" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.RETURNS_PENDING, returnPeriods))
            result <- sdilConnector.getPendingReturnsFromCache(UTR)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(returnPeriods)
          }
        }
      }
    }
  }

  "getVariableReturnsFromCache" - {
    "when the no variable returns in the cache" - {
      "should call the backend" - {
        "and return None when no pending returns" in {
          build
            .sdilBackend
            .no_returns_variable(UTR)

          val res = sdilConnector.getVariableReturnsFromCache(UTR)

          whenReady(res.value) { result =>
            result mustBe Right(List.empty)
          }
        }
        "and return the list of variable return when exist" in {
          build
            .sdilBackend
            .returns_variable(UTR)

          val res = sdilConnector.getVariableReturnsFromCache(UTR)

          whenReady(res.value) { result =>
            result mustBe Right(returnPeriodList)
          }
        }

        "and return UnexpectedResponseFromSDIL when the backend returns an unexpectedResponse code" in {
          build
            .sdilBackend
            .returns_variable_error(UTR)

          val res = sdilConnector.getVariableReturnsFromCache(UTR)

          whenReady(res.value) { result =>
            result mustBe Left(UnexpectedResponseFromSDIL)
          }
        }
      }
    }

    "when a variable returns record is in the cache" - {
      "should read the value from the cache" - {
        "and return None when no pending returns" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.VARIABLE_RETURNS, List.empty[ReturnPeriod]))
            result <- sdilConnector.getVariableReturnsFromCache(UTR)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(List.empty)
          }
        }
        "and return the list of pending return when exist" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.VARIABLE_RETURNS, returnPeriods))
            result <- sdilConnector.getVariableReturnsFromCache(UTR)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(returnPeriods)
          }
        }
      }
    }
  }


  "getReturn" - {
    "when the no previous submitted returns in the cache" - {
      "should call the backend" - {
        "and return None when no previous return submitted" in {
          build
            .sdilBackend
            .retrieveReturn(UTR, currentReturnPeriod.previous, None)

          val res = sdilConnector.getReturn(UTR, currentReturnPeriod.previous)

          whenReady(res.value) { result =>
            result mustBe Right(None)
          }
        }
        "and return the sdil return when exists" in {
          build
            .sdilBackend
            .retrieveReturn(UTR, currentReturnPeriod.previous, Some(emptyReturn))

          val res = sdilConnector.getReturn(UTR, currentReturnPeriod.previous)

          whenReady(res.value) { result =>
            result mustBe Right(Some(emptyReturn))
          }
        }

        "and return UnexpectedResponseFromSDIL when the backend returns an unexpectedResponse code" in {
          build
            .sdilBackend
            .retrieveReturnError(UTR, currentReturnPeriod.previous)

          val res = sdilConnector.getReturn(UTR, currentReturnPeriod.previous)

          whenReady(res.value) { result =>
            result mustBe Left(UnexpectedResponseFromSDIL)
          }
        }
      }
    }

    "when a submitted returns record is in the cache for the build period" - {
      "should read the value from the cache" - {
        "and return None when no return submitted for period" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.previousSubmittedReturn(UTR, currentReturnPeriod.previous), OptPreviousSubmittedReturn(None)))
            result <- sdilConnector.getReturn(UTR, currentReturnPeriod.previous)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(None)
          }
        }
        "and return the submitted return when exist" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.previousSubmittedReturn(UTR, currentReturnPeriod.previous), OptPreviousSubmittedReturn(Some(emptyReturn))))
            result <- sdilConnector.getReturn(UTR, currentReturnPeriod.previous)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(Some(emptyReturn))
          }
        }
      }
    }
  }

  "balance" - {
    "when assessment is true" - {
      "and there is no balance in the cache" - {
        "should call the backend" - {
          "and return the balance when sucessful" in {
            build
              .sdilBackend
              .balance(aSubscription.sdilRef, withAssessment = true)

            val res = sdilConnector.balance(aSubscription.sdilRef, true)

            whenReady(res.value) { result =>
              result mustBe Right(BigDecimal(1000))
            }
          }
        }
      }
    }
    "when assessment is false" - {
      "and there is no balance in the cache" - {
        "should call the backend" - {
          "and return the balance when sucessful" in {
            build
              .sdilBackend
              .balance(aSubscription.sdilRef, false)

            val res = sdilConnector.balance(aSubscription.sdilRef, false)

            whenReady(res.value) { result =>
              result mustBe Right(BigDecimal(1000))
            }
          }
        }
      }
    }
  }

  "balanceHistory" - {
    "when assessment is true" - {
      "and there is no balanceHistory in the cache" - {
        "should call the backend" - {
          "and return the balanceHistory when sucessful" in {
            build
              .sdilBackend
              .balanceHistory(aSubscription.sdilRef, true, allFinicialItems)

            val res = sdilConnector.balanceHistory(aSubscription.sdilRef, true)

            whenReady(res.value) { result =>
              result mustBe Right(allFinicialItems)
            }
          }
        }
      }

      "and the balanceHistory is in the cache" - {
        "should return the balanceHistory" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save[List[FinancialLineItem]](SDIL_REF,
              SDILSessionKeys.balanceHistory(true), allFinicialItems))
            result <- sdilConnector.balanceHistory(aSubscription.sdilRef, true)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(allFinicialItems)
          }
        }
      }
    }
    "when assessment is false" - {
      "and there is no balanceHistory in the cache" - {
        "should call the backend" - {
          "and return an empty list when no history and when sucessful" in {
            build
              .sdilBackend
              .balanceHistory(aSubscription.sdilRef, false, List.empty)

            val res = sdilConnector.balanceHistory(aSubscription.sdilRef, false)

            whenReady(res.value) { result =>
              result mustBe Right(List.empty)
            }
          }
        }
      }

      "and the balanceHistory is in the cache" - {
        "should return the balanceHistory from the cache" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save[List[FinancialLineItem]](SDIL_REF, SDILSessionKeys.balanceHistory(false), allFinicialItems))
            result <- sdilConnector.balanceHistory(aSubscription.sdilRef, false)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(allFinicialItems)
          }
        }
      }
    }
  }

  "checkSmallProducerStatus" - {
    "when the cache doesn't contain the result for the build utr and period" - {
      "should call the backend" - {
        "and return true" - {
          "when the backend call returns true" in {
            build
              .sdilBackend
              .checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head, true)

            val res = sdilConnector.checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head)

            whenReady(res.value) { result =>
              result mustBe Right(Some(true))
            }
          }
        }

        "and return false" - {
          "when the backend call returns false" in {
            build
              .sdilBackend
              .checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head, false)

            val res = sdilConnector.checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head)

            whenReady(res.value) { result =>
              result mustBe Right(Some(false))
            }
          }
        }


        "and return None" - {
          "when the backend call returns 404" in {
            build
              .sdilBackend
              .checkSmallProducerStatusNone(aSubscription.sdilRef, returnPeriods.head)

            val res = sdilConnector.checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head)

            whenReady(res.value) { result =>
              result mustBe Right(None)
            }
          }
        }

        "and return UnexpectedResponseFromSDIL error" - {
          "when the backend call fails" in {
            build
              .sdilBackend
              .checkSmallProducerStatusError(aSubscription.sdilRef, returnPeriods.head)

            val res = sdilConnector.checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head)

            whenReady(res.value) { result =>
              result mustBe Left(UnexpectedResponseFromSDIL)
            }
          }
        }
      }
    }

    "when the cache contains the result for the build utr and period" - {
      "and return true" - {
        "when the cache returns true" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save[OptSmallProducer](SDIL_REF,
              SDILSessionKeys.smallProducerForPeriod(returnPeriods.head), OptSmallProducer(Some(true))))
            result <- sdilConnector.checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(Some(true))
          }
        }
      }

      "and return false" - {
        "when the cache returns false" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save[OptSmallProducer](SDIL_REF,
              SDILSessionKeys.smallProducerForPeriod(returnPeriods.head), OptSmallProducer(Some(false))))
            result <- sdilConnector.checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(Some(false))
          }
        }
      }


      "and return None" - {
        "when the cache returns None" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save[OptSmallProducer](SDIL_REF,
              SDILSessionKeys.smallProducerForPeriod(returnPeriods.head), OptSmallProducer(None)))
            result <- sdilConnector.checkSmallProducerStatus(aSubscription.sdilRef, returnPeriods.head)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(None)
          }
        }
      }
    }
  }
}
