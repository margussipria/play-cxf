package services.hello

import javax.jws.WebService

@WebService(endpointInterface = "services.hello.HelloWorld")
class HelloWorldImpl extends HelloWorld {

    @Override
    def sayHi(text: String): String = {
        "Hello " + text
    }

    @Override
    def abc(word: String): String = {
        word.toLowerCase()
    }
}
