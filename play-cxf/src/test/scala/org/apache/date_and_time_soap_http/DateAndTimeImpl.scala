package org.apache.date_and_time_soap_http

import java.time._
import java.util.GregorianCalendar

import javax.inject.Inject
import javax.xml.datatype.{DatatypeConstants, DatatypeFactory, XMLGregorianCalendar}

import scala.language.implicitConversions

class DateAndTimeImpl @Inject() (
  clock: Clock
) extends DateAndTime {

  def `with`[T](value: T)(f: T => Unit): T = { f(value); value }

  override def askTime(in: AskTimeRequest): AskTimeResponse = {

    val zoneId = ZoneId.of(in.getTimeZone)
    val utcClock = clock.withZone(ZoneOffset.UTC)

    `with`(new AskTimeResponse) { response =>
      response.setResponse(ZonedDateTime.now(utcClock).withZoneSameInstant(zoneId))
    }
  }

  implicit private def zonedDateTimeToXMLGregorianCalendar(time: ZonedDateTime): XMLGregorianCalendar = {
    val gregorianCalendar = GregorianCalendar.from(time)

    val xmlGregorian = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar)
    xmlGregorian.setTimezone(DatatypeConstants.FIELD_UNDEFINED)
    xmlGregorian
  }
}
