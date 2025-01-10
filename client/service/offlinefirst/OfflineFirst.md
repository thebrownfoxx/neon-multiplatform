# Offline-First behavior

## Safely repeatable actions

An action is safely repeatable if the client can just infinitely retry it without being sure about its success. This mostly applies to loading data from their unique ID, like groups, members, or messages.

### Implementation

1. Send the request to the server via WebSocket (request ID is not necessary).
2. Wait for the response. If the server did not respond, try again, but space the retries with exponential backoff since you don't want to spam the server.

## Single-fire actions

An action counts as single-fire if it needs to happen only once at the current moment and must be canceled if it failed.

### Examples

- Logging in
- Signing up
- Creating a community

### Implementation

1. The service must send the request to the server, either via WebSocket (with a request ID) or via REST.
2. Wait for the response. If the request times out, report that it timed out, yet we are not sure if it actually didn't get processed.

## Locally cached actions

An action can be locally cached if it can be retried later when connection to the server is available.

### Examples

- Sending of messages
- Liking of posts

### Implementation

1. Save the request to local storage with a tag to let the client know that it must be requested to the server.
2. The UI must be updated as soon as the local storage gets updated. If feedback is essential, it should show the user a pending status.
3. The client must try sending all pending requests to the server and must be retried until they succeed or fail.

## Missing documentation

- Handling of different errors. Some errors warrant retrying while others don't.