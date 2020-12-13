# Java: CompletableFuture

## CompletionStage

### triggered by a single previous stage
thenApply
thenAccept
thenRun

### triggered by both of two previous stages
thenCombine
thenAcceptBoth
runAfterBoth

### triggered by either one of two previous stages
applyToEither
acceptEither
runAfterEither

## CompletableFuture

### Future methods

### CompletionStage methods

### methods to combine multiple futures into one: 
allOf 
anyOf