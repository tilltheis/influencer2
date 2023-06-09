# influencer 2
Another practice project that implements an Instagram style feed.

## Running
### Backend
`docker-compose up` starts the database cluster that is used by the app and tests.  
`sbt run` starts the API server on http://localhost:8080.
The supported routes can be found in the [AppRouter](src/main/scala/influencer2/application/AppRouter.scala) and [tests](/src/test/scala/influencer2).  
`sbt test` runs the test suite.

### Frontend
`cd frontend && yarn && yarn dev` starts the frontend that can be accessed via http://localhost:5173.

## Technologies
The technologies were primarily chosen based on personal interest.
They're not necessarily the best at solving the given problem.

### Backend
* JVM 17
* Scala 3
* ZIO 2
* MongoDB 6

### Frontend
* Node.js 18
* TypeScript 5
* React 18

## Features
* [x] create posts (picture + message)
* [x] show all posts
* [x] show user profile
* [ ] show personal feed
* [x] like posts
* [ ] follow other users
* [ ] notify followed user
* [x] login
* [x] registration

## Security
* __XSS__ protection is achieved by storing the JWT signature in an HTTP only cookie that cannot be read by the client.
* __CSRF__ protection is not necessary because the authentication token is sent via an HTTP header.

## Tests
Due to the small size and complexity of this project, there are only HTTP router integration tests.

## Possible Feature Extensions
* upload media instead of only linking to it
  * use S3 API client because many cloud storage providers have compatible APIs
  * split media upload from post creation because that allows using `multipart/form-data` uploads and also gives users the chance to upload the file while they prepare the rest of the post
    1. upload media w/ ID + TTL to tmp folder that cannot be served to internet
    2. create post, referencing that media and move media to permanent, public location
       * ensure that users can only reference own media, eg by having separate tmp folder per user
