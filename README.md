[![Build Status][ci-img]][ci]

# Idempotency barrier
## Theoretical concept
Even when a sender application sends a message only once,
the receiver application may receive the message more than once.

The term idempotent is used in mathematics to describe a function that produces the same result 
if it is applied to itself: f(x) = f(f(x)). 
In Messaging this concepts translates into a message that has the same effect whether it is received 
once or multiple times. 
This means that a message can safely be resent without causing any problems even if the receiver receives 
duplicates of the same message.

The recipient can explicitly de-dupe messages by keeping track of messages that it already received. 
A unique message identifier simplifies this task and helps detect those cases where 
two legitimate messages with the same message content arrive.

In order to detect and eliminate duplicate messages based on the message identifier, 
the message recipient has to keep a list of already received message identifiers.

## Technical implementation

In order to store the processed message metadata, we have to be in a transactional context.
If something goes wrong, the transaction has to be roll backed.

1. The **IdempotentMessageInterceptor** creates an **IdempotentContext** and stores it in a ThreadLocal
2. The **IdempotentMessageAspect** is an aspect around the **@IdempotentConsumer** annotation. 
It retrieves the IdempotentContext and checks if the message already exists. 
The unique message key is composed by the messageId and the consumerQueueName.

If the message does not exist, the target method is invoked and the message is stored in Redis.

If the message already exists, an error message is logged and the target method is not invoked

[ci-img]: https://api.travis-ci.com/ask4gilles/idempotency-barrier.svg?branch=master
[ci]: https://travis-ci.com/ask4gilles/idempotency-barrier