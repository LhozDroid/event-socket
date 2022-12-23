# Java Event-Oriented Sockets

A simple Java library to handle sockets with events... nothing else...

## Server

1- Create the server using the builder

```java
final ServerSocket server = new ServerSocketBuilder()//
		.withAutoReopen(true)//
		.withClients(3)//
		.withTimeout(250)//
		.withPort(9797)//
		.build();
```

2- Add a listener

```java
final ServerListener listener = new ServerListener();
server.add(listener);
```

3- Start the server

```java
server.start();
```

## Client

1- Create the client using the builder

```java
ClientSocket client = new ClientSocketBuilder() //
		.withAddress("localhost")//
		.withPort(9797)//
		.withReadTries(3)//
		.withWriteTries(3)//
		.withTimeout(250)//
		.build();
```

2- Add a listener

```java
ClientListener listener = new ClientListener();
client.add(listener);
```

3- Start the client

```java
client.start();
```

The test folder has one example

<a href="https://www.paypal.com/donate/?cmd=_donations&business=CSQRVLE2D43NU&item_name=Buy+me+a+beer!&currency_code=USD">
  <strong>Buy me a beer!</strong>
</a>

