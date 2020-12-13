# Java: CompletableFuture

## CompletionStage

### triggered by a single previous stage
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
constructor
runAsync
supplyAsync

#### completed
completedFuture,completedStage
failedFuture,failedStage

### methods to complete
boolean complete(java.lang.Object)
boolean completeExceptionally(java.lang.Throwable)
boolean cancel(boolean)

### methods to verify
isDone
isCompletedExceptionally
isCancelled

### methods to get
java.lang.Object get()
java.lang.Object get(long,TimeUnit)
java.lang.Object join()
java.lang.Object getNow(java.lang.Object)
CompletableFuture orTimeout(long,TimeUnit)

### methods to handle errors
exceptionally(Async)
handle
whenComplete

### methods with timeout

### methods to combine multiple futures into one
allOf 
anyOf