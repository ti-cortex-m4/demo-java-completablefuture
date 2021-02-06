https://www.jesperdj.com/2015/09/26/the-future-is-completable-in-java-8/
https://www.nurkiewicz.com/2013/05/java-8-definitive-guide-to.html
https://www.deadcoderising.com/java8-writing-asynchronous-code-with-completablefuture/
https://www.baeldung.com/java-completablefuture
https://www.baeldung.com/java-9-completablefuture
https://www.callicoder.com/java-8-completablefuture-tutorial/


https://www.marccarre.com/2016/05/08/java-8-concurrency-completablefuture-in-practice.html
https://www.ibm.com/developerworks/library/j-jvmc2/index.html
https://www.infoq.com/articles/Functional-Style-Callbacks-Using-CompletableFuture/
https://en.wikipedia.org/wiki/Futures_and_promises

https://www.youtube.com/watch?v=W7iK74YA5NM&ab_channel=JUGNsk   Сергей Куксенко - Как сделать CompletableFuture еще быстрее
https://www.youtube.com/watch?v=hqR41XVx3kM&ab_channel=JUG.ru   Дмитрий Чуйко — CompletableFuture. Хочется взять и применить
https://www.youtube.com/watch?v=-MBPQ7NIL_Y&ab_channel=JUG.ru   Tomasz Nurkiewicz — CompletableFuture in Java 8, asynchronous processing done right
https://www.youtube.com/watch?v=x5akmCWgGY0&ab_channel=Java     Parallel Streams, CompletableFuture, and All That: Concurrency in Java 8
https://www.youtube.com/watch?v=IwJ-SCfXoAU&ab_channel=Devoxx
https://www.youtube.com/watch?v=0hQvWIdwnw4&ab_channel=Devoxx   Parallel and Asynchronous Programming with Streams and CompletableFuture with Venkat Subramaniam
https://www.youtube.com/watch?v=Q_0_1mKTlnY&ab_channel=GOTOConferences  GOTO 2014 • New Concurrency Utilities in Java 8 • Angelika Langer

https://plantuml.com/class-diagram



>The Java CompletableFuture API is designed to implement multi-stage operations that have a single result. If it’s necessary to implement multi-stage operations that have a stream of results, it’s necessary to use reactive streams implemented in Java Flow API or third-party frameworks (RxJava, etc).



Synchronous vs. asynchronous API
The phrase synchronous API is another way of talking about a traditional call to a method: you call it, the caller waits while the method computes, the method returns, and the caller continues with the returned value. Even if the caller and callee were executed on different threads, the caller would still wait for the callee to complete. This situation gives rise to the phrase blocking call. By contrast, in an asynchronous API the method returns immediately (or at least before its computation is complete), delegating its remaining computation to a thread, which runs asynchronously to the caller—hence, the phrase nonblocking call. The remaining computation gives its value to the caller by calling a callback method,  or the caller invokes a further “wait until the computation is complete” method. This style of computation is common in I/O systems programming: you initiate a disc access, which happens asynchronously while you do more computation, and when you have nothing more useful to do, you wait until the disc blocks are loaded into memory. Note that blocking and nonblocking are often used for specific implementations of I/O by the operating system. However, these terms tend to be used interchangeably with asynchronous and synchronous even in non-I/O contexts.  Executing relatively long-lasting operations by using asynchronous tasks can increase the performance and responsiveness of your application, especially if it relies on one or more remote external services.


https://java-design-patterns.com/patterns/promise/
https://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html
https://salonegupta.wordpress.com/2017/08/24/internals-of-java-futuret/
https://www.infoq.com/presentations/Asynchronous-Scala-Java/
http://www.labviewcraftsmen.com/blog/futures-promises-and-continuations-oh-my
https://www.infoq.com/articles/Functional-Style-Callbacks-Using-CompletableFuture/
https://www.youtube.com/watch?v=CITVS-gwySo&ab_channel=CopperSpice

https://www.anuragkapur.com/blog/devoxx-uk-2018#completablefutures
https://blog.qfotografie.de/2018/05/15/devoxx-uk-deep-dive-streams-and-completablefutures/

http://millross-consultants.com/completion-stage-future-introduction.html
http://millross-consultants.com/completable-future-error-propagation.html

чтобы получить быструю асинхронную обработку мы не должны блокировать потоки