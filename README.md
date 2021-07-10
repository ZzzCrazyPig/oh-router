## oh-router

基于netty实现的代理转发:

![oh-router](https://github.com/ZzzCrazyPig/oh-router/blob/master/doc/img/oh-router.svg)

流程说明如下:
1. Client首次请求RouteServer的REST接口: `http://{host1}:8080/route/bind/{host3}/3306`，需要访问代理目标 `{host3}:3306`
2. RouteServer收到请求后，发起socket connect 连接ProxyServer `{host2}:9000`，并发送`ConnectCommand`消息，消息体中包含了要访问代理目标地址
3. ProxyServer收到`ConnectCommand`消息以后，发起socket connect连接代理目标 `{host3}:3306`
4. ProxyServer连接代理目标成功以后，往RouteServer的连接发送`ConnectResponse`消息表示已连上
5. RouteServer在接收到ProxyServer发送过来的`ConnectResponse`消息以后，再次发送`AckCommand`往ProxyServer代表此代理链路可正式建立
6. RouteServer在本地找到一个可用的tcp端口，起server socker监听这个端口，REST接口返回此端口告知Client之后连接此端口就代表着需要建立连接访问代理目标 `{host3}:3306`
7. RouteServer使用socket connect 前面REST接口返回的 `{host1}:34553` 来连接RouteServer，RouteServer再次重复2-5的步骤建立一个代理会话链路，直到13所有的通道建立起联系，通道便可发送消息接收消息

即每次Client需要访问某个代理目标，则先通过REST API请求RouteServer得到一个代理地址，后面通过这个地址即可访问代理目标。RouteServer会缓存代理监听端口，若端口上没有对应的链路访问，则会将端口进行回收处理。

## 使用介绍

介绍本地测试的使用, 使用IDEA 和 外置Tomcat 运行测试:

1. 配置proxy tomcat, 使用端口8081, 配置启动参数 -Dserver.mode=proxy

2. 配置router tomcat, 使用端口8080, 配置启动参数 -Dserver.mode=router

3. 测试获取代理端口: http://127.0.0.1:8080/route/bind/127.0.0.1/8080

4. 上述接口返回映射的ip port后即可通过此地址代理访问127.0.0.1的8080端口服务

> 可用于代理任何tcp实现的服务, 如数据库(mysql等), http server, tcp server等等

## 特别说明

仅供学习参考使用, 切莫用于生产环境
