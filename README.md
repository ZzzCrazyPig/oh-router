## oh-router

Port mapping proxy based on Netty. The sequence diagram of the core process logic is as follows:

![oh-router](https://github.com/ZzzCrazyPig/oh-router/blob/master/doc/img/oh-router.svg)

Description:
1. Client requests RouteServer's REST API for the first time: `http://{host1}:8080/route/bind/{host3}/3306`, indicate the need to access the target connection
 `{host3}:3306`
2. After the RouteServer receives the request, it initiates a socket client to connect to the ProxyServer
 `{host2}:9000`, and send `ConnectCommand` to the ProxyServer which message body contains the target address
3. After the ProxyServer receives the `ConnectCommand` message, it initiates a socket client to connect the target `{host3}:3306`
4. After ProxyServer connects to the target successfully, it will send a `ConnectResponse` message to the RouteServer connection to indicate that it is connected
5. After the RouteServer receives the `ConnectResponse` message from the ProxyServer, it sends the `AckCommand` to the ProxyServer again to indicate that the proxy session can be formally established
6. RouteServer finds an available tcp port locally, starts the server socket to listen to this port, and the REST API returns this port to inform the Client to connect to this port, which means that a connection needs to be established to access target `{host3}:3306`
7. RouteServer uses the `{host1}:34553` returned from the previous REST API of socket connect to connect to RouteServer. RouteServer repeats steps 2-5 again to establish a proxy session. Until all channels of step 13 are connected, the channel can send and receive messages.

That is, every time the Client needs to access a certain proxy target, it first requests the RouteServer through the REST API to obtain a proxy address, and then the proxy target can be accessed through this address. RouteServer will cache the proxy listening port, and if there is no corresponding session access on the port, the port will be recycled.

## Usage

Introduce the use of local tests, use IDEA and external Tomcat to run tests:

1. Configure ProxyServer tomcat, use port 8081, configure startup parameters `-Dserver.mode=proxy`

2. Configure RouteServer tomcat, use port 8080, configure startup parameters `-Dserver.mode=router`

3. Test to obtain proxy address: `http://127.0.0.1:8080/route/bind/127.0.0.1/8080`

4. After the above interface returns the mapped ip port, you can access the 8080 port service of 127.0.0.1 through this address proxy

> More: oh-router can be used to proxy any tcp-implemented services, such as databases (mysql, etc.), http server, tcp server, etc.

## Special Note

For learning and reference only, don’t use it in a production environment
