## oh-router

基于netty实现的代理转发:

```
Client (Request to connect ip1:port1)  =>  RouterServer(router.server) => ProxyServer (proxy.server:9000) => ip1:port1

Client connect router.server:xxx       <=> bind router.server:xxx to connect <=> ProxyServer (...) <=> ip1:port1         
```

![oh-router](https://github.com/ZzzCrazyPig/oh-router/blob/master/doc/img/oh-router.svg)

## 使用介绍

介绍本地测试的使用, 使用IDEA + TOMCAT 运行测试:

1. 配置proxy tomcat, 使用端口8081, 配置-Dserver.mode=proxy

2. 配置router tomcat, 使用端口8080, 配置-Dserver.mode=router

3. 测试获取代理端口: http://127.0.0.1:8080/route/bind/127.0.0.1/8080

4. 上述接口返回映射的ip port后即可通过此地址代理访问127.0.0.1的8080端口服务

> 可用于代理任何tcp实现的服务, 如数据库(mysql等), http server, tcp server等等

## 特别说明

仅供学习参考使用, 切莫用于生产环境
