# Java: CompletableFuture

Java's completable future framework provides an asynchronous & reactive concurrent programming model

Supports dependent actions that trigger upon completion of async operations

Async operations can be forked, chained, & joined

Async operations can run concurrently in thread pools
Either a (common) fork-join pool or various types of preor user-defined thread pools

Java completable futures often need no explicit synchronization or threading when developing concurrent apps!
Alleviates many accidental & inherent complexities of concurrent programming
Java class libraries handle locking needed to protect shared mutable state

## The Pros & Cons of Synchrony

Method calls in typical Java programs are largely synchronous i.e., a callee borrows the thread of its caller until its computation(s) finish
Note “request/response” nature of these calls

Pros of synchronous calls:
• “Intuitive” to program & debug, e.g.
    • Maps onto common two-way method patterns
    • Local caller state retained when callee returns

Cons of synchronous calls:
• May not leverage all parallelism available in multi-core systems
    • Blocking threads incur overhead
        • e.g., synchronization, context switching, data movement, & memory management costs
    • Selecting right # of threads is hard
        A large # of threads may help to improve performance, but can also waste resources
        A small # of threads may conserve resources at the cost of performance
• May need to change common fork-join pool size in a Java parallel stream

Asynchronous operations can alleviate limitations with synchronous operations
• Asynchrony is a means of concurrent programming where the caller does not block while waiting for the called code to finish

Pros of asynchronous operations
• Responsiveness
    • A calling thread needn’t block waiting for the async request to complete
• Elasticity
    • Multiple requests can run scalably & concurrently on multiple cores
        • Elasticity is particularly useful to auto-scale computationsin cloud environments

Cons of asynchronous operations
• Unpredictability
    • Response times may not unpredictable due to non-determinism of async operations
    • Results can occur in a different order than the original calls were made
• Complicated programming & debugging
    • The patterns & best-practices of asynchronous programming are not well understood
    • Errors can be hard to track due to unpredictability

Two things are necessary for the pros of asynchrony to outweigh the cons
• Performance should improve to offset the increased complexity of programming & debugging
• An asynchronous programming model should reflect the key principles of the reactive paradigm

## Future

Methods on Java Future can manage a task’s lifecycle after it’s submitted to run asynchronously

A future can be tested for completion: isDone
A future be tested for cancellation & cancelled: isCancelled, cancel
A future can retrieve a two-way task’s result: get, get(long,TimeUnit)

An Java async call returns a future & continues running the computation in the background

ExecutorService.submit() can initiate an async call in Java
• Create a thread pool
• Submit a task
• Return a future
• Run computation asynchronously

When the async call completes the future is triggered & the result is available
• get() can block 
• get() can also be (time-)polled

get() blocks if necessary for the computation to complete & then retrieves its result

Computations can complete in a different order than the async calls were made

A future is essentially a proxy that represents the result(s) of an async call
Result obtained only after the computation completes
https://en.wikipedia.org/wiki/Futures_and_promises

Pros of async calls with Java futures 
• May leverage parallelism more effectively with fewer threads
    • Queue async computations for execution in a pool of threads
    • Automatically tune # of threads
    • Results can be taken from queue of completed futures: ExecutorCompletionService
    • Can block until the result of an async two-way task is available
    • Can also poll or time-block
    • Can be canceled & tested to see if a task is done

Cons of async calls with Java futures
    • Limited feature set
    • Cannot be completed explicitly
        • e.g., additional mechanisms like FutureTask are needed
    • Cannot be chained fluently to handle async results
    • Cannot be triggered reactively
        • i.e., must (timed-)wait or poll
    • Cannot be treated efficiently as a collection of futures
        Can’t wait efficiently for the completion of whichever async computation finishes first

In general, it’s awkward & inefficient to “compose” multiple futures
These limitations with Java futures motivate the need for the Java completable futures framework!

The completable future framework overcomes Java future limitations
• Can be completed explicitly
    future.complete
    After complete() is done calls to join() will unblock
• Can be chained fluently to handle async results efficiently & cleanly
    The action of each “completion stage” is triggered when the future from the previous stage completes asynchronously
• Can be triggered reactively/efficiently as a collection of futures w/out undue overhead
    Create a single future that will be triggered when a group of other futures all complete

Java completable futures can also be combined with Java sequential streams

## CompletionStage

### triggered by a single previous stage
Chain together actions that perform async result processing & composition
Help make programs more responsive by not blocking user code

thenApply(Async)
thenAccept(Async)
thenRun(Async)

### triggered by both of two previous stages
thenCombine
thenAcceptBoth
runAfterBoth

### ?
thenCompose(Async)

### triggered by either one of two previous stages
applyToEither
acceptEither
runAfterEither

## CompletableFuture

### methods to create

#### incompleted
new CompletableFuture<>()
    Create an incomplete future

##### Factory methods
Initiate async two-way or one-way computations without using threads explicitly
Help make programs more elastic by leveraging a pool of worker threads

Four factory methods initiate async computations
These computations may or may not return a value
Async functionality runs in a thread pool
However, a pre- or user-defined thread pool can also be given

runAsync
supplyAsync

• supplyAsync() allows twoway calls via a supplier
• Can be passed params & returns a value
• supplyAsync() does not create a new thread!
• Instead, it return a future that’s completed by a worker thread running in common fork-join pool

Methods Params Returns Behavior
supplyAsync
Supplier 
Completable Future with result of Supplier
Asynchronouslyrun supplier incommon fork/join pool

supplyAsync
Supplier,Executor
CompletableFuture withresult ofSupplier
Asynchronously run supplier in given executor pool

• runAsync() enables oneway calls via a runnable
runAsync() enables oneway calls via a runnable
• Can be passed params,but returns no values
• Any output must therefore come from “side-effects”

Methods Params Returns Behavior
runAsync
Runnable 
Completable Future with result of Void
Asynchronously run runnable in common fork/join pool

runAsync
Runnable,Executor
Completable Future with result of Void
Asynchronously run runnable in given executor pool

#### completed
completedFuture,completedStage
A completable future can be initialized to a value/constant

failedFuture,failedStage

### methods to complete
boolean complete(java.lang.Object)
    Can be completed explicitly
    i.e., sets result returned by get()/join() to a given value
boolean completeExceptionally(java.lang.Throwable)
boolean cancel(boolean)

• cancel() doesn’t interruptthe computation by default
    www.nurkiewicz.com/2015/03/completablefuture-cant-be-interrupted.html

### methods to verify
isDone
isCompletedExceptionally
isCancelled

### methods to get
java.lang.Object get()
java.lang.Object get(long,TimeUnit)

java.lang.Object join()     
    Define a join() method Behaves like get() without using checked exceptions
    CompletableFuture::join can be used as a method reference in a Java stream

java.lang.Object getNow(java.lang.Object)
CompletableFuture orTimeout(long,TimeUnit)

### methods to handle errors
Handle exceptional conditions at runtime
Help make programs more resilient by handling erroneous computations gracefully
exceptionally(Async)
handle
whenComplete

### methods with timeout

### methods to combine multiple futures into one
“Arbitrary-arity” methods
• Process futures in bulk by combine multiple futures into a single future
Help make programs more responsive by not blocking user code
allOf 
anyOf