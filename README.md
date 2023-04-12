# influencer 2
Another practice project that implements an Instagram style feed.

## Running
`sbt run` will start the API server on http://localhost:8080.
The supported routes can be found in the [AppRouter](src/main/scala/influencer2/http/AppRouter.scala) and [tests](/src/test/scala/influencer2).  
`sbt test` will run the test suite.

## Technologies
* JVM 17
* Scala 3
* ZIO 2
* MongoDB 6

## Features
* create posts (picture + message)
* show feed
* like posts
* follow other users (+notify followed user)
* login
* registration

## Security
* __XSS__ protection is achieved by storing the JWT signature in an HTTP only cookie that cannot be read by the client.
* __CSRF__ protection is not necessary because the authentication token is sent via an HTTP header.

## Tests
Because of the small size and complexity of this project, there are only HTTP router integration tests.
