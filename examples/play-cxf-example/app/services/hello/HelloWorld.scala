package services.hello

import javax.jws.WebService

@WebService
trait HelloWorld {
    def sayHi(text: String): String
    def abc(word: String): String
}
