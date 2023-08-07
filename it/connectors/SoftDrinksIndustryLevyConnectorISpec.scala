package connectors

import cats.data.EitherT
import errors.{UnexpectedResponseFromSDIL, VariationsErrors}
import models.{FinancialLineItem, OptPreviousSubmittedReturn, OptRetrievedSubscription, ReturnPeriod}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import repositories.SDILSessionKeys
import testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import uk.gov.hmrc.http.HeaderCarrier
import testSupport.SDILBackendTestData._

class SoftDrinksIndustryLevyConnectorISpec extends Specifications with TestConfiguration with ITCoreTestData{

  val sdilConnector = app.injector.instanceOf[SoftDrinksIndustryLevyConnector]
  implicit val hc = new HeaderCarrier()

  "retrieveSubscription" - {
    "when the cache is empty" - {
      "and the backend call returns no subscription" - {
        "should return None" - {
          "when searching by utr" in {
            given
              .sdilBackend
              .retrieveSubscriptionNone("utr", UTR)

            val res = sdilConnector.retrieveSubscription(UTR, "utr")

            whenReady(res.value) { result =>
              result mustBe Right(None)
            }
          }

          "when searching by sdilRef" in {
            given
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
            given
              .sdilBackend
              .retrieveSubscription("utr", UTR)

            val res = sdilConnector.retrieveSubscription(UTR, "utr")

            whenReady(res.value) { result =>
              result mustBe Right(Some(aSubscription))
            }
          }

          "when searching by sdilRef" in {
            given
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
          given
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

  "returns_pending" - {
    "when the no pending returns in the cache" - {
      "should call the backend" - {
        "and return None when no pending returns" in {
          given
            .sdilBackend
            .no_returns_pending(UTR)

          val res = sdilConnector.returnsPending(UTR)

          whenReady(res.value) { result =>
            result mustBe Right(List.empty)
          }
        }
        "and return the list of pending return when exist" in {
          given
            .sdilBackend
            .returns_pending(UTR)

          val res = sdilConnector.returnsPending(UTR)

          whenReady(res.value) { result =>
            result mustBe Right(returnPeriods)
          }
        }

        "and return UnexpectedResponseFromSDIL when the backend returns an unexpectedResponse code" in {
          given
            .sdilBackend
            .returns_pending_error(UTR)

          val res = sdilConnector.returnsPending(UTR)

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
            result <- sdilConnector.returnsPending(UTR)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(List.empty)
          }
        }
        "and return the list of pending return when exist" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(UTR, SDILSessionKeys.RETURNS_PENDING, returnPeriods))
            result <- sdilConnector.returnsPending(UTR)
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
          given
            .sdilBackend
            .retrieveReturn(UTR, currentReturnPeriod.previous, None)

          val res = sdilConnector.getReturn(UTR, currentReturnPeriod.previous)

          whenReady(res.value) { result =>
            result mustBe Right(None)
          }
        }
        "and return the sdil return when exists" in {
          given
            .sdilBackend
            .retrieveReturn(UTR, currentReturnPeriod.previous, Some(emptyReturn))

          val res = sdilConnector.getReturn(UTR, currentReturnPeriod.previous)

          whenReady(res.value) { result =>
            result mustBe Right(Some(emptyReturn))
          }
        }

        "and return UnexpectedResponseFromSDIL when the backend returns an unexpectedResponse code" in {
          given
            .sdilBackend
            .retrieveReturnError(UTR, currentReturnPeriod.previous)

          val res = sdilConnector.getReturn(UTR, currentReturnPeriod.previous)

          whenReady(res.value) { result =>
            result mustBe Left(UnexpectedResponseFromSDIL)
          }
        }
      }
    }

    "when a submitted returns record is in the cache for the given period" - {
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
            given
              .sdilBackend
              .balance(aSubscription.sdilRef, true)

            val res = sdilConnector.balance(aSubscription.sdilRef, true)

            whenReady(res.value) { result =>
              result mustBe Right(BigDecimal(1000))
            }
          }

          "and return UnexpectedResponseFromSDIL when call fails" in {
            given
              .sdilBackend
              .balancefailure(aSubscription.sdilRef, true)

            val res = sdilConnector.balance(aSubscription.sdilRef, true)

            whenReady(res.value) { result =>
              result mustBe Left(UnexpectedResponseFromSDIL)
            }
          }
        }
      }

      "and the balance is in the cache" - {
        "should return the balance" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(SDIL_REF, SDILSessionKeys.balance(true), BigDecimal(1000)))
            result <- sdilConnector.balance(aSubscription.sdilRef, true)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(BigDecimal(1000))
          }
        }
      }
    }
    "when assessment is false" - {
      "and there is no balance in the cache" - {
        "should call the backend" - {
          "and return the balance when sucessful" in {
            given
              .sdilBackend
              .balance(aSubscription.sdilRef, false)

            val res = sdilConnector.balance(aSubscription.sdilRef, false)

            whenReady(res.value) { result =>
              result mustBe Right(BigDecimal(1000))
            }
          }

          "and return UnexpectedResponseFromSDIL when call fails" in {
            given
              .sdilBackend
              .balancefailure(aSubscription.sdilRef, false)

            val res = sdilConnector.balance(aSubscription.sdilRef, false)

            whenReady(res.value) { result =>
              result mustBe Left(UnexpectedResponseFromSDIL)
            }
          }
        }
      }

      "and the balance is in the cache" - {
        "should return the balance from the cache" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save(SDIL_REF, SDILSessionKeys.balance(false), BigDecimal(1000)))
            result <- sdilConnector.balance(aSubscription.sdilRef, false)
          } yield result

          whenReady(res.value) { result =>
            result mustBe Right(BigDecimal(1000))
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
            given
              .sdilBackend
              .balanceHistory(aSubscription.sdilRef, true, allFinicialItems)

            val res = sdilConnector.balanceHistory(aSubscription.sdilRef, true)

            whenReady(res.value) { result =>
              result mustBe Right(allFinicialItems)
            }
          }

          "and return UnexpectedResponseFromSDIL when call fails" in {
            given
              .sdilBackend
              .balanceHistoryfailure(aSubscription.sdilRef, true)

            val res = sdilConnector.balanceHistory(aSubscription.sdilRef, true)

            whenReady(res.value) { result =>
              result mustBe Left(UnexpectedResponseFromSDIL)
            }
          }
        }
      }

      "and the balanceHistory is in the cache" - {
        "should return the balanceHistory" in {
          val res = for {
            _ <- EitherT.right[VariationsErrors](sdilSessionCache.save[List[FinancialLineItem]](SDIL_REF, SDILSessionKeys.balanceHistory(true), allFinicialItems))
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
            given
              .sdilBackend
              .balanceHistory(aSubscription.sdilRef, false, List.empty)

            val res = sdilConnector.balanceHistory(aSubscription.sdilRef, false)

            whenReady(res.value) { result =>
              result mustBe Right(List.empty)
            }
          }

          "and return UnexpectedResponseFromSDIL when call fails" in {
            given
              .sdilBackend
              .balanceHistoryfailure(aSubscription.sdilRef, false)

            val res = sdilConnector.balanceHistory(aSubscription.sdilRef, false)

            whenReady(res.value) { result =>
              result mustBe Left(UnexpectedResponseFromSDIL)
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
}
