# Play Scala Game of Life

This is a simple implementation of [Conway's Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) 
using [Play for Scala](https://www.playframework.com).
It's a fun project for comparison with other implementations in
[Ruby](https://github.com/christian-schlichtherle/ruby-game-of-life),
[Scala with Akka](https://github.com/christian-schlichtherle/akka-game-of-life) and
[RxScala](https://github.com/christian-schlichtherle/rxscala-game-of-life).

This implementation streams generations of boards to the Web browser using an HTML 5 `EventSource`.

## How to Use

First, start sbt:

    sbt

Now, for starting the web app:

    > server/run
    
Next, start a web browser and browse to http://localhost:9000/ .
You should see something like this:

![Still image of Conway's Game of Life](docs/images/screenshot1.png)]

Here's a screen capture of the web app in fullscreen mode:

![Video recording of Conway's Game of Life](https://user-images.githubusercontent.com/1545428/131516765-b09adabe-df1a-40b4-a0e5-c9a11f30487c.mov)
