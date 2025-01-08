### Default Offline-First

| On new local value            | Local Success | Local Not Found | Unexpected Local Failure |
|-------------------------------|---------------|-----------------|--------------------------|
| **Remote Success**            | emit local    | -               | emit local               | 
| **Remote Not Found**          | emit local    | emit local      | emit local               | 
| **Unexpected Remote Failure** | emit local    | -               | emit local               | 
| **No remote yet**             | emit local    | -               | emit local               | 

| On new remote value           | Local Success | Local Not Found | Unexpected Local Failure | No local yet |
|-------------------------------|---------------|-----------------|--------------------------|--------------|
| **Remote Success**            | update local  | update local    | update local             | update local |
| **Remote Not Found**          | delete local  | delete local    | delete local             | delete local |
| **Unexpected Remote Failure** | -             | emit remote     | -                        | -            |

### Lazy writes

| On new local value            | Local Success | Local Not Found | Unexpected Local Failure |
|-------------------------------|---------------|-----------------|--------------------------|
| **Remote Success**            | emit local    | -               | emit local               | 
| **Remote Not Found**          | emit local    | emit local      | emit local               | 
| **Unexpected Remote Failure** | emit local    | -               | emit local               | 
| **No remote yet**             | emit local    | -               | emit local               | 

| On new remote value           | Local Success | Local Not Found | Unexpected Local Failure | No local yet |
|-------------------------------|---------------|-----------------|--------------------------|--------------|
| **Remote Success**            | update local  | update local    | update local             | update local |
| **Remote Not Found**          | -             | -               | -                        | -            |
| **Unexpected Remote Failure** | -             | emit remote     | -                        | -            |