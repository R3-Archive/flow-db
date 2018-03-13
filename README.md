![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# Flow Database Access CorDapp

This CorDapp provides a simple example of how the node database can be accessed in flows. In this case, the flows 
maintain a table of cryptocurrency values in the node's database. There are three flows:

The CorDapp defines three flows:

* `AddTokenValueFlow`, which adds a new token to the database table with an initial value
* `UpdateTokenValueFlow`, which updates the value of an existing token in the database table
* `QueryTokenValueFlow`, which reads the value of an existing token from the database table

Under the hood, the database accesses are managed by the CryptoValuesDatabaseService CordaService.

Be aware that support of database accesses in flows is currently limited:

* The operation must be executed in a BLOCKING way. Flows don't currently support suspending to await an operation's response
* The operation must be idempotent. If the flow fails and has to restart from a checkpoint, the operation will also be replayed

# Pre-requisites:
  
See https://docs.corda.net/getting-set-up.html.

# Usage

## Running the nodes:

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Interacting with the nodes:

### Via the web API

Add a token to the node's database table by making a PUT request to:

    localhost:10007/api/token/add-token?token=TOKEN_NAME&value=TOKEN_INITIAL_VALUE

Update a token's value in the node's database table by making a POST request to:

    localhost:10007/api/token/update-token?token=TOKEN_NAME&value=TOKEN_NEW_VALUE

And read back a token's value from the node's database table by making a GET request to:

    localhost:10007/api/token/query-token?token=TOKEN_NAME
