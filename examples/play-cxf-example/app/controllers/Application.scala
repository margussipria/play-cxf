package controllers

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.mvc._
import services.sunset.rise.{LatLonDate, SunSetRiseServiceSoap}

class Application @Inject() (service: SunSetRiseServiceSoap) extends Controller {

  def index = Action {
    Ok("Your new application is ready.")
  }

  def sunriseLondon = Action {
    val now = DateTime.now()
    val request = new LatLonDate
    request.setYear(now.getYear)
    request.setMonth(now.getMonthOfYear)
    request.setDay(now.getDayOfMonth)
    request.setLatitude(60.1733244f)
    request.setLongitude(24.9410248f)

    val response = service.getSunSetRiseTime(request)

    Ok(s"Sunrise in Helsinki: ${response.getSunRiseTime}")
  }

}
